package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.data.ai.GeminiAiService
import com.example.data.auth.FirebaseAuthService
import com.example.data.auth.GoogleSignInCancelledException
import com.example.data.backend.CivicFirestoreService
import com.example.data.backend.CivicFirestoreServiceImpl
import com.example.data.local.CivicDatabase
import com.example.data.local.ComplaintDao
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.local.ComplaintConverters
import com.example.data.local.ComplaintDraftEntity
import com.example.model.*
import com.example.util.AppLanguage
import com.example.util.LocalizationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class CivicRepository(
    context: Context,
    private val geminiService: GeminiAiService = GeminiAiService(),
    database: CivicDatabase = CivicDatabase.getInstance(context),
    private val scope: CoroutineScope = CoroutineScope(kotlinx.coroutines.SupervisorJob() + Dispatchers.Default),
    private val firestoreService: CivicFirestoreService = CivicFirestoreServiceImpl(context.applicationContext)
) {
    val authService = FirebaseAuthService(context.applicationContext)
    val complaintDao: ComplaintDao = database.complaintDao()
    private val localizationManager = LocalizationManager(context.applicationContext)
    val currentLanguage: StateFlow<AppLanguage> = localizationManager.currentLanguage

    fun setLanguage(language: AppLanguage) {
        localizationManager.setLanguage(language)
    }

    // Voting restriction tracking
    private val votePrefs = context.getSharedPreferences("civic_incident_votes", Context.MODE_PRIVATE)
    private val _upvotedIncidentIds = MutableStateFlow(
        votePrefs.getStringSet("voted_ids", emptySet()) ?: emptySet()
    )
    val upvotedIncidentIds: StateFlow<Set<String>> = _upvotedIncidentIds.asStateFlow()

    fun hasUpvoted(incidentId: String): Boolean = _upvotedIncidentIds.value.contains(incidentId)

    // Offline draft flow
    val activeDraft: Flow<ComplaintDraftEntity?> = complaintDao.getActiveDraft()

    suspend fun saveDraft(
        title: String,
        description: String,
        category: String,
        address: String,
        latitude: Double,
        longitude: Double,
        hasPhoto: Boolean,
        photoUri: String
    ) {
        val draft = ComplaintDraftEntity(
            id = "ACTIVE_CITIZEN_DRAFT",
            title = title,
            description = description,
            category = category,
            address = address,
            latitude = latitude,
            longitude = longitude,
            hasPhoto = hasPhoto,
            photoUri = photoUri,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        complaintDao.saveDraft(draft)
    }

    suspend fun clearDraft() {
        complaintDao.clearDraft()
    }

    // Auth State
    private val _authUiState = MutableStateFlow(
        AuthUiState(
            isAuthenticated = authService.getCurrentUser() != null,
            isFirebaseConfigured = authService.isConfigured(),
            currentUser = authService.getCurrentUser()
        )
    )
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    // App Theme Preference Persistence
    private val themePrefs = context.getSharedPreferences("civic_theme_preferences", Context.MODE_PRIVATE)
    private val _themeMode = MutableStateFlow(
        try {
            val saved = themePrefs.getString("app_theme_mode", AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
            AppThemeMode.valueOf(saved)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        try {
            themePrefs.edit().putString("app_theme_mode", mode.name).apply()
        } catch (e: Exception) {
            Log.w("CivicRepository", "Failed to persist theme preference: ${e.message}")
        }
    }

    // Active Citizen UID for user-scoped complaint persistence
    private val _activeCitizenUid = MutableStateFlow(
        authService.getCurrentUser()?.uid ?: "CN-CITIZEN-001"
    )

    private val _cityPulse = MutableStateFlow(SampleData.initialCityPulse)
    val cityPulse: StateFlow<CityPulseStats> = _cityPulse.asStateFlow()

    // Persistent Complaints Flow backed by Room Database
    @OptIn(ExperimentalCoroutinesApi::class)
    val complaints: StateFlow<List<Complaint>> = _activeCitizenUid
        .flatMapLatest { uid ->
            complaintDao.getComplaintsForUser(uid).map { entities ->
                entities.map { it.toDomain() }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun close() {
        scope.cancel()
    }

    private val _incidents = MutableStateFlow(SampleData.initialIncidents)
    val incidents: StateFlow<List<Incident>> = _incidents.asStateFlow()

    private val _emergencyAlerts = MutableStateFlow(SampleData.emergencyAlerts)
    val emergencyAlerts: StateFlow<List<EmergencyAlert>> = _emergencyAlerts.asStateFlow()

    private val _emergencyFacilities = MutableStateFlow(SampleData.emergencyFacilities)
    val emergencyFacilities: StateFlow<List<EmergencyFacility>> = _emergencyFacilities.asStateFlow()

    private val _notifications = MutableStateFlow(SampleData.initialNotifications)
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    private val _chatMessages = MutableStateFlow(SampleData.initialChatMessages)
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _simulation = MutableStateFlow(SampleData.defaultEmergencySimulation)
    val simulation: StateFlow<EmergencySimulation> = _simulation.asStateFlow()

    private val _profile = MutableStateFlow(
        authService.getCurrentUser()?.let { user ->
            SampleData.citizenProfile.copy(
                uid = user.uid,
                name = user.displayName ?: "Citizen",
                email = user.email ?: "",
                citizenId = "CN-OD-2026-" + user.uid.take(5).uppercase(),
                role = "citizen"
            )
        } ?: SampleData.citizenProfile
    )
    val profile: StateFlow<CitizenProfile> = _profile.asStateFlow()

    init {
        // Observe live Auth session changes
        scope.launch {
            authService.observeAuthState().collect { authUser ->
                val configured = authService.isConfigured()
                if (authUser != null) {
                    _activeCitizenUid.value = authUser.uid
                    _authUiState.value = _authUiState.value.copy(
                        isAuthenticated = true,
                        isFirebaseConfigured = configured,
                        currentUser = authUser,
                        errorMessage = null
                    )
                    _profile.value = _profile.value.copy(
                        uid = authUser.uid,
                        name = authUser.displayName ?: "Citizen",
                        email = authUser.email ?: "",
                        citizenId = "CN-OD-2026-" + authUser.uid.take(5).uppercase(),
                        role = "citizen"
                    )
                } else {
                    _activeCitizenUid.value = "CN-CITIZEN-001"
                    _authUiState.value = _authUiState.value.copy(
                        isAuthenticated = false,
                        isFirebaseConfigured = configured,
                        currentUser = null
                    )
                }
            }
        }
    }

    fun isGeminiOnline(): Boolean = geminiService.isGeminiKeyConfigured()

    fun setAuthScreenMode(mode: AuthScreenMode) {
        _authUiState.value = _authUiState.value.copy(
            authScreenMode = mode,
            errorMessage = null,
            successMessage = null
        )
    }

    fun clearAuthMessages() {
        _authUiState.value = _authUiState.value.copy(errorMessage = null, successMessage = null)
    }

    suspend fun signInWithGoogle(activity: Activity): Result<CivicAuthUser> {
        _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null)
        val result = authService.signInWithGoogle(activity)
        result.onSuccess { user ->
            _activeCitizenUid.value = user.uid
            _authUiState.value = _authUiState.value.copy(
                isAuthenticated = true,
                isLoading = false,
                currentUser = user,
                isFirebaseConfigured = true,
                errorMessage = null
            )
        }.onFailure { err ->
            val isCancelled = err is GoogleSignInCancelledException
            _authUiState.value = _authUiState.value.copy(
                isLoading = false,
                isFirebaseConfigured = authService.isConfigured(),
                errorMessage = if (isCancelled) null else (err.localizedMessage ?: "Google sign in failed")
            )
        }
        return result
    }

    suspend fun signInWithEmail(email: String, pass: String): Result<CivicAuthUser> {
        _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null)
        val result = authService.signInWithEmail(email, pass)
        result.onSuccess { user ->
            _activeCitizenUid.value = user.uid
            _authUiState.value = _authUiState.value.copy(
                isAuthenticated = true,
                isLoading = false,
                currentUser = user,
                errorMessage = null
            )
        }.onFailure { err ->
            _authUiState.value = _authUiState.value.copy(
                isLoading = false,
                errorMessage = err.localizedMessage ?: "Authentication failed"
            )
        }
        return result
    }

    suspend fun registerWithEmail(fullName: String, email: String, pass: String): Result<CivicAuthUser> {
        _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null)
        val result = authService.registerWithEmail(fullName, email, pass)
        result.onSuccess { user ->
            _activeCitizenUid.value = user.uid
            _authUiState.value = _authUiState.value.copy(
                isAuthenticated = true,
                isLoading = false,
                currentUser = user,
                successMessage = "Account registered successfully!",
                errorMessage = null
            )
        }.onFailure { err ->
            _authUiState.value = _authUiState.value.copy(
                isLoading = false,
                errorMessage = err.localizedMessage ?: "Registration failed"
            )
        }
        return result
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> {
        _authUiState.value = _authUiState.value.copy(isLoading = true, errorMessage = null)
        val result = authService.sendPasswordReset(email)
        result.onSuccess {
            _authUiState.value = _authUiState.value.copy(
                isLoading = false,
                successMessage = "Password reset link sent to $email."
            )
        }.onFailure { err ->
            _authUiState.value = _authUiState.value.copy(
                isLoading = false,
                errorMessage = err.localizedMessage ?: "Failed to send reset link."
            )
        }
        return result
    }

    suspend fun signOut(): Result<Unit> {
        val result = authService.signOut()
        _activeCitizenUid.value = "CN-CITIZEN-001"
        _authUiState.value = _authUiState.value.copy(
            isAuthenticated = false,
            currentUser = null,
            authScreenMode = AuthScreenMode.LOGIN,
            errorMessage = null,
            successMessage = null
        )
        // Reset transient chat data without wiping persistent complaints
        _chatMessages.value = SampleData.initialChatMessages
        return result
    }

    suspend fun analyzeIncident(description: String, categoryName: String?): AiAnalysisResult {
        return geminiService.analyzeIncident(description, categoryName)
    }

    suspend fun sendChatMessage(userText: String) {
        val userMsgId = UUID.randomUUID().toString()
        val nowTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        val userMessage = ChatMessage(
            id = userMsgId,
            text = userText,
            isUser = true,
            timestamp = nowTime
        )

        val thinkingMsgId = UUID.randomUUID().toString()
        val thinkingMessage = ChatMessage(
            id = thinkingMsgId,
            text = "Analyzing civic knowledge network...",
            isUser = false,
            timestamp = nowTime,
            isAiThinking = true
        )

        _chatMessages.value = _chatMessages.value + userMessage + thinkingMessage

        val context = "City pulse score is ${_cityPulse.value.pulseScore}/100. " +
                "Complaints logged: ${complaints.value.size}. " +
                "Latest reference: ${complaints.value.firstOrNull()?.referenceId ?: "None"}."

        val (responseString, isGemini) = geminiService.askAssistant(userText, context)

        _chatMessages.value = _chatMessages.value.filter { it.id != thinkingMsgId } + ChatMessage(
            id = UUID.randomUUID().toString(),
            text = responseString,
            isUser = false,
            timestamp = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()),
            isAiThinking = false,
            isGeminiPowered = isGemini,
            suggestedActions = if (userText.lowercase().contains("report")) {
                listOf("Go to Report Screen", "Check Active Incidents")
            } else {
                listOf("How do I report a pothole?", "Emergency helplines", "Status of CN-2026-1042")
            }
        )
    }

    suspend fun submitNewComplaint(
        title: String,
        description: String,
        category: IncidentCategory,
        address: String,
        latitude: Double,
        longitude: Double,
        hasPhoto: Boolean,
        aiAnalysis: AiAnalysisResult
    ): Result<Complaint> {
        // 1. Validation
        if (title.isBlank() && description.isBlank()) {
            return Result.failure(IllegalArgumentException("Incident description or title is required."))
        }
        if (address.isBlank()) {
            return Result.failure(IllegalArgumentException("Civic address or landmark is required."))
        }

        return try {
            val currentUid = authService.getCurrentUser()?.uid ?: _activeCitizenUid.value

            // 2. Generate unique reference ID
            var refId: String
            var attempts = 0
            do {
                val randNum = Random.nextInt(1000, 9999)
                refId = "CN-2026-$randNum"
                attempts++
            } while (complaintDao.getComplaintById(refId) != null && attempts < 10)

            val currentTimeStr = "Today, " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val initialTimeline = listOf(
                TimelineStep(
                    status = ComplaintStatus.SUBMITTED,
                    title = "Citizen Report Logged",
                    timestamp = currentTimeStr,
                    note = "Citizen ${profile.value.name} submitted report with GPS coordinates ($latitude, $longitude).",
                    isCompleted = true,
                    isCurrent = false
                ),
                TimelineStep(
                    status = ComplaintStatus.UNDER_REVIEW,
                    title = "AI Triage Verification",
                    timestamp = "Just now",
                    note = "Confidence ${aiAnalysis.confidenceScore}%: Routing to ${aiAnalysis.recommendedDepartment}.",
                    isCompleted = true,
                    isCurrent = true
                ),
                TimelineStep(
                    status = ComplaintStatus.ASSIGNED,
                    title = "Zonal Engineer Assignment",
                    timestamp = "Pending",
                    note = "Auto-assigning to nearest municipal division response van.",
                    isCompleted = false,
                    isCurrent = false
                ),
                TimelineStep(
                    status = ComplaintStatus.IN_PROGRESS,
                    title = "On-Site Physical Remediation",
                    timestamp = "Pending",
                    note = "Dispatched crew will remediate and provide photographic proof.",
                    isCompleted = false,
                    isCurrent = false
                ),
                TimelineStep(
                    status = ComplaintStatus.RESOLVED,
                    title = "Civic Quality Assurance",
                    timestamp = "Pending",
                    note = "Telemetry inspection and citizen feedback loop.",
                    isCompleted = false,
                    isCurrent = false
                )
            )

            val newComplaint = Complaint(
                referenceId = refId,
                category = category,
                title = title.ifBlank { "${category.displayName} issue at $address" },
                description = description,
                hasPhoto = hasPhoto,
                photoName = if (hasPhoto) "civic_incident_capture.jpg" else "",
                latitude = latitude,
                longitude = longitude,
                address = address,
                submittedDate = currentTimeStr,
                status = ComplaintStatus.UNDER_REVIEW,
                timeline = initialTimeline,
                assignedOfficer = "Assigned to ${aiAnalysis.recommendedDepartment}",
                aiAnalysis = aiAnalysis,
                userId = currentUid
            )

            // 3. Save to persistent Room database
            complaintDao.insertComplaint(newComplaint.toEntity())

            // 4. Background sync to Firestore (best effort)
            try {
                firestoreService.submitComplaint(newComplaint)
            } catch (e: Throwable) {
                Log.w("CivicRepository", "Firestore sync skipped: ${e.message}")
            }

            // 5. Update citizen profile count & emit local notification
            _profile.value = _profile.value.copy(
                reportsCount = _profile.value.reportsCount + 1,
                impactPoints = _profile.value.impactPoints + 50
            )

            val notif = AppNotification(
                id = "NOTIF-" + UUID.randomUUID().toString().take(6),
                title = "Report Submitted: $refId",
                message = "Your civic report has been received and routed to ${aiAnalysis.recommendedDepartment}.",
                category = "Complaint Update",
                timestamp = "Just now",
                isRead = false,
                isSimulated = true
            )
            _notifications.value = listOf(notif) + _notifications.value

            Result.success(newComplaint)
        } catch (e: Throwable) {
            Log.e("CivicRepository", "Failed to persist complaint: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun upvoteIncident(incidentId: String): Boolean {
        if (_upvotedIncidentIds.value.contains(incidentId)) {
            return false // Already verified by this citizen
        }
        val current = _incidents.value.toMutableList()
        val index = current.indexOfFirst { it.id == incidentId }
        if (index != -1) {
            val item = current[index]
            current[index] = item.copy(verifiedCount = item.verifiedCount + 1)
            _incidents.value = current

            val updatedSet = _upvotedIncidentIds.value + incidentId
            _upvotedIncidentIds.value = updatedSet
            try {
                votePrefs.edit().putStringSet("voted_ids", updatedSet).apply()
            } catch (e: Exception) {
                Log.w("CivicRepository", "Failed to persist vote: ${e.message}")
            }
            // Best effort Firestore upvote
            scope.launch {
                try {
                    firestoreService.upvoteIncidentVerification(incidentId)
                } catch (e: Exception) {
                    // Ignored in offline/local mode
                }
            }
            return true
        }
        return false
    }

    suspend fun reopenComplaint(referenceId: String, citizenFeedback: String): Result<Unit> {
        return try {
            val existing = complaintDao.getComplaintById(referenceId)
                ?: return Result.failure(IllegalArgumentException("Complaint $referenceId not found in local store."))

            val domain = existing.toDomain()
            val nowTime = "Today, " + SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val reopenedStep = TimelineStep(
                status = ComplaintStatus.REOPENED,
                title = "Complaint Reopened by Citizen",
                timestamp = nowTime,
                note = "Citizen escalated: ${citizenFeedback.ifBlank { "Issue persisted or recurrence observed." }}",
                isCompleted = true,
                isCurrent = true
            )

            val updatedTimeline = domain.timeline.map { it.copy(isCurrent = false) } + reopenedStep
            val updatedTimelineJson = ComplaintConverters.timelineToJson(updatedTimeline)

            // Update in Room
            complaintDao.updateComplaintStatus(
                referenceId = referenceId,
                newStatus = ComplaintStatus.REOPENED.name,
                newTimelineJson = updatedTimelineJson
            )

            // Best effort sync to Firestore
            try {
                firestoreService.updateComplaintTimeline(referenceId, ComplaintStatus.REOPENED.name, citizenFeedback)
            } catch (e: Throwable) {
                Log.w("CivicRepository", "Firestore reopen sync skipped: ${e.message}")
            }

            // Emit notification
            val notif = AppNotification(
                id = "NOTIF-REOPEN-" + UUID.randomUUID().toString().take(6),
                title = "Complaint Reopened: $referenceId",
                message = "Your grievance has been reopened for priority zonal review.",
                category = "Status Update",
                timestamp = "Just now",
                isRead = false,
                isSimulated = true
            )
            _notifications.value = listOf(notif) + _notifications.value

            Result.success(Unit)
        } catch (e: Throwable) {
            Log.e("CivicRepository", "Failed to reopen complaint: ${e.message}", e)
            Result.failure(e)
        }
    }

    fun markNotificationAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun startCoordinationSimulation() {
        if (_simulation.value.isRunning) return

        scope.launch {
            _simulation.value = _simulation.value.copy(isRunning = true, isCompleted = false)

            val baseSteps = _simulation.value.steps
            for (stepIndex in baseSteps.indices) {
                delay(1200)
                val updatedSteps = _simulation.value.steps.mapIndexed { idx, step ->
                    if (idx == stepIndex) {
                        step.copy(status = "Active", progress = 1.0f)
                    } else if (idx < stepIndex) {
                        step.copy(status = "Resolved", progress = 1.0f)
                    } else {
                        step
                    }
                }
                _simulation.value = _simulation.value.copy(steps = updatedSteps)
            }

            delay(1000)
            val finalSteps = _simulation.value.steps.map { it.copy(status = "Resolved", progress = 1.0f) }
            _simulation.value = _simulation.value.copy(
                steps = finalSteps,
                isRunning = false,
                isCompleted = true
            )
        }
    }

    fun resetCoordinationSimulation() {
        _simulation.value = SampleData.defaultEmergencySimulation
    }
}
