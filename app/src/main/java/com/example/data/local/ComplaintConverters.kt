package com.example.data.local

import com.example.model.*
import org.json.JSONArray
import org.json.JSONObject

object ComplaintConverters {

    fun timelineToJson(timeline: List<TimelineStep>): String {
        val array = JSONArray()
        for (step in timeline) {
            val obj = JSONObject().apply {
                put("status", step.status.name)
                put("title", step.title)
                put("timestamp", step.timestamp)
                put("note", step.note)
                put("isCompleted", step.isCompleted)
                put("isCurrent", step.isCurrent)
            }
            array.put(obj)
        }
        return array.toString()
    }

    fun jsonToTimeline(jsonStr: String): List<TimelineStep> {
        if (jsonStr.isBlank()) return emptyList()
        val list = mutableListOf<TimelineStep>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val statusStr = obj.optString("status", ComplaintStatus.SUBMITTED.name)
                val status = try {
                    ComplaintStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    ComplaintStatus.SUBMITTED
                }
                list.add(
                    TimelineStep(
                        status = status,
                        title = obj.optString("title", ""),
                        timestamp = obj.optString("timestamp", ""),
                        note = obj.optString("note", ""),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        isCurrent = obj.optBoolean("isCurrent", false)
                    )
                )
            }
        } catch (e: Exception) {
            // Safely return empty list on malformed input
        }
        return list
    }

    fun aiAnalysisToJson(ai: AiAnalysisResult): String {
        return JSONObject().apply {
            put("suggestedCategory", ai.suggestedCategory.name)
            put("incidentSummary", ai.incidentSummary)
            put("suggestedPriority", ai.suggestedPriority.name)
            put("recommendedDepartment", ai.recommendedDepartment)
            put("reasoning", ai.reasoning)
            put("confidenceScore", ai.confidenceScore)
            put("suggestedPrecaution", ai.suggestedPrecaution)
            put("isReviewedByCitizen", ai.isReviewedByCitizen)
        }.toString()
    }

    fun jsonToAiAnalysis(jsonStr: String, fallbackCategory: IncidentCategory): AiAnalysisResult {
        if (jsonStr.isBlank()) {
            return AiAnalysisResult(
                suggestedCategory = fallbackCategory,
                incidentSummary = "Civic complaint logged.",
                suggestedPriority = IncidentSeverity.MEDIUM,
                recommendedDepartment = "Municipal Services",
                reasoning = "Recorded in municipal repository.",
                confidenceScore = 90,
                suggestedPrecaution = "Proceed with caution."
            )
        }
        try {
            val obj = JSONObject(jsonStr)
            val cat = try {
                IncidentCategory.valueOf(obj.optString("suggestedCategory", fallbackCategory.name))
            } catch (e: Exception) {
                fallbackCategory
            }
            val priority = try {
                IncidentSeverity.valueOf(obj.optString("suggestedPriority", IncidentSeverity.MEDIUM.name))
            } catch (e: Exception) {
                IncidentSeverity.MEDIUM
            }
            return AiAnalysisResult(
                suggestedCategory = cat,
                incidentSummary = obj.optString("incidentSummary", "Civic complaint logged."),
                suggestedPriority = priority,
                recommendedDepartment = obj.optString("recommendedDepartment", "Municipal Services"),
                reasoning = obj.optString("reasoning", "Recorded in municipal repository."),
                confidenceScore = obj.optInt("confidenceScore", 90),
                suggestedPrecaution = obj.optString("suggestedPrecaution", "Proceed with caution."),
                isReviewedByCitizen = obj.optBoolean("isReviewedByCitizen", false)
            )
        } catch (e: Exception) {
            return AiAnalysisResult(
                suggestedCategory = fallbackCategory,
                incidentSummary = "Civic complaint logged.",
                suggestedPriority = IncidentSeverity.MEDIUM,
                recommendedDepartment = "Municipal Services",
                reasoning = "Recorded in municipal repository.",
                confidenceScore = 90,
                suggestedPrecaution = "Proceed with caution."
            )
        }
    }
}

fun ComplaintEntity.toDomain(): Complaint {
    val cat = try {
        IncidentCategory.valueOf(category)
    } catch (e: Exception) {
        IncidentCategory.fromDisplayName(category)
    }
    val stat = try {
        ComplaintStatus.valueOf(status)
    } catch (e: Exception) {
        ComplaintStatus.SUBMITTED
    }
    return Complaint(
        referenceId = referenceId,
        category = cat,
        title = title,
        description = description,
        hasPhoto = hasPhoto,
        photoName = photoName,
        latitude = latitude,
        longitude = longitude,
        address = address,
        submittedDate = submittedDate,
        status = stat,
        timeline = ComplaintConverters.jsonToTimeline(timelineJson),
        assignedOfficer = assignedOfficer,
        aiAnalysis = ComplaintConverters.jsonToAiAnalysis(aiAnalysisJson, cat),
        userId = userId
    )
}

fun Complaint.toEntity(overrideTimestamp: Long? = null): ComplaintEntity {
    return ComplaintEntity(
        referenceId = referenceId,
        userId = userId,
        category = category.name,
        title = title,
        description = description,
        hasPhoto = hasPhoto,
        photoName = photoName,
        latitude = latitude,
        longitude = longitude,
        address = address,
        submittedDate = submittedDate,
        status = status.name,
        timelineJson = ComplaintConverters.timelineToJson(timeline),
        assignedOfficer = assignedOfficer,
        aiAnalysisJson = ComplaintConverters.aiAnalysisToJson(aiAnalysis),
        createdAtTimestamp = overrideTimestamp ?: System.currentTimeMillis()
    )
}
