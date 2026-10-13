package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    ODIA("ଓଡ଼ିଆ (Odia)", "or"),
    HINDI("हिन्दी (Hindi)", "hi");

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: ENGLISH
        }
    }
}

class LocalizationManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("civic_locale_prefs", Context.MODE_PRIVATE)

    private val _currentLanguage = MutableStateFlow(
        AppLanguage.fromCode(prefs.getString("selected_language_code", AppLanguage.ENGLISH.code) ?: "en")
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString("selected_language_code", language.code).apply()
    }

    fun getString(englishKey: String): String {
        val lang = _currentLanguage.value
        return translations[lang]?.get(englishKey)
            ?: translations[AppLanguage.ENGLISH]?.get(englishKey)
            ?: englishKey
    }

    companion object {
        private val translations: Map<AppLanguage, Map<String, String>> = mapOf(
            AppLanguage.ENGLISH to mapOf(
                "home" to "Home",
                "explore" to "Explore",
                "complaints" to "My Complaints",
                "notifications" to "Notifications",
                "profile" to "Profile",
                "welcome" to "Welcome back,",
                "report_issue" to "Report Issue",
                "explore_nearby" to "Explore Nearby",
                "emergency_sos" to "Emergency SOS",
                "quick_actions" to "Quick Actions",
                "total_reports" to "Total Reports",
                "active_issues" to "Active Issues",
                "resolved" to "Resolved",
                "under_review" to "Under Review",
                "recent_activity" to "Recent Activity",
                "nearby_issues" to "Nearby Civic Issues",
                "community_impact" to "Community Impact",
                "submit_report" to "Submit Report",
                "save_draft" to "Save Draft",
                "discard_draft" to "Discard Draft",
                "ai_assist" to "AI Assist",
                "voice_input" to "Voice Input",
                "take_photo" to "Take Photo",
                "choose_gallery" to "Choose Gallery",
                "use_location" to "Use Current Location"
            ),
            AppLanguage.HINDI to mapOf(
                "home" to "होम",
                "explore" to "एक्सप्लोर",
                "complaints" to "मेरी शिकायतें",
                "notifications" to "सूचनाएं",
                "profile" to "प्रोफ़ाइल",
                "welcome" to "नमस्ते,",
                "report_issue" to "समस्या दर्ज करें",
                "explore_nearby" to "आस-पास देखें",
                "emergency_sos" to "आपातकालीन एसओएस",
                "quick_actions" to "त्वरित कार्य",
                "total_reports" to "कुल रिपोर्ट",
                "active_issues" to "सक्रिय मुद्दे",
                "resolved" to "समाधान हुआ",
                "under_review" to "समीक्षाधीन",
                "recent_activity" to "हालिया गतिविधि",
                "nearby_issues" to "आस-पास की समस्याएं",
                "community_impact" to "सामुदायिक प्रभाव",
                "submit_report" to "शिकायत भेजें",
                "save_draft" to "ड्राफ्ट सहेजें",
                "discard_draft" to "ड्राफ्ट हटाएं",
                "ai_assist" to "एआई सहायता",
                "voice_input" to "वॉयस इनपुट",
                "take_photo" to "फोटो लें",
                "choose_gallery" to "गैलरी से चुनें",
                "use_location" to "वर्तमान स्थान उपयोग करें"
            ),
            AppLanguage.ODIA to mapOf(
                "home" to "ମୂଳପୃଷ୍ଠା",
                "explore" to "ଅନ୍ୱେଷଣ",
                "complaints" to "ମୋର ଅଭିଯୋଗ",
                "notifications" to "ବିଜ୍ଞପ୍ତି",
                "profile" to "ପ୍ରୋଫାଇଲ୍",
                "welcome" to "ନମସ୍କାର,",
                "report_issue" to "ଅଭିଯୋଗ ଦାଖଲ",
                "explore_nearby" to "ନିକଟସ୍ଥ ସମସ୍ୟା",
                "emergency_sos" to "ଜରୁରୀକାଳୀନ ଏସଓଏସ",
                "quick_actions" to "ତୁରନ୍ତ କାର୍ଯ୍ୟ",
                "total_reports" to "ମୋଟ ଅଭିଯୋଗ",
                "active_issues" to "ସକ୍ରିୟ ସମସ୍ୟା",
                "resolved" to "ସମାଧାନ ହୋଇଛି",
                "under_review" to "ସମୀକ୍ଷାଧୀନ",
                "recent_activity" to "ସାମ୍ପ୍ରତିକ କାର୍ଯ୍ୟକଳାପ",
                "nearby_issues" to "ନିକଟସ୍ଥ ସମସ୍ୟା",
                "community_impact" to "ସାମାଜିକ ପ୍ରଭାବ",
                "submit_report" to "ଅଭିଯୋଗ ପଠାନ୍ତୁ",
                "save_draft" to "ଡ୍ରାଫ୍ଟ ସାଇତନ୍ତୁ",
                "discard_draft" to "ଡ୍ରାଫ୍ଟ କାଢନ୍ତୁ",
                "ai_assist" to "ଏଆଇ ସହାୟତା",
                "voice_input" to "ଭଏସ୍ ଇନପୁଟ୍",
                "take_photo" to "ଫଟୋ ଉଠାନ୍ତୁ",
                "choose_gallery" to "ଗ୍ୟାଲେରୀରୁ ବାଛନ୍ତୁ",
                "use_location" to "ବର୍ତ୍ତମାନର ସ୍ଥାନ"
            )
        )
    }
}
