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
class ComplaintPersistenceRobolectricTest {

    private lateinit var context: Context
    private lateinit var testDb: CivicDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        testDb = Room.inMemoryDatabaseBuilder(context, CivicDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        testDb.close()
    }

    @Test
    fun `complaint dao inserts and retrieves complaint correctly`() = runTest {
        val dao = testDb.complaintDao()
        val analysis = AiAnalysisResult(
            suggestedCategory = IncidentCategory.POTHOLES,
            incidentSummary = "Deep pothole cluster on Patia main road.",
            suggestedPriority = IncidentSeverity.HIGH,
            recommendedDepartment = "Works Department",
            reasoning = "Visual detection shows exposed aggregate.",
            confidenceScore = 95,
            suggestedPrecaution = "Place safety cones."
        )

        val complaint = Complaint(
            referenceId = "CN-2026-9901",
            category = IncidentCategory.POTHOLES,
            title = "Deep pothole on Patia main road",
            description = "Severe road damage endangering two-wheelers.",
            hasPhoto = true,
            photoName = "pothole.jpg",
            latitude = 20.3548,
            longitude = 85.8195,
            address = "KIIT Main Avenue, Patia",
            submittedDate = "Today, 10:30 AM",
            status = ComplaintStatus.SUBMITTED,
            timeline = listOf(
                TimelineStep(
                    status = ComplaintStatus.SUBMITTED,
                    title = "Report Logged",
                    timestamp = "10:30 AM",
                    note = "Citizen filed report",
                    isCompleted = true,
                    isCurrent = true
                )
            ),
            assignedOfficer = "Assigned to Works Department",
            aiAnalysis = analysis,
            userId = "citizen_uid_101"
        )

        dao.insertComplaint(complaint.toEntity())

        val retrievedEntity = dao.getComplaintById("CN-2026-9901")
        assertNotNull(retrievedEntity)
        val retrievedDomain = retrievedEntity!!.toDomain()

        assertEquals("CN-2026-9901", retrievedDomain.referenceId)
        assertEquals(IncidentCategory.POTHOLES, retrievedDomain.category)
        assertEquals("Deep pothole on Patia main road", retrievedDomain.title)
        assertEquals("citizen_uid_101", retrievedDomain.userId)
        assertEquals(20.3548, retrievedDomain.latitude, 0.0001)
        assertEquals(85.8195, retrievedDomain.longitude, 0.0001)
        assertEquals(1, retrievedDomain.timeline.size)
        assertEquals(95, retrievedDomain.aiAnalysis.confidenceScore)
    }

    @Test
    fun `citizen data isolation - user A cannot see user B complaints`() = runTest {
        val dao = testDb.complaintDao()
        val analysis = AiAnalysisResult(
            suggestedCategory = IncidentCategory.GARBAGE,
            incidentSummary = "Garbage overflow",
            suggestedPriority = IncidentSeverity.MEDIUM,
            recommendedDepartment = "Sanitation",
            reasoning = "Bin fill volume > 90%",
            confidenceScore = 90,
            suggestedPrecaution = "Avoid contact"
        )

        val complaintUserA = Complaint(
            referenceId = "CN-2026-A001",
            category = IncidentCategory.GARBAGE,
            title = "Garbage near market",
            description = "Overflowing waste",
            latitude = 20.2882,
            longitude = 85.8456,
            address = "Saheed Nagar",
            submittedDate = "Today, 09:00 AM",
            status = ComplaintStatus.SUBMITTED,
            timeline = emptyList(),
            assignedOfficer = "Sanitation Officer",
            aiAnalysis = analysis,
            userId = "user_A"
        )

        val complaintUserB = Complaint(
            referenceId = "CN-2026-B001",
            category = IncidentCategory.STREETLIGHTS,
            title = "Broken streetlights",
            description = "Dark corridor",
            latitude = 20.2589,
            longitude = 85.7820,
            address = "Khandagiri",
            submittedDate = "Today, 09:30 AM",
            status = ComplaintStatus.SUBMITTED,
            timeline = emptyList(),
            assignedOfficer = "Electrical Utility",
            aiAnalysis = analysis,
            userId = "user_B"
        )

        dao.insertComplaint(complaintUserA.toEntity())
        dao.insertComplaint(complaintUserB.toEntity())

        val userAComplaints = dao.getComplaintsForUserOnce("user_A")
        val userBComplaints = dao.getComplaintsForUserOnce("user_B")

        assertEquals(1, userAComplaints.size)
        assertEquals("CN-2026-A001", userAComplaints[0].referenceId)

        assertEquals(1, userBComplaints.size)
        assertEquals("CN-2026-B001", userBComplaints[0].referenceId)
    }

    @Test
    fun `complaint submission validation rejects blank title and description`() = runTest {
        val repo = CivicRepository(
            context = context,
            database = testDb,
            firestoreService = com.example.data.backend.NoOpCivicFirestoreService()
        )
        try {
            val analysis = AiAnalysisResult(
                suggestedCategory = IncidentCategory.OTHER,
                incidentSummary = "General issue",
                suggestedPriority = IncidentSeverity.LOW,
                recommendedDepartment = "General Admin",
                reasoning = "General",
                confidenceScore = 80,
                suggestedPrecaution = "None"
            )

            val result = repo.submitNewComplaint(
                title = "",
                description = "",
                category = IncidentCategory.OTHER,
                address = "Main Street",
                latitude = 20.0,
                longitude = 85.0,
                hasPhoto = false,
                aiAnalysis = analysis
            )

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IllegalArgumentException)
        } finally {
            repo.close()
        }
    }

    @Test
    fun `submitted complaint persists and survives repository re-instantiation`() = runTest {
        // 1. Initial app run: Submit a complaint using repo1
        val repo1 = CivicRepository(
            context = context,
            database = testDb,
            firestoreService = com.example.data.backend.NoOpCivicFirestoreService()
        )
        try {
            val analysis = AiAnalysisResult(
                suggestedCategory = IncidentCategory.FLOODING,
                incidentSummary = "Waterlogging at underpass",
                suggestedPriority = IncidentSeverity.CRITICAL,
                recommendedDepartment = "Drainage Division",
                reasoning = "Telemetry indicates 20cm water accumulation",
                confidenceScore = 98,
                suggestedPrecaution = "Avoid route"
            )

            val submitResult = repo1.submitNewComplaint(
                title = "Severe waterlogging at underpass",
                description = "Drain clogged, vehicles unable to pass.",
                category = IncidentCategory.FLOODING,
                address = "Rasulgarh Flyover Undercrossing",
                latitude = 20.3015,
                longitude = 85.8205,
                hasPhoto = true,
                aiAnalysis = analysis
            )

            assertTrue("Complaint submission should succeed", submitResult.isSuccess)
            val submitted = submitResult.getOrNull()
            assertNotNull(submitted)
            val savedRefId = submitted!!.referenceId

            // 2. Simulate closing and reopening app by creating repo2 pointing to the same database
            val repo2 = CivicRepository(
                context = context,
                database = testDb,
                firestoreService = com.example.data.backend.NoOpCivicFirestoreService()
            )
            try {
                val loadedComplaints = repo2.complaintDao.getComplaintsForUserOnce(submitted.userId)

                assertEquals(1, loadedComplaints.size)
                assertEquals(savedRefId, loadedComplaints[0].referenceId)
                assertEquals("Severe waterlogging at underpass", loadedComplaints[0].title)
                assertEquals("Drainage Division", loadedComplaints[0].assignedOfficer.substringAfter("Assigned to "))
            } finally {
                repo2.close()
            }
        } finally {
            repo1.close()
        }
    }
}
