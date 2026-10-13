package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.CivicRepository
import com.example.data.local.CivicDatabase
import com.example.data.local.ComplaintConverters
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ComplaintPersistenceTest {

    private lateinit var context: Context
    private lateinit var inMemoryDb: CivicDatabase

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        inMemoryDb = Room.inMemoryDatabaseBuilder(context, CivicDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        inMemoryDb.close()
    }

    @Test
    fun `insert and retrieve complaint entity via Room DAO`() = runTest {
        val dao = inMemoryDb.complaintDao()
        val originalComplaint = Complaint(
            referenceId = "CN-2026-9999",
            category = IncidentCategory.POTHOLES,
            title = "Deep pothole on KIIT Road",
            description = "Hazardous road crater causing traffic slow down.",
            hasPhoto = true,
            photoName = "pothole.jpg",
            latitude = 20.3533,
            longitude = 85.8172,
            address = "KIIT Square, Patia",
            submittedDate = "Today, 10:30 AM",
            status = ComplaintStatus.UNDER_REVIEW,
            timeline = listOf(
                TimelineStep(
                    status = ComplaintStatus.SUBMITTED,
                    title = "Citizen Report Logged",
                    timestamp = "10:30 AM",
                    note = "Logged via mobile app",
                    isCompleted = true,
                    isCurrent = false
                )
            ),
            assignedOfficer = "Roads & Traffic Department",
            aiAnalysis = AiAnalysisResult(
                suggestedCategory = IncidentCategory.POTHOLES,
                incidentSummary = "Road surface hazard",
                suggestedPriority = IncidentSeverity.HIGH,
                recommendedDepartment = "Roads & Traffic",
                reasoning = "Hazardous for two-wheelers",
                confidenceScore = 96,
                suggestedPrecaution = "Caution barricades needed"
            ),
            userId = "USER-12345"
        )

        dao.insertComplaint(originalComplaint.toEntity())

        // Retrieve by ID
        val retrievedEntity = dao.getComplaintById("CN-2026-9999")
        assertNotNull(retrievedEntity)
        val domainComplaint = retrievedEntity!!.toDomain()
        assertEquals("CN-2026-9999", domainComplaint.referenceId)
        assertEquals(IncidentCategory.POTHOLES, domainComplaint.category)
        assertEquals("Deep pothole on KIIT Road", domainComplaint.title)
        assertEquals("USER-12345", domainComplaint.userId)
        assertEquals(1, domainComplaint.timeline.size)
        assertEquals(96, domainComplaint.aiAnalysis.confidenceScore)

        // Retrieve by User ID
        val userComplaints = dao.getComplaintsForUserOnce("USER-12345")
        assertEquals(1, userComplaints.size)
        assertEquals("CN-2026-9999", userComplaints[0].referenceId)

        // Other users should not see this complaint
        val otherUserComplaints = dao.getComplaintsForUserOnce("OTHER-USER-999")
        assertTrue(otherUserComplaints.isEmpty())
    }

    @Test
    fun `converter round-trip preserves all fields faithfully`() {
        val original = Complaint(
            referenceId = "CN-2026-5555",
            category = IncidentCategory.FLOODING,
            title = "Drainage Overflow at Sector 4",
            description = "Stagnant stormwater near market complex.",
            hasPhoto = false,
            photoName = "",
            latitude = 20.3500,
            longitude = 85.8200,
            address = "Sector 4 Market",
            submittedDate = "Today, 02:15 PM",
            status = ComplaintStatus.SUBMITTED,
            timeline = listOf(
                TimelineStep(
                    status = ComplaintStatus.SUBMITTED,
                    title = "Logged",
                    timestamp = "02:15 PM",
                    note = "Auto-recorded",
                    isCompleted = true,
                    isCurrent = true
                )
            ),
            assignedOfficer = "Drainage Division",
            aiAnalysis = AiAnalysisResult(
                suggestedCategory = IncidentCategory.FLOODING,
                incidentSummary = "Drainage issue",
                suggestedPriority = IncidentSeverity.MEDIUM,
                recommendedDepartment = "Drainage & Sewerage",
                reasoning = "Monsoon runoff",
                confidenceScore = 91,
                suggestedPrecaution = "Avoid waterlogged section",
                isReviewedByCitizen = true
            ),
            userId = "CITIZEN-ALPHA"
        )

        val entity = original.toEntity()
        val converted = entity.toDomain()

        assertEquals(original.referenceId, converted.referenceId)
        assertEquals(original.category, converted.category)
        assertEquals(original.title, converted.title)
        assertEquals(original.description, converted.description)
        assertEquals(original.hasPhoto, converted.hasPhoto)
        assertEquals(original.latitude, converted.latitude, 0.0001)
        assertEquals(original.longitude, converted.longitude, 0.0001)
        assertEquals(original.address, converted.address)
        assertEquals(original.status, converted.status)
        assertEquals(original.assignedOfficer, converted.assignedOfficer)
        assertEquals(original.userId, converted.userId)
        assertEquals(original.timeline.size, converted.timeline.size)
        assertEquals(original.timeline[0].title, converted.timeline[0].title)
        assertEquals(original.aiAnalysis.confidenceScore, converted.aiAnalysis.confidenceScore)
        assertEquals(original.aiAnalysis.recommendedDepartment, converted.aiAnalysis.recommendedDepartment)
    }

    @Test
    fun `submitNewComplaint validates input and saves to database`() = runTest {
        val repository = CivicRepository(
            context,
            database = inMemoryDb,
            firestoreService = com.example.data.backend.NoOpCivicFirestoreService()
        )

        val ai = AiAnalysisResult(
            suggestedCategory = IncidentCategory.STREETLIGHTS,
            incidentSummary = "Broken street lamp",
            suggestedPriority = IncidentSeverity.LOW,
            recommendedDepartment = "Electrical Department",
            reasoning = "Non-functional luminaire",
            confidenceScore = 88,
            suggestedPrecaution = "Use torch at night"
        )

        // 1. Validation test: blank title and description must fail
        val invalidResult = repository.submitNewComplaint(
            title = "",
            description = "   ",
            category = IncidentCategory.STREETLIGHTS,
            address = "Main Street",
            latitude = 20.35,
            longitude = 85.81,
            hasPhoto = false,
            aiAnalysis = ai
        )
        assertTrue(invalidResult.isFailure)

        // 2. Validation test: blank address must fail
        val noAddressResult = repository.submitNewComplaint(
            title = "Dark Street",
            description = "Light not working",
            category = IncidentCategory.STREETLIGHTS,
            address = "   ",
            latitude = 20.35,
            longitude = 85.81,
            hasPhoto = false,
            aiAnalysis = ai
        )
        assertTrue(noAddressResult.isFailure)

        // 3. Valid submission succeeds and is saved
        val validResult = repository.submitNewComplaint(
            title = "Broken Streetlight on Pole 12",
            description = "The lamp flickers and shuts off after dusk.",
            category = IncidentCategory.STREETLIGHTS,
            address = "Pole 12, Park Road",
            latitude = 20.3512,
            longitude = 85.8190,
            hasPhoto = false,
            aiAnalysis = ai
        )
        assertTrue(validResult.isSuccess)
        val created = validResult.getOrThrow()
        assertNotNull(created.referenceId)
        assertTrue(created.referenceId.startsWith("CN-2026-"))

        // Verify it was persisted in Room DB
        val inDb = inMemoryDb.complaintDao().getComplaintById(created.referenceId)
        assertNotNull(inDb)
        assertEquals(created.referenceId, inDb!!.referenceId)
        assertEquals("Broken Streetlight on Pole 12", inDb.title)

        repository.close()
    }

    @Test
    fun `persisted complaint survives repository recreation (app restart simulation)`() = runTest {
        // Step 1: Submit complaint in initial session
        val session1Repo = CivicRepository(
            context,
            database = inMemoryDb,
            firestoreService = com.example.data.backend.NoOpCivicFirestoreService()
        )
        val ai = AiAnalysisResult(
            suggestedCategory = IncidentCategory.GARBAGE,
            incidentSummary = "Uncollected municipal waste",
            suggestedPriority = IncidentSeverity.HIGH,
            recommendedDepartment = "Sanitation & Waste Management",
            reasoning = "Piled up refuse creating health hazard",
            confidenceScore = 95,
            suggestedPrecaution = "Stay clear of area"
        )

        val submitResult = session1Repo.submitNewComplaint(
            title = "Garbage Pile on 5th Avenue",
            description = "Waste container overflowing for 4 days.",
            category = IncidentCategory.GARBAGE,
            address = "5th Avenue Corner",
            latitude = 20.3540,
            longitude = 85.8150,
            hasPhoto = true,
            aiAnalysis = ai
        )
        assertTrue(submitResult.isSuccess)
        val savedId = submitResult.getOrThrow().referenceId
        session1Repo.close()

        // Step 2: Simulate app restart / ViewModel recreation with the same database
        val session2Repo = CivicRepository(
            context,
            database = inMemoryDb,
            firestoreService = com.example.data.backend.NoOpCivicFirestoreService()
        )

        // Query complaints from DAO directly and verify it is present
        val persistedList = inMemoryDb.complaintDao().getComplaintsForUserOnce("CN-CITIZEN-001")
        assertEquals(1, persistedList.size)
        assertEquals(savedId, persistedList[0].referenceId)
        assertEquals("Garbage Pile on 5th Avenue", persistedList[0].title)

        session2Repo.close()
    }
}
