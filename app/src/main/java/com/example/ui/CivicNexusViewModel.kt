package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CivicRepository
import com.example.data.local.ComplaintDraftEntity
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class SubScreen {
    data object None : SubScreen()
    data object ReportIssue : SubScreen()
    data object AiAssistant : SubScreen()
    data object EmergencyCenter : SubScreen()
    data object MyComplaints : SubScreen()
    data class ComplaintDetail(val complaint: Complaint) : SubScreen()
    data object Notifications : SubScreen()
}

class CivicNexusViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: CivicRepository = CivicRepository(application)

    // Authentication State
    val authUiState = repository.authUiState

    // Theme Mode
    val themeMode: StateFlow<AppThemeMode> = repository.themeMode

    fun setThemeMode(mode: AppThemeMode) {
        repository.setThemeMode(mode)
    }

    fun setAuthScreenMode(mode: AuthScreenMode) {
        repository.setAuthScreenMode(mode)
    }

    fun clearAuthMessages() {
        repository.clearAuthMessages()
    }

    fun signInWithGoogle(activity: Activity) {
        viewModelScope.launch {
            repository.signInWithGoogle(activity)
        }
    }

    fun signInWithEmail(email: String, pass: String) {
        viewModelScope.launch {
            repository.signInWithEmail(email, pass)
        }
    }

    fun registerWithEmail(name: String, email: String, pass: String) {
        viewModelScope.launch {
            repository.registerWithEmail(name, email, pass)
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            repository.sendPasswordReset(email)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            repository.signOut()
            _currentTab.value = MainNavigationTab.HOME
            _currentSubScreen.value = SubScreen.None
        }
    }

    // Navigation State
    private val _currentTab = MutableStateFlow(MainNavigationTab.HOME)
    val currentTab: StateFlow<MainNavigationTab> = _currentTab.asStateFlow()

    private val _currentSubScreen = MutableStateFlow<SubScreen>(SubScreen.None)
    val currentSubScreen: StateFlow<SubScreen> = _currentSubScreen.asStateFlow()

    // Repository Flows
    val cityPulse = repository.cityPulse
    val complaints = repository.complaints
    val incidents = repository.incidents
    val emergencyAlerts = repository.emergencyAlerts
    val emergencyFacilities = repository.emergencyFacilities
    val notifications = repository.notifications
    val chatMessages = repository.chatMessages
    val isChatLoading = repository.isChatLoading
    val simulation = repository.simulation
    val profile = repository.profile

    val isGeminiConfigured = repository.isGeminiOnline()

    // Localization
    val currentLanguage = repository.currentLanguage
    fun setLanguage(language: com.example.util.AppLanguage) {
        repository.setLanguage(language)
    }

    // Active Offline Draft
    val activeDraft: StateFlow<ComplaintDraftEntity?> = repository.activeDraft.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Map Screen Filter & Selection
    private val _mapCategoryFilter = MutableStateFlow<IncidentCategory?>(null)
    val mapCategoryFilter: StateFlow<IncidentCategory?> = _mapCategoryFilter.asStateFlow()

    private val _selectedMapIncident = MutableStateFlow<Incident?>(null)
    val selectedMapIncident: StateFlow<Incident?> = _selectedMapIncident.asStateFlow()

    private val _mapSector = MutableStateFlow("All Sectors")
    val mapSector: StateFlow<String> = _mapSector.asStateFlow()

    // Complaint Report Form State
    private val _reportCategory = MutableStateFlow(IncidentCategory.POTHOLES)
    val reportCategory: StateFlow<IncidentCategory> = _reportCategory.asStateFlow()

    private val _reportTitle = MutableStateFlow("")
    val reportTitle: StateFlow<String> = _reportTitle.asStateFlow()

    private val _reportDescription = MutableStateFlow("")
    val reportDescription: StateFlow<String> = _reportDescription.asStateFlow()

    private val _reportAddress = MutableStateFlow("Plot 42, Avenue 3, KIIT Innovation Enclave, Patia")
    val reportAddress: StateFlow<String> = _reportAddress.asStateFlow()

    private val _reportLatitude = MutableStateFlow(20.3533)
    val reportLatitude: StateFlow<Double> = _reportLatitude.asStateFlow()

    private val _reportLongitude = MutableStateFlow(85.8172)
    val reportLongitude: StateFlow<Double> = _reportLongitude.asStateFlow()

    private val _reportHasPhoto = MutableStateFlow(false)
    val reportHasPhoto: StateFlow<Boolean> = _reportHasPhoto.asStateFlow()

    private val _reportPhotoUri = MutableStateFlow<String?>(null)
    val reportPhotoUri: StateFlow<String?> = _reportPhotoUri.asStateFlow()

    private val _reportAiAnalysis = MutableStateFlow<AiAnalysisResult?>(null)
    val reportAiAnalysis: StateFlow<AiAnalysisResult?> = _reportAiAnalysis.asStateFlow()

    private val _isAnalyzingAi = MutableStateFlow(false)
    val isAnalyzingAi: StateFlow<Boolean> = _isAnalyzingAi.asStateFlow()

    private val _isSubmittingReport = MutableStateFlow(false)
    val isSubmittingReport: StateFlow<Boolean> = _isSubmittingReport.asStateFlow()

    private val _submissionError = MutableStateFlow<String?>(null)
    val submissionError: StateFlow<String?> = _submissionError.asStateFlow()

    private val _lastSubmittedComplaint = MutableStateFlow<Complaint?>(null)
    val lastSubmittedComplaint: StateFlow<Complaint?> = _lastSubmittedComplaint.asStateFlow()

    fun setReportPhotoUri(uri: String?) {
        _reportPhotoUri.value = uri
        _reportHasPhoto.value = !uri.isNullOrBlank()
    }

    // Complaints Tab Filter
    private val _complaintStatusFilter = MutableStateFlow<ComplaintStatus?>(null)
    val complaintStatusFilter: StateFlow<ComplaintStatus?> = _complaintStatusFilter.asStateFlow()

    private val _complaintSearchQuery = MutableStateFlow("")
    val complaintSearchQuery: StateFlow<String> = _complaintSearchQuery.asStateFlow()

    // Navigation Methods
    fun selectTab(tab: MainNavigationTab) {
        _currentTab.value = tab
        _currentSubScreen.value = SubScreen.None
    }

    fun openSubScreen(subScreen: SubScreen) {
        _currentSubScreen.value = subScreen
    }

    fun closeSubScreen() {
        _currentSubScreen.value = SubScreen.None
    }

    fun handleBackPress(): Boolean {
        if (_currentSubScreen.value !is SubScreen.None) {
            _currentSubScreen.value = SubScreen.None
            return true
        }
        if (_currentTab.value != MainNavigationTab.HOME) {
            _currentTab.value = MainNavigationTab.HOME
            return true
        }
        return false
    }

    // Map Actions
    fun setMapCategoryFilter(category: IncidentCategory?) {
        _mapCategoryFilter.value = category
    }

    fun selectMapIncident(incident: Incident?) {
        _selectedMapIncident.value = incident
    }

    fun setMapSector(sector: String) {
        _mapSector.value = sector
    }

    // Report Actions
    fun setReportCategory(category: IncidentCategory) {
        _reportCategory.value = category
    }

    fun setReportTitle(title: String) {
        _reportTitle.value = title
    }

    fun setReportDescription(desc: String) {
        _reportDescription.value = desc
    }

    fun setReportAddress(address: String) {
        _reportAddress.value = address
    }

    fun setReportCoordinates(lat: Double, lng: Double, address: String) {
        _reportLatitude.value = lat
        _reportLongitude.value = lng
        _reportAddress.value = address
    }

    fun toggleReportHasPhoto(hasPhoto: Boolean) {
        _reportHasPhoto.value = hasPhoto
    }

    fun triggerAiAnalysis() {
        if (_isAnalyzingAi.value) return
        val desc = _reportDescription.value.ifBlank { "Road damage with deep pothole posing severe vehicle hazard." }

        viewModelScope.launch {
            _isAnalyzingAi.value = true
            try {
                val result = repository.analyzeIncident(desc, _reportCategory.value.displayName)
                _reportAiAnalysis.value = result
                _reportCategory.value = result.suggestedCategory
                if (_reportTitle.value.isBlank()) {
                    _reportTitle.value = result.incidentSummary
                }
            } finally {
                _isAnalyzingAi.value = false
            }
        }
    }

    fun updateAiAnalysisReview(category: IncidentCategory, priority: IncidentSeverity, department: String) {
        _reportAiAnalysis.value = _reportAiAnalysis.value?.copy(
            suggestedCategory = category,
            suggestedPriority = priority,
            recommendedDepartment = department,
            isReviewedByCitizen = true
        )
        _reportCategory.value = category
    }

    fun clearSubmissionError() {
        _submissionError.value = null
    }

    fun submitComplaint(): Boolean {
        if (_isSubmittingReport.value) return false
        val title = _reportTitle.value
        val desc = _reportDescription.value
        val address = _reportAddress.value

        if (desc.isBlank() && title.isBlank()) {
            _submissionError.value = "Please provide an incident description or title before submitting."
            return false
        }

        viewModelScope.launch {
            _isSubmittingReport.value = true
            _submissionError.value = null
            try {
                val analysis = _reportAiAnalysis.value ?: repository.analyzeIncident(
                    desc.ifBlank { title },
                    _reportCategory.value.displayName
                )

                val result = repository.submitNewComplaint(
                    title = title.ifBlank { analysis.incidentSummary },
                    description = desc.ifBlank { analysis.incidentSummary },
                    category = _reportCategory.value,
                    address = address,
                    latitude = _reportLatitude.value,
                    longitude = _reportLongitude.value,
                    hasPhoto = _reportHasPhoto.value,
                    aiAnalysis = analysis
                )

                result.onSuccess { created ->
                    _lastSubmittedComplaint.value = created
                    _submissionError.value = null
                    // Reset form fields only on success
                    _reportTitle.value = ""
                    _reportDescription.value = ""
                    _reportAiAnalysis.value = null
                }.onFailure { error ->
                    _submissionError.value = error.localizedMessage ?: "Failed to save complaint to persistent storage."
                }
            } catch (e: Exception) {
                _submissionError.value = e.localizedMessage ?: "Unexpected error saving complaint."
            } finally {
                _isSubmittingReport.value = false
            }
        }
        return true
    }

    fun dismissSuccessDialog() {
        _lastSubmittedComplaint.value = null
    }

    // Draft Management
    fun saveDraft() {
        viewModelScope.launch {
            repository.saveDraft(
                title = _reportTitle.value,
                description = _reportDescription.value,
                category = _reportCategory.value.displayName,
                address = _reportAddress.value,
                latitude = _reportLatitude.value,
                longitude = _reportLongitude.value,
                hasPhoto = _reportHasPhoto.value,
                photoUri = _reportPhotoUri.value ?: ""
            )
        }
    }

    fun restoreDraft(draft: com.example.data.local.ComplaintDraftEntity) {
        _reportTitle.value = draft.title
        _reportDescription.value = draft.description
        _reportCategory.value = IncidentCategory.fromDisplayName(draft.category)
        _reportAddress.value = draft.address
        _reportLatitude.value = draft.latitude
        _reportLongitude.value = draft.longitude
        _reportHasPhoto.value = draft.hasPhoto
        _reportPhotoUri.value = draft.photoUri.ifBlank { null }
    }

    fun discardDraft() {
        viewModelScope.launch {
            repository.clearDraft()
        }
    }

    // Incidents & Upvoting with restrictions
    fun hasUpvotedIncident(incidentId: String): Boolean = repository.hasUpvoted(incidentId)

    fun upvoteIncident(incidentId: String): Boolean {
        return repository.upvoteIncident(incidentId)
    }

    // Complaint Reopening Workflow
    fun reopenComplaint(referenceId: String, reason: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.reopenComplaint(referenceId, reason)
            onComplete(result.isSuccess)
        }
    }

    // Assistant Chat
    fun sendChatMessage(text: String) {
        if (text.isBlank() || isChatLoading.value) return
        viewModelScope.launch {
            repository.sendChatMessage(text)
        }
    }

    fun retryChatMessage(prompt: String) {
        if (isChatLoading.value) return
        viewModelScope.launch {
            repository.retryChatMessage(prompt)
        }
    }

    fun requestOfflineGuidance(query: String) {
        repository.provideOfflineGuidance(query)
    }

    fun clearChat() {
        repository.clearChat()
    }

    // Emergency Simulation
    fun startEmergencySimulation() {
        repository.startCoordinationSimulation()
    }

    fun resetEmergencySimulation() {
        repository.resetCoordinationSimulation()
    }

    // Notifications
    fun markNotificationAsRead(id: String) {
        repository.markNotificationAsRead(id)
    }

    fun markAllNotificationsAsRead() {
        repository.markAllNotificationsAsRead()
    }

    // Complaints Filter
    fun setComplaintStatusFilter(status: ComplaintStatus?) {
        _complaintStatusFilter.value = status
    }

    fun setComplaintSearchQuery(query: String) {
        _complaintSearchQuery.value = query
    }
}
