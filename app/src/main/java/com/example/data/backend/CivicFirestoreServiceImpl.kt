package com.example.data.backend

import android.content.Context
import android.util.Log
import com.example.data.local.ComplaintConverters
import com.example.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.tasks.await

class CivicFirestoreServiceImpl(
    private val context: Context
) : CivicFirestoreService {

    private val tag = "CivicFirestore"

    private val firestore: FirebaseFirestore?
        get() = try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                FirebaseFirestore.getInstance()
            } else {
                null
            }
        } catch (e: Throwable) {
            Log.w(tag, "FirebaseFirestore instance unavailable: ${e.message}")
            null
        }

    override suspend fun submitComplaint(complaint: Complaint): Result<String> {
        val db = firestore ?: return Result.failure(
            IllegalStateException("Firebase Firestore is not initialized on this client.")
        )
        return try {
            val payload = hashMapOf<String, Any>(
                "referenceId" to complaint.referenceId,
                "userId" to complaint.userId,
                "category" to complaint.category.name,
                "title" to complaint.title,
                "description" to complaint.description,
                "hasPhoto" to complaint.hasPhoto,
                "photoName" to complaint.photoName,
                "latitude" to complaint.latitude,
                "longitude" to complaint.longitude,
                "address" to complaint.address,
                "submittedDate" to complaint.submittedDate,
                "status" to complaint.status.name,
                "timelineJson" to ComplaintConverters.timelineToJson(complaint.timeline),
                "assignedOfficer" to complaint.assignedOfficer,
                "aiAnalysisJson" to ComplaintConverters.aiAnalysisToJson(complaint.aiAnalysis),
                "updatedAt" to System.currentTimeMillis()
            )

            // Store in global complaints and in user-scoped path with timeout for offline/resilience
            kotlinx.coroutines.withTimeoutOrNull(2000L) {
                db.collection("complaints")
                    .document(complaint.referenceId)
                    .set(payload, SetOptions.merge())
                    .await()

                if (complaint.userId.isNotBlank()) {
                    db.collection("users")
                        .document(complaint.userId)
                        .collection("complaints")
                        .document(complaint.referenceId)
                        .set(payload, SetOptions.merge())
                        .await()
                }
            }

            Result.success(complaint.referenceId)
        } catch (e: Throwable) {
            Log.w(tag, "Failed to sync complaint to Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun getComplaintById(referenceId: String): Result<Complaint?> {
        val db = firestore ?: return Result.failure(
            IllegalStateException("Firebase Firestore is not initialized.")
        )
        return try {
            val doc = db.collection("complaints").document(referenceId).get().await()
            if (!doc.exists()) {
                Result.success(null)
            } else {
                val catStr = doc.getString("category") ?: IncidentCategory.OTHER.name
                val cat = try {
                    IncidentCategory.valueOf(catStr)
                } catch (e: Exception) {
                    IncidentCategory.OTHER
                }
                val statStr = doc.getString("status") ?: ComplaintStatus.SUBMITTED.name
                val stat = try {
                    ComplaintStatus.valueOf(statStr)
                } catch (e: Exception) {
                    ComplaintStatus.SUBMITTED
                }
                val timelineJson = doc.getString("timelineJson") ?: ""
                val aiJson = doc.getString("aiAnalysisJson") ?: ""

                val complaint = Complaint(
                    referenceId = doc.getString("referenceId") ?: referenceId,
                    category = cat,
                    title = doc.getString("title") ?: "",
                    description = doc.getString("description") ?: "",
                    hasPhoto = doc.getBoolean("hasPhoto") ?: false,
                    photoName = doc.getString("photoName") ?: "",
                    latitude = doc.getDouble("latitude") ?: 0.0,
                    longitude = doc.getDouble("longitude") ?: 0.0,
                    address = doc.getString("address") ?: "",
                    submittedDate = doc.getString("submittedDate") ?: "",
                    status = stat,
                    timeline = ComplaintConverters.jsonToTimeline(timelineJson),
                    assignedOfficer = doc.getString("assignedOfficer") ?: "",
                    aiAnalysis = ComplaintConverters.jsonToAiAnalysis(aiJson, cat),
                    userId = doc.getString("userId") ?: ""
                )
                Result.success(complaint)
            }
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override fun observeCitizenComplaints(citizenUid: String): Flow<List<Complaint>> {
        val db = firestore ?: return emptyFlow()
        if (citizenUid.isBlank()) return emptyFlow()

        return callbackFlow {
            val listener = db.collection("users")
                .document(citizenUid)
                .collection("complaints")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Error listening to citizen complaints: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val items = snapshot.documents.mapNotNull { doc ->
                            try {
                                val cat = IncidentCategory.valueOf(doc.getString("category") ?: IncidentCategory.OTHER.name)
                                val stat = ComplaintStatus.valueOf(doc.getString("status") ?: ComplaintStatus.SUBMITTED.name)
                                Complaint(
                                    referenceId = doc.getString("referenceId") ?: doc.id,
                                    category = cat,
                                    title = doc.getString("title") ?: "",
                                    description = doc.getString("description") ?: "",
                                    hasPhoto = doc.getBoolean("hasPhoto") ?: false,
                                    photoName = doc.getString("photoName") ?: "",
                                    latitude = doc.getDouble("latitude") ?: 0.0,
                                    longitude = doc.getDouble("longitude") ?: 0.0,
                                    address = doc.getString("address") ?: "",
                                    submittedDate = doc.getString("submittedDate") ?: "",
                                    status = stat,
                                    timeline = ComplaintConverters.jsonToTimeline(doc.getString("timelineJson") ?: ""),
                                    assignedOfficer = doc.getString("assignedOfficer") ?: "",
                                    aiAnalysis = ComplaintConverters.jsonToAiAnalysis(doc.getString("aiAnalysisJson") ?: "", cat),
                                    userId = doc.getString("userId") ?: citizenUid
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(items)
                    }
                }
            awaitClose { listener.remove() }
        }
    }

    override fun observeActiveIncidents(): Flow<List<Incident>> {
        val db = firestore ?: return emptyFlow()
        return callbackFlow {
            val listener = db.collection("incidents")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Error listening to incidents: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val list = snapshot.documents.mapNotNull { doc ->
                            try {
                                Incident(
                                    id = doc.id,
                                    title = doc.getString("title") ?: "",
                                    category = IncidentCategory.valueOf(doc.getString("category") ?: "OTHER"),
                                    severity = IncidentSeverity.valueOf(doc.getString("severity") ?: "MEDIUM"),
                                    status = doc.getString("status") ?: "Reported",
                                    locationName = doc.getString("locationName") ?: "",
                                    latitude = doc.getDouble("latitude") ?: 0.0,
                                    longitude = doc.getDouble("longitude") ?: 0.0,
                                    description = doc.getString("description") ?: "",
                                    reportedTime = doc.getString("reportedTime") ?: "",
                                    department = doc.getString("department") ?: "",
                                    verifiedCount = doc.getLong("verifiedCount")?.toInt() ?: 0,
                                    isSimulated = doc.getBoolean("isSimulated") ?: false
                                )
                            } catch (e: Exception) {
                                null
                            }
                        }
                        trySend(list)
                    }
                }
            awaitClose { listener.remove() }
        }
    }

    override suspend fun upvoteIncidentVerification(incidentId: String): Result<Int> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        return try {
            val docRef = db.collection("incidents").document(incidentId)
            var newCount = 0
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val current = snapshot.getLong("verifiedCount") ?: 0
                newCount = (current + 1).toInt()
                transaction.update(docRef, "verifiedCount", newCount)
            }.await()
            Result.success(newCount)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }

    override suspend fun updateComplaintTimeline(referenceId: String, status: String, note: String): Result<Unit> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firestore unavailable"))
        return try {
            db.collection("complaints").document(referenceId).update(
                mapOf(
                    "status" to status,
                    "lastNote" to note,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Throwable) {
            Result.failure(e)
        }
    }
}
