package com.example.model

enum class IncidentCategory(val displayName: String, val iconName: String) {
    POTHOLES("Potholes & Roads", "pothole"),
    GARBAGE("Garbage & Waste", "delete"),
    FLOODING("Flooding & Drainage", "water_damage"),
    FIRE("Fire & Hazard", "local_fire_department"),
    TRAFFIC("Traffic Congestion", "traffic"),
    STREETLIGHTS("Broken Streetlights", "lightbulb"),
    WATER_BURST("Water Pipe Burst", "plumbing"),
    OTHER("Other Civic Issue", "report_problem");

    companion object {
        fun fromDisplayName(name: String): IncidentCategory {
            return entries.find { it.displayName.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}

enum class IncidentSeverity(val label: String) {
    CRITICAL("Critical"),
    HIGH("High"),
    MEDIUM("Medium"),
    LOW("Low")
}

enum class ComplaintStatus(val displayName: String, val stepIndex: Int) {
    SUBMITTED("Submitted", 0),
    UNDER_REVIEW("Under Review", 1),
    ASSIGNED("Assigned", 2),
    IN_PROGRESS("In Progress", 3),
    RESOLVED("Resolved", 4),
    REOPENED("Reopened", 1)
}

data class AiDraftSuggestion(
    val suggestedTitle: String,
    val improvedDescription: String,
    val suggestedCategory: IncidentCategory,
    val suggestedPriority: IncidentSeverity,
    val priorityReasoning: String,
    val missingDetailsAdvice: String
)

data class TimelineStep(
    val status: ComplaintStatus,
    val title: String,
    val timestamp: String,
    val note: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean
)

data class AiAnalysisResult(
    val suggestedCategory: IncidentCategory,
    val incidentSummary: String,
    val suggestedPriority: IncidentSeverity,
    val recommendedDepartment: String,
    val reasoning: String,
    val confidenceScore: Int,
    val suggestedPrecaution: String,
    val isReviewedByCitizen: Boolean = false
)

data class Complaint(
    val referenceId: String,
    val category: IncidentCategory,
    val title: String,
    val description: String,
    val hasPhoto: Boolean = true,
    val photoName: String = "civic_incident_capture.jpg",
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val submittedDate: String,
    val status: ComplaintStatus,
    val timeline: List<TimelineStep>,
    val assignedOfficer: String,
    val aiAnalysis: AiAnalysisResult,
    val userId: String = ""
)

data class Incident(
    val id: String,
    val title: String,
    val category: IncidentCategory,
    val severity: IncidentSeverity,
    val status: String,
    val locationName: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val reportedTime: String,
    val department: String,
    val verifiedCount: Int,
    val isSimulated: Boolean = true
)

data class EmergencyFacility(
    val id: String,
    val name: String,
    val type: String, // "Hospital", "Fire Station", "Police Hub", "Disaster Unit"
    val contactNumber: String,
    val address: String,
    val distanceKm: Double,
    val latitude: Double,
    val longitude: Double,
    val isOperational: Boolean = true
)

data class EmergencyAlert(
    val id: String,
    val title: String,
    val severity: IncidentSeverity,
    val area: String,
    val details: String,
    val timestamp: String,
    val issuingAuthority: String,
    val isSimulated: Boolean = true
)

enum class CoordinationAgency(val agencyName: String, val role: String) {
    TRAFFIC_AUTHORITY("City Traffic Control", "Perimeter cordon & green corridor routing"),
    ODRAF_DISASTER("ODRAF Disaster Team", "Rapid water extraction & hazard neutralization"),
    FIRE_RESCUE("Fire & Rescue Unit 04", "Structural integrity check & safety perimeter"),
    AMBULANCE_CORPS("108 Trauma Ambulance", "On-scene medical standby & triage prep"),
    MUNICIPAL_WORKS("BMC Civil Engineering", "Emergency pipeline isolation & pump activation")
}

data class CoordinationStep(
    val agency: CoordinationAgency,
    val action: String,
    val status: String, // "Queued", "Dispatched", "On Scene", "Active", "Resolved"
    val progress: Float,
    val timestamp: String
)

data class EmergencySimulation(
    val incidentTitle: String,
    val location: String,
    val severity: IncidentSeverity,
    val steps: List<CoordinationStep>,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false
)

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: String,
    val isAiThinking: Boolean = false,
    val isError: Boolean = false,
    val retryPrompt: String? = null,
    val suggestedActions: List<String> = emptyList(),
    val isGeminiPowered: Boolean = false,
    val isOfflineGuidance: Boolean = false
)

data class CityPulseStats(
    val pulseScore: Int, // e.g. 94/100
    val activeIncidents: Int,
    val resolvedToday: Int,
    val avgResponseMinutes: Int,
    val sensorHealthPercent: Int,
    val selfHealingActionsCount: Int
)

data class AppNotification(
    val id: String,
    val title: String,
    val message: String,
    val category: String, // "Emergency Broadcast", "Complaint Update", "Department Notice"
    val timestamp: String,
    val isRead: Boolean = false,
    val isSimulated: Boolean = true
)

data class CitizenProfile(
    val uid: String = "",
    val name: String = "Subhransu Mohapatra",
    val citizenId: String = "CN-OD-2026-7841",
    val ward: String = "Ward 14 (Tech Hub / Patia)",
    val trustScore: Int = 98,
    val reportsCount: Int = 12,
    val resolvedCount: Int = 10,
    val impactPoints: Int = 840,
    val email: String = "citizen.reporter@civicnexus.gov.in",
    val phone: String = "+91 98765 43210",
    val photoUrl: String? = null,
    val role: String = "citizen",
    val createdAt: String = "",
    val updatedAt: String = ""
)

enum class AuthScreenMode {
    LOGIN,
    REGISTER
}

data class CivicAuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String? = null,
    val role: String = "citizen"
)

data class AuthUiState(
    val isAuthenticated: Boolean = false,
    val authScreenMode: AuthScreenMode = AuthScreenMode.LOGIN,
    val isFirebaseConfigured: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUser: CivicAuthUser? = null
)

enum class MainNavigationTab(val label: String) {
    HOME("Home"),
    EXPLORE("Explore"),
    COMPLAINTS("My Complaints"),
    ASSISTANT("AI Copilot"),
    PROFILE("Profile"),
    NOTIFICATIONS("Notifications");

    companion object {
        val MAP = EXPLORE
        val REPORT = COMPLAINTS
        val COPILOT = ASSISTANT
    }
}

enum class AppThemeMode(val displayName: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

