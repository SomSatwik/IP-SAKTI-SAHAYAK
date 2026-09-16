package com.ipsakti.sahayak.data.manager

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object LanguageManager {
    private const val PREFS_NAME = "ipsakti_language_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    private var prefs: SharedPreferences? = null
    private val _currentLanguage = MutableStateFlow("en")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedLang = prefs?.getString(KEY_LANGUAGE, "en") ?: "en"
            _currentLanguage.value = savedLang
        }
    }

    fun getLanguage(): String = _currentLanguage.value

    fun setLanguage(langCode: String) {
        val normalized = when (langCode.lowercase()) {
            "hi", "hindi" -> "hi"
            "or", "odia", "oriya" -> "or"
            else -> "en"
        }
        _currentLanguage.value = normalized
        prefs?.edit()?.putString(KEY_LANGUAGE, normalized)?.apply()
    }

    fun getDisplayName(code: String): String {
        return when (code) {
            "hi" -> "हिन्दी (Hindi)"
            "or" -> "ଓଡ଼ିଆ (Odia)"
            else -> "English"
        }
    }

    /**
     * Localized dictionary mapping for key application terms across en, hi, and or.
     */
    private val translations = mapOf(
        "app_name" to mapOf(
            "en" to "IP-SAKTI SAHAYAK",
            "hi" to "आईपी-शक्ति सहायक",
            "or" to "ଆଇପି-ଶକ୍ତି ସହାୟକ"
        ),
        "app_tagline" to mapOf(
            "en" to "AI-Powered Indian IP Intelligence",
            "hi" to "एआई-संचालित भारतीय बौद्धिक संपदा आसूचना",
            "or" to "AI-ଚାଳିତ ଭାରତୀୟ ବୌଦ୍ଧିକ ସମ୍ପତ୍ତି ଗୁପ୍ତଚର"
        ),
        "nav_home" to mapOf(
            "en" to "Dashboard",
            "hi" to "डैशबोर्ड",
            "or" to "ଡ୍ୟାସବୋର୍ଡ"
        ),
        "nav_investigate" to mapOf(
            "en" to "Investigate",
            "hi" to "जांच करें",
            "or" to "ଅନୁସନ୍ଧାନ"
        ),
        "nav_evidence" to mapOf(
            "en" to "Evidence",
            "hi" to "साक्ष्य",
            "or" to "ପ୍ରମାଣ"
        ),
        "nav_saved" to mapOf(
            "en" to "Saved",
            "hi" to "सहेजे गए",
            "or" to "ସଂରକ୍ଷିତ"
        ),
        "nav_settings" to mapOf(
            "en" to "Settings",
            "hi" to "सेटिंग्स",
            "or" to "ସେଟିଙ୍ଗ୍ସ"
        ),
        "dashboard_title" to mapOf(
            "en" to "Executive Intelligence Dashboard",
            "hi" to "कार्यकारी आसूचना डैशबोर्ड",
            "or" to "କାର୍ଯ୍ୟନିର୍ବାହୀ ଗୁପ୍ତଚର ଡ୍ୟାସବୋର୍ଡ"
        ),
        "start_investigation" to mapOf(
            "en" to "Start Investigation",
            "hi" to "नई जांच शुरू करें",
            "or" to "ନୂତନ ଅନୁସନ୍ଧାନ ଆରମ୍ଭ କରନ୍ତୁ"
        ),
        "load_demo" to mapOf(
            "en" to "Load Demo Case",
            "hi" to "डेमो केस लोड करें",
            "or" to "ଡେମୋ ମାମଲା ଲୋଡ୍ କରନ୍ତୁ"
        ),
        "upload_doc" to mapOf(
            "en" to "Upload Document",
            "hi" to "दस्तावेज़ अपलोड करें",
            "or" to "ଦସ୍ତାବିଜ୍ ଅପଲୋଡ୍ କରନ୍ତୁ"
        ),
        "target_domains" to mapOf(
            "en" to "TARGET IP & REGULATORY DOMAINS",
            "hi" to "लक्षित बौद्धिक संपदा और नियामक क्षेत्र",
            "or" to "ଲକ୍ଷ୍ୟ ବୌଦ୍ଧିକ ସମ୍ପତ୍ତି ଏବଂ ନିୟାମକ କ୍ଷେତ୍ର"
        ),
        "describe_case" to mapOf(
            "en" to "DESCRIBE YOUR CASE / INVENTION",
            "hi" to "अपने मामले / आविष्कार का विवरण दें",
            "or" to "ଆପଣଙ୍କ ମାମଲା / ଉଦ୍ଭାବନର ବର୍ଣ୍ଣନା କରନ୍ତୁ"
        ),
        "deep_analysis" to mapOf(
            "en" to "Deep Analysis (Graph + Roadmap)",
            "hi" to "गहन विश्लेषण (ग्राफ + रोडमैप)",
            "or" to "ଗଭୀର ବିଶ୍ଳେଷଣ (ଗ୍ରାଫ୍ + ରୋଡମ୍ୟାପ୍)"
        ),
        "quick_answer" to mapOf(
            "en" to "Quick Grounded Answer",
            "hi" to "त्वरित प्रामाणिक उत्तर",
            "or" to "ଦ୍ରୁତ ପ୍ରାମାଣିକ ଉତ୍ତର"
        ),
        "run_analysis" to mapOf(
            "en" to "Run Grounded IP Analysis",
            "hi" to "प्रामाणिक आईपी विश्लेषण चलाएं",
            "or" to "ପ୍ରାମାଣିକ IP ବିଶ୍ଳେଷଣ କରନ୍ତୁ"
        ),
        "analyzing" to mapOf(
            "en" to "Analyzing Legal Sources...",
            "hi" to "कानूनी स्रोतों का विश्लेषण जारी है...",
            "or" to "ଆଇନଗତ ଉତ୍ସଗୁଡ଼ିକର ବିଶ୍ଳେଷଣ ଚାଲିଛି..."
        ),
        "key_findings" to mapOf(
            "en" to "KEY FINDINGS",
            "hi" to "मुख्य निष्कर्ष",
            "or" to "ମୁଖ୍ୟ ନିଷ୍କର୍ଷ"
        ),
        "identified_risks" to mapOf(
            "en" to "IDENTIFIED RISKS",
            "hi" to "पहचाने गए कानूनी जोखिम",
            "or" to "ଚିହ୍ନଟ ହୋଇଥିବା ଆଇନଗତ ବିପଦ"
        ),
        "authoritative_evidence" to mapOf(
            "en" to "AUTHORITATIVE EVIDENCE",
            "hi" to "प्रामाणिक वैधानिक साक्ष्य",
            "or" to "ପ୍ରାମାଣିକ ସାମ୍ବିଧାନିକ ପ୍ରମାଣ"
        ),
        "recommended_actions" to mapOf(
            "en" to "RECOMMENDED ACTIONS",
            "hi" to "अनुशंसित कानूनी कदम",
            "or" to "ପରାମର୍ଶିତ ଆଇନଗତ ପଦକ୍ଷେପ"
        ),
        "view_graph" to mapOf(
            "en" to "Evidence Graph",
            "hi" to "साक्ष्य ग्राफ",
            "or" to "ପ୍ରମାଣ ଗ୍ରାଫ୍"
        ),
        "view_roadmap" to mapOf(
            "en" to "Roadmap",
            "hi" to "रोडमैप",
            "or" to "ରୋଡମ୍ୟାପ୍"
        ),
        "settings_title" to mapOf(
            "en" to "Settings",
            "hi" to "सेटिंग्स",
            "or" to "ସେଟିଙ୍ଗ୍ସ"
        ),
        "settings_subtitle" to mapOf(
            "en" to "System Configuration & API Status",
            "hi" to "सिस्टम कॉन्फ़िगरेशन और एपीआई स्थिति",
            "or" to "ସିଷ୍ଟମ୍ କନଫିଗରେସନ୍ ଏବଂ API ସ୍ଥିତି"
        ),
        "backend_config" to mapOf(
            "en" to "BACKEND API CONFIGURATION",
            "hi" to "बैकएंड एपीआई कॉन्फ़िगरेशन",
            "or" to "ବ୍ୟାକଏଣ୍ଡ API କନଫିଗରେସନ୍"
        ),
        "test_connection" to mapOf(
            "en" to "Test Connection",
            "hi" to "कनेक्शन जांचें",
            "or" to "ସଂଯୋଗ ପରୀକ୍ଷା କରନ୍ତୁ"
        ),
        "save" to mapOf(
            "en" to "Save",
            "hi" to "सहेजें",
            "or" to "ସଂରକ୍ଷଣ କରନ୍ତୁ"
        ),
        "language_section" to mapOf(
            "en" to "APPLICATION LANGUAGE",
            "hi" to "एप्लिकेशन की भाषा",
            "or" to "ଆପ୍ଲିକେସନ୍ ଭାଷା"
        ),
        "upload_title" to mapOf(
            "en" to "Document Intelligence",
            "hi" to "दस्तावेज़ आसूचना",
            "or" to "ଦସ୍ତାବିଜ୍ ଗୁପ୍ତଚର"
        ),
        "upload_subtitle" to mapOf(
            "en" to "Ingest, Process & Index Authoritative Sources",
            "hi" to "वैधानिक स्रोतों को अपलोड, प्रोसेस और इंडेक्स करें",
            "or" to "ପ୍ରାମାଣିକ ଉତ୍ସଗୁଡ଼ିକୁ ଅପଲୋଡ୍, ପ୍ରକ୍ରିୟା ଏବଂ ଇଣ୍ଡେକ୍ସ କରନ୍ତୁ"
        ),
        "select_document" to mapOf(
            "en" to "Select Document (PDF)",
            "hi" to "दस्तावेज़ चुनें (PDF)",
            "or" to "ଦସ୍ତାବିଜ୍ ଚୟନ କରନ୍ତୁ (PDF)"
        ),
        "upload_and_index" to mapOf(
            "en" to "Upload & Index Document",
            "hi" to "अपलोड और इंडेक्स करें",
            "or" to "ଅପଲୋଡ୍ ଏବଂ ଇଣ୍ଡେକ୍ସ କରନ୍ତୁ"
        ),
        "indexed_documents" to mapOf(
            "en" to "INDEXED KNOWLEDGE BASE DOCUMENTS",
            "hi" to "इंडेक्स किए गए कानूनी दस्तावेज़",
            "or" to "ଇଣ୍ଡେକ୍ସ ହୋଇଥିବା ଆଇନଗତ ଦସ୍ତାବିଜ୍"
        )
    )

    fun getString(key: String): String {
        val lang = _currentLanguage.value
        val map = translations[key] ?: return key
        return map[lang] ?: map["en"] ?: key
    }
}
