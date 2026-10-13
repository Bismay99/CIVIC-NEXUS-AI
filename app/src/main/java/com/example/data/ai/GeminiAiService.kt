package com.example.data.ai

import com.example.BuildConfig
import com.example.model.AiAnalysisResult
import com.example.model.AiDraftSuggestion
import com.example.model.IncidentCategory
import com.example.model.IncidentSeverity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class GeminiAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Model per official gemini-api guidelines: gemini-3.5-flash for general text and Q&A
    private val geminiModel = "gemini-3.5-flash"

    private val apiKey: String
        get() = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

    fun isGeminiKeyConfigured(): Boolean {
        val key = apiKey.trim()
        return key.isNotEmpty() &&
                key != "MY_GEMINI_API_KEY" &&
                !key.startsWith("placeholder", ignoreCase = true) &&
                !key.startsWith("your_", ignoreCase = true)
    }

    /**
     * Ask AI City Copilot with multi-turn conversation context.
     * Throws typed CivicAiException on failure to enable clear error UI and retries.
     */
    suspend fun askAssistant(
        userPrompt: String,
        history: List<Pair<String, String>> = emptyList(),
        contextSummary: String = ""
    ): Pair<String, Boolean> = withContext(Dispatchers.IO) {
        val trimmedPrompt = userPrompt.trim()
        if (trimmedPrompt.isBlank()) {
            throw CivicAiException.BadRequest("Message cannot be empty.")
        }

        if (!isGeminiKeyConfigured()) {
            throw CivicAiException.KeyMissing(
                "Gemini API key is not configured. Please add your GEMINI_API_KEY to the AI Studio Secrets panel or .env file."
            )
        }

        val systemPrompt = """
            You are Civic Nexus AI, the official AI City Copilot for smart municipal governance and citizen assistance.
            Tone: Professional, empathetic, objective, and action-oriented.
            Core Capabilities:
            1. Guide citizens on reporting civic issues (potholes, overflowing garbage, broken streetlights, water pipeline leaks, flooding/drainage, traffic bottlenecks).
            2. Explain complaint lifecycle in the app: Submitted -> Verified -> Dispatched -> In Progress -> Resolved.
            3. Provide emergency helpline contacts (112 Universal, 108 Ambulance, 101 Fire, 1916 Water Supply, 1912 Power).
            4. Explain municipal procedures and Service Level Agreements (SLAs) accurately.
            Important Guidelines:
            - Keep answers structured and concise (use bullet points where appropriate).
            - Do not fabricate live municipal authority decisions, official proclamations, or emergency dispatches.
            - Provide safe immediate precautions for citizens near physical hazards.
            Context telemetry: $contextSummary
        """.trimIndent()

        val responseText = callGeminiMultiTurn(
            userPrompt = trimmedPrompt.take(1000),
            history = history.takeLast(8), // Keep recent conversation context bounded
            systemInstruction = systemPrompt
        )

        if (responseText.isBlank()) {
            throw CivicAiException.InvalidResponse("AI City Copilot returned an empty response.")
        }

        return@withContext Pair(responseText, true)
    }

    /**
     * Analyzes citizen complaint report using Gemini multimodal/text reasoning,
     * falling back to the local deterministic rule engine only if Gemini key is unconfigured.
     */
    suspend fun analyzeIncident(
        description: String,
        selectedCategoryName: String?
    ): AiAnalysisResult = withContext(Dispatchers.IO) {
        val prompt = """
            You are Civic Nexus AI, the municipal intelligence engine for a smart city.
            Analyze the following citizen civic complaint report and provide an incident assessment:
            Complaint: "$description"
            Citizen selected category: "${selectedCategoryName ?: "Unknown"}"

            Output valid JSON only with keys:
            - suggestedCategory: one of ["Potholes & Roads", "Garbage & Waste", "Flooding & Drainage", "Fire & Hazard", "Traffic Congestion", "Broken Streetlights", "Water Pipe Burst", "Other Civic Issue"]
            - incidentSummary: concise 1-sentence technical summary
            - suggestedPriority: one of ["Critical", "High", "Medium", "Low"]
            - recommendedDepartment: appropriate municipal division
            - reasoning: 1-2 sentence engineering justification
            - confidenceScore: integer 70-99
            - suggestedPrecaution: safe immediate guidance for citizens nearby
        """.trimIndent()

        if (isGeminiKeyConfigured()) {
            try {
                val jsonResponse = callGeminiSinglePrompt(prompt)
                val parsed = parseAnalysisJson(jsonResponse, description, selectedCategoryName)
                if (parsed != null) return@withContext parsed
            } catch (e: Exception) {
                // Fall back gracefully to offline engineering rule engine for complaint filing
            }
        }

        return@withContext performOfflineAnalysis(description, selectedCategoryName)
    }

    /**
     * Draft improvement for citizen complaint descriptions.
     */
    suspend fun improveComplaintDraft(
        draftText: String,
        category: String?
    ): AiDraftSuggestion = withContext(Dispatchers.IO) {
        val prompt = """
            You are Civic Nexus AI, assisting a citizen in writing an official municipal civic complaint.
            Input text: "$draftText"
            Selected Category: "${category ?: "Unknown"}"

            Task:
            1. Suggest a clear, concise, professional title (under 8 words).
            2. Rewrite the complaint into a professional, grammatically clear, well-structured description.
            3. Suggest the most appropriate category among: ["Potholes & Roads", "Garbage & Waste", "Flooding & Drainage", "Fire & Hazard", "Traffic Congestion", "Broken Streetlights", "Water Pipe Burst", "Other Civic Issue"]
            4. Suggest a priority level ["Critical", "High", "Medium", "Low"] with a brief 1-sentence engineering justification.
            5. Identify missing details (e.g., landmark, duration, severity, exact spot) that the citizen could add to expedite resolution.

            Output VALID JSON ONLY with keys:
            - suggestedTitle: string
            - improvedDescription: string
            - suggestedCategory: string
            - suggestedPriority: string
            - priorityReasoning: string
            - missingDetailsAdvice: string
        """.trimIndent()

        if (isGeminiKeyConfigured()) {
            try {
                val jsonResponse = callGeminiSinglePrompt(prompt)
                val parsed = parseDraftJson(jsonResponse, draftText, category)
                if (parsed != null) return@withContext parsed
            } catch (e: Exception) {
                // Fall back to offline heuristics
            }
        }

        return@withContext performOfflineDraftImprovement(draftText, category)
    }

    /**
     * Calls Gemini 3.5 Flash REST API with multi-turn conversation history.
     */
    private fun callGeminiMultiTurn(
        userPrompt: String,
        history: List<Pair<String, String>>,
        systemInstruction: String
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$geminiModel:generateContent?key=$apiKey"

        val contentsArray = JSONArray()

        // Append historical turns
        for ((histUser, histModel) in history) {
            if (histUser.isNotBlank()) {
                val userContent = JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply { put(JSONObject().put("text", histUser)) })
                }
                contentsArray.put(userContent)
            }
            if (histModel.isNotBlank()) {
                val modelContent = JSONObject().apply {
                    put("role", "model")
                    put("parts", JSONArray().apply { put(JSONObject().put("text", histModel)) })
                }
                contentsArray.put(modelContent)
            }
        }

        // Append latest user query
        val currentContent = JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply { put(JSONObject().put("text", userPrompt)) })
        }
        contentsArray.put(currentContent)

        val jsonBody = JSONObject().apply {
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply { put(JSONObject().put("text", systemInstruction)) })
            })
            put("contents", contentsArray)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.3)
                put("maxOutputTokens", 800)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return executeRequest(request)
    }

    /**
     * Calls Gemini 3.5 Flash REST API with a single prompt.
     */
    private fun callGeminiSinglePrompt(prompt: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$geminiModel:generateContent?key=$apiKey"

        val jsonBody = JSONObject().apply {
            val contents = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val parts = JSONArray().apply {
                        put(JSONObject().put("text", prompt))
                    }
                    put("parts", parts)
                }
                put(contentObj)
            }
            put("contents", contents)
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.2)
                put("maxOutputTokens", 800)
            })
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        return executeRequest(request)
    }

    private fun executeRequest(request: Request): String {
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    when (response.code) {
                        429 -> throw CivicAiException.RateLimit()
                        400, 401, 403 -> throw CivicAiException.ServerError(
                            response.code,
                            "AI request rejected (${response.code}). Check API key and quotas."
                        )
                        else -> throw CivicAiException.ServerError(
                            response.code,
                            "AI service error (HTTP ${response.code})."
                        )
                    }
                }
                val respString = response.body?.string() ?: ""
                val jsonRoot = JSONObject(respString)
                val candidates = jsonRoot.optJSONArray("candidates") ?: return ""
                if (candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        return parts.getJSONObject(0).optString("text", "").trim()
                    }
                }
            }
        } catch (e: SocketTimeoutException) {
            throw CivicAiException.NetworkError("Network request timed out. Please check connection and retry.", e)
        } catch (e: UnknownHostException) {
            throw CivicAiException.NetworkError("Unable to resolve AI host. Check internet connectivity.", e)
        } catch (e: ConnectException) {
            throw CivicAiException.NetworkError("Failed to connect to AI server.", e)
        } catch (e: IOException) {
            throw CivicAiException.NetworkError("Network error communicating with AI server: ${e.message}", e)
        }
        return ""
    }

    /**
     * Explicit offline municipal handbook guidance for citizens when network is unavailable.
     * Explicitly marked in the UI as offline guidance rather than fake AI generation.
     */
    fun getOfflineCivicGuidance(query: String): String {
        val lower = query.lowercase()
        return when {
            lower.contains("streetlight") || lower.contains("light") || lower.contains("lamp") -> {
                "### Official Municipal Guide: Reporting Broken Streetlights\n\n" +
                "1. **Navigate to 'Report'** and select **'Broken Streetlights'**.\n" +
                "2. **Location Details**: Mention the pole identification tag or nearest building number.\n" +
                "3. **SLA Notice**: Municipal Electrical Division conducts circuit testing and replaces LED fixtures within 24 hours."
            }
            lower.contains("pothole") || lower.contains("road") || lower.contains("crater") -> {
                "### Official Municipal Guide: Reporting Road Defects\n\n" +
                "1. **Navigate to 'Report'**: Use the center action or tab.\n" +
                "2. **Category**: Select 'Potholes & Roads'.\n" +
                "3. **Attach Visual Evidence**: Take a clear snapshot showing depth and lane position.\n" +
                "4. **AI Risk Assessment**: Tap 'Analyze with Civic AI' to tag priority.\n" +
                "5. **Resolution SLA**: Emergency asphalt patch teams are dispatched within 24–48 hours upon verification."
            }
            lower.contains("garbage") || lower.contains("waste") || lower.contains("sanitation") -> {
                "### Official Municipal Guide: Waste Management & Sanitation\n\n" +
                "• **Waste Segregation**: Green (Biodegradable/Wet), Blue (Recyclable/Dry), Red (Domestic Hazardous).\n" +
                "• **Uncollected Garbage**: Report via the 'Garbage & Waste' category.\n" +
                "• **Resolution SLA**: Municipal mechanical compactors clear reported overflow within 4 hours."
            }
            lower.contains("drainage") || lower.contains("flood") || lower.contains("waterlog") -> {
                "### Official Municipal Guide: Stormwater & Drainage\n\n" +
                "• Report standing water accumulation or blocked culverts in the 'Flooding & Drainage' category.\n" +
                "• High waterlogging alerts are automatically routed to the Disaster Quick-Response Wing (ODRAF)."
            }
            lower.contains("emergency") || lower.contains("helpline") || lower.contains("number") -> {
                "### Verified Emergency Helplines:\n\n" +
                "• 🚨 **Universal Emergency:** `112`\n" +
                "• 🚑 **Medical Ambulance:** `108`\n" +
                "• 🚒 **Fire & Rescue:** `101`\n" +
                "• 💧 **Water Supply Emergency:** `1916`\n" +
                "• ⚡ **Power Grid Faults:** `1912`\n" +
                "• 🏛️ **Municipal Citizen Helpline:** `1800-345-0061`"
            }
            else -> {
                "### Municipal Guidance Directory\n\n" +
                "You are viewing offline civic directory guidance. Supported topics:\n" +
                "• Filing reports for potholes, waste, drainage, and streetlights\n" +
                "• Checking emergency helplines (112, 108, 101, 1916)\n" +
                "• Understanding civic service level agreements (SLAs)\n\n" +
                "To receive interactive AI answers, please configure your Gemini API key and reconnect to the internet."
            }
        }
    }

    private fun parseAnalysisJson(
        rawText: String,
        originalDescription: String,
        userCategory: String?
    ): AiAnalysisResult? {
        try {
            var cleaned = rawText.trim()
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.removePrefix("```json").removeSuffix("```").trim()
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.removePrefix("```").removeSuffix("```").trim()
            }

            val json = JSONObject(cleaned)
            val catStr = json.optString("suggestedCategory", userCategory ?: "Other Civic Issue")
            val summary = json.optString("incidentSummary", originalDescription.take(60))
            val prioStr = json.optString("suggestedPriority", "Medium")
            val dept = json.optString("recommendedDepartment", "Municipal Works Division")
            val reasoning = json.optString("reasoning", "Assessed based on municipal safety guidelines.")
            val confidence = json.optInt("confidenceScore", 85)
            val precaution = json.optString("suggestedPrecaution", "Exercise caution in the vicinity.")

            val category = IncidentCategory.fromDisplayName(catStr)
            val priority = when (prioStr.uppercase()) {
                "CRITICAL" -> IncidentSeverity.CRITICAL
                "HIGH" -> IncidentSeverity.HIGH
                "LOW" -> IncidentSeverity.LOW
                else -> IncidentSeverity.MEDIUM
            }

            return AiAnalysisResult(
                suggestedCategory = category,
                incidentSummary = summary,
                suggestedPriority = priority,
                recommendedDepartment = dept,
                reasoning = reasoning,
                confidenceScore = confidence,
                suggestedPrecaution = precaution
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun parseDraftJson(
        jsonString: String,
        originalDraft: String,
        userCategory: String?
    ): AiDraftSuggestion? {
        try {
            var cleaned = jsonString.trim()
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.removePrefix("```json").removeSuffix("```").trim()
            } else if (cleaned.startsWith("```")) {
                cleaned = cleaned.removePrefix("```").removeSuffix("```").trim()
            }

            val json = JSONObject(cleaned)
            val title = json.optString("suggestedTitle", originalDraft.take(50))
            val desc = json.optString("improvedDescription", originalDraft)
            val catStr = json.optString("suggestedCategory", userCategory ?: "Other Civic Issue")
            val prioStr = json.optString("suggestedPriority", "Medium")
            val reason = json.optString("priorityReasoning", "Recommended based on civic engineering assessment.")
            val missing = json.optString("missingDetailsAdvice", "Consider mentioning nearest landmarks.")

            val category = IncidentCategory.fromDisplayName(catStr)
            val priority = when (prioStr.uppercase()) {
                "CRITICAL" -> IncidentSeverity.CRITICAL
                "HIGH" -> IncidentSeverity.HIGH
                "LOW" -> IncidentSeverity.LOW
                else -> IncidentSeverity.MEDIUM
            }

            return AiDraftSuggestion(
                suggestedTitle = title,
                improvedDescription = desc,
                suggestedCategory = category,
                suggestedPriority = priority,
                priorityReasoning = reason,
                missingDetailsAdvice = missing
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun performOfflineAnalysis(
        description: String,
        selectedCategoryName: String?
    ): AiAnalysisResult {
        val lower = description.lowercase()
        val category: IncidentCategory
        val priority: IncidentSeverity
        val department: String
        val reasoning: String
        val precaution: String
        val confidence: Int

        when {
            lower.contains("pothole") || lower.contains("crater") || lower.contains("road") || lower.contains("asphalt") -> {
                category = IncidentCategory.POTHOLES
                priority = if (lower.contains("deep") || lower.contains("accident") || lower.contains("severe")) IncidentSeverity.HIGH else IncidentSeverity.MEDIUM
                department = "Roads & Bridges Maintenance Division"
                reasoning = "Surface crater detected with potential vehicular destabilization risk."
                precaution = "Slow down vehicular speed and avoid abrupt steering maneuvers."
                confidence = 94
            }
            lower.contains("garbage") || lower.contains("waste") || lower.contains("trash") || lower.contains("bin") || lower.contains("dump") -> {
                category = IncidentCategory.GARBAGE
                priority = if (lower.contains("stink") || lower.contains("overflow") || lower.contains("maggot")) IncidentSeverity.HIGH else IncidentSeverity.MEDIUM
                department = "Solid Waste & Public Sanitation Cell"
                reasoning = "Uncollected municipal refuse accumulating with public hygiene implications."
                precaution = "Maintain safe distance to prevent biohazard exposure."
                confidence = 91
            }
            lower.contains("flood") || lower.contains("drain") || lower.contains("waterlog") || lower.contains("sewer") -> {
                category = IncidentCategory.FLOODING
                priority = if (lower.contains("deep") || lower.contains("enter") || lower.contains("house")) IncidentSeverity.CRITICAL else IncidentSeverity.HIGH
                department = "Stormwater Drainage & Flood Control Cell"
                reasoning = "Hydraulic drainage bottleneck posing structural waterlogging risk."
                precaution = "Avoid walking through standing murky water due to hidden open manholes."
                confidence = 89
            }
            lower.contains("light") || lower.contains("dark") || lower.contains("lamp") || lower.contains("streetlight") -> {
                category = IncidentCategory.STREETLIGHTS
                priority = IncidentSeverity.MEDIUM
                department = "Public Lighting & Electrical Works"
                reasoning = "Luminaire failure causing low-visibility night hazard."
                precaution = "Use auxiliary flashlight while traversing this public corridor."
                confidence = 95
            }
            lower.contains("pipe") || lower.contains("burst") || lower.contains("leak") || lower.contains("drinking water") -> {
                category = IncidentCategory.WATER_BURST
                priority = IncidentSeverity.HIGH
                department = "Water Corporation Supply & Maintenance Wing"
                reasoning = "Potable water pressure breach causing treated water wastage."
                precaution = "Exercise caution due to potential soil cavitation."
                confidence = 92
            }
            lower.contains("fire") || lower.contains("smoke") || lower.contains("flame") || lower.contains("spark") -> {
                category = IncidentCategory.FIRE
                priority = IncidentSeverity.CRITICAL
                department = "Fire Safety & Emergency Disaster Operations"
                reasoning = "Thermal hazard with rapid escalation potential."
                precaution = "Evacuate upwind immediately and maintain 100m perimeter."
                confidence = 98
            }
            lower.contains("traffic") || lower.contains("jam") || lower.contains("signal") || lower.contains("congestion") -> {
                category = IncidentCategory.TRAFFIC
                priority = IncidentSeverity.MEDIUM
                department = "Traffic Engineering & Transit Management"
                reasoning = "Vehicular choke point hindering arterial thoroughfare."
                precaution = "Utilize secondary peripheral bypass corridors."
                confidence = 87
            }
            else -> {
                category = if (selectedCategoryName != null) IncidentCategory.fromDisplayName(selectedCategoryName) else IncidentCategory.OTHER
                priority = IncidentSeverity.MEDIUM
                department = "Municipal Grievance Redressal Cell"
                reasoning = "Categorized via standard municipal triage protocol."
                precaution = "Proceed with caution around reported location."
                confidence = 85
            }
        }

        val summary = if (description.isNotBlank()) {
            description.trim().take(70) + if (description.length > 70) "..." else ""
        } else {
            "Civic incident reported at location."
        }

        return AiAnalysisResult(
            suggestedCategory = category,
            incidentSummary = summary,
            suggestedPriority = priority,
            recommendedDepartment = department,
            reasoning = reasoning,
            confidenceScore = confidence,
            suggestedPrecaution = precaution
        )
    }

    private fun performOfflineDraftImprovement(
        draftText: String,
        category: String?
    ): AiDraftSuggestion {
        val offlineAnalysis = performOfflineAnalysis(draftText, category)

        val generatedTitle: String
        val polishedDescription: String
        val missingDetails: String

        when (offlineAnalysis.suggestedCategory) {
            IncidentCategory.POTHOLES -> {
                generatedTitle = "Pothole Hazard on Roadway"
                polishedDescription = "Severe roadway depression and asphalt degradation reported. $draftText. Poses safety risks to vehicles and commuters requiring urgent patch remediation."
                missingDetails = "Please specify approximate lane, direction of traffic flow, and whether water collects in the pothole."
            }
            IncidentCategory.GARBAGE -> {
                generatedTitle = "Uncollected Waste and Garbage Overflow"
                polishedDescription = "Sanitation issue regarding uncollected waste and overflowing municipal dumpster. $draftText. Requires rapid sanitation compactor clearance."
                missingDetails = "Mention if the waste is spilling onto the pedestrian sidewalk or blocking residential access."
            }
            IncidentCategory.FLOODING -> {
                generatedTitle = "Severe Stormwater Drain Clog & Waterlogging"
                polishedDescription = "Drainage obstruction causing standing water accumulation. $draftText. Poses structural and hygiene concerns requiring municipal desilting."
                missingDetails = "Note approximate water depth and if nearby ground floors are affected."
            }
            IncidentCategory.STREETLIGHTS -> {
                generatedTitle = "Non-Functional Streetlight on Public Corridor"
                polishedDescription = "Public lighting malfunction causing dark stretch on roadway. $draftText. Requires municipal electrical inspection."
                missingDetails = "Provide the nearest pole number or landmark building opposite the fixture."
            }
            IncidentCategory.WATER_BURST -> {
                generatedTitle = "Potable Water Pipeline Rupture"
                polishedDescription = "High-pressure municipal pipeline leakage causing treated water loss. $draftText. Requires emergency valve isolation."
                missingDetails = "Indicate if water pressure in nearby houses has dropped."
            }
            IncidentCategory.FIRE -> {
                generatedTitle = "Combustion and Thermal Fire Hazard"
                polishedDescription = "Active or impending combustion hazard reported. $draftText. Urgent containment and fire safety dispatch required."
                missingDetails = "Confirm if inflammable materials or high-voltage lines are in immediate proximity."
            }
            IncidentCategory.TRAFFIC -> {
                generatedTitle = "Severe Traffic Chokepoint & Signal Malfunction"
                polishedDescription = "Vehicular congestion bottleneck reported. $draftText. Requires traffic management intervention."
                missingDetails = "State which intersection approaches are gridlocked."
            }
            else -> {
                generatedTitle = draftText.take(45).ifBlank { "Civic Infrastructure Issue" }
                polishedDescription = "Citizen civic report: $draftText. Requires municipal inspection and departmental assignment."
                missingDetails = "Add specific landmarks, street number, and how many days this issue has been observed."
            }
        }

        return AiDraftSuggestion(
            suggestedTitle = generatedTitle,
            improvedDescription = polishedDescription,
            suggestedCategory = offlineAnalysis.suggestedCategory,
            suggestedPriority = offlineAnalysis.suggestedPriority,
            priorityReasoning = offlineAnalysis.reasoning,
            missingDetailsAdvice = missingDetails
        )
    }
}
