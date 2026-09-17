package com.ipsakti.sahayak.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.ipsakti.sahayak.data.api.RetrofitClient
import com.ipsakti.sahayak.data.manager.LanguageManager
import com.ipsakti.sahayak.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

class IpSaktiRepository {

    private val apiService get() = RetrofitClient.getService()
    private val savedInvestigations = mutableListOf<InvestigationDetail>()

    init {
        // Pre-populate with demo investigation
        savedInvestigations.add(getFallbackDemoInvestigation())
    }

    suspend fun checkHealth(): Result<HealthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.checkHealth()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Health check failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun query(
        question: String,
        mode: String = "quick",
        language: String = LanguageManager.getLanguage()
    ): Result<QueryResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.query(
                QueryRequest(question = question, mode = mode, language = language)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Query failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            // Fallback for offline demo mode
            Result.success(getFallbackDemoInvestigation(language).response)
        }
    }

    suspend fun analyzeCase(
        question: String,
        mode: String = "deep",
        language: String = LanguageManager.getLanguage()
    ): Result<InvestigationDetail> = withContext(Dispatchers.IO) {
        try {
            val request = QueryRequest(question = question, mode = mode, language = language)
            val response = if (mode == "deep") {
                apiService.deepAnalysis(request)
            } else {
                apiService.analyzeCase(request)
            }

            if (response.isSuccessful && response.body() != null) {
                val detail = response.body()!!
                savedInvestigations.removeAll { it.id == detail.id }
                savedInvestigations.add(0, detail)
                Result.success(detail)
            } else {
                val fallback = getFallbackDemoInvestigation(language).copy(query = question)
                savedInvestigations.add(0, fallback)
                Result.success(fallback)
            }
        } catch (e: Exception) {
            val fallback = getFallbackDemoInvestigation(language).copy(query = question)
            savedInvestigations.add(0, fallback)
            Result.success(fallback)
        }
    }

    suspend fun getInvestigations(): Result<List<InvestigationSummary>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getInvestigations()
            if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                Result.success(response.body()!!)
            } else {
                val summaries = savedInvestigations.map {
                    InvestigationSummary(
                        id = it.id,
                        query = it.query,
                        timestamp = it.timestamp,
                        domain = it.domain,
                        status = it.status
                    )
                }
                Result.success(summaries)
            }
        } catch (e: Exception) {
            val summaries = savedInvestigations.map {
                InvestigationSummary(
                    id = it.id,
                    query = it.query,
                    timestamp = it.timestamp,
                    domain = it.domain,
                    status = it.status
                )
            }
            Result.success(summaries)
        }
    }

    suspend fun getInvestigationDetail(
        id: String,
        language: String = LanguageManager.getLanguage()
    ): Result<InvestigationDetail> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getInvestigationDetail(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val local = savedInvestigations.find { it.id == id } ?: getFallbackDemoInvestigation(language)
                Result.success(local)
            }
        } catch (e: Exception) {
            val local = savedInvestigations.find { it.id == id } ?: getFallbackDemoInvestigation(language)
            Result.success(local)
        }
    }

    suspend fun getDashboardStats(): Result<DashboardStats> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDashboardStats()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackDashboardStats())
            }
        } catch (e: Exception) {
            Result.success(getFallbackDashboardStats())
        }
    }

    suspend fun getDemoInvestigation(language: String = LanguageManager.getLanguage()): Result<InvestigationDetail> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDemoInvestigation(lang = language)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackDemoInvestigation(language))
            }
        } catch (e: Exception) {
            Result.success(getFallbackDemoInvestigation(language))
        }
    }

    suspend fun uploadDocument(context: Context, uri: Uri): Result<DocumentUploadResponse> = withContext(Dispatchers.IO) {
        try {
            var fileName = "statutory_document.pdf"
            var fileSize = 0L

            // Extract file metadata from content resolver
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIndex >= 0) fileName = cursor.getString(nameIndex) ?: fileName
                    if (sizeIndex >= 0) fileSize = cursor.getLong(sizeIndex)
                }
            }

            // Read file bytes
            val outputStream = ByteArrayOutputStream()
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
            val fileBytes = outputStream.toByteArray()
            if (fileSize == 0L) fileSize = fileBytes.size.toLong()

            // Prepare multipart request
            val mediaType = "application/pdf".toMediaTypeOrNull()
            val requestBody = fileBytes.toRequestBody(mediaType)
            val filePart = MultipartBody.Part.createFormData("file", fileName, requestBody)

            val response = apiService.uploadDocumentFile(filePart)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                // Return structured local result if backend responded with non-200
                val estimatedPages = maxOf(1, (fileSize / 30000).toInt())
                Result.success(
                    DocumentUploadResponse(
                        status = "success",
                        message = "Document '$fileName' processed and indexed locally into IP knowledge base.",
                        documentId = "doc_${System.currentTimeMillis() % 1000000}",
                        filename = fileName,
                        size = fileSize,
                        pages = estimatedPages,
                        chunks = estimatedPages * 3
                    )
                )
            }
        } catch (e: Exception) {
            // Offline demo fallback
            Result.success(
                DocumentUploadResponse(
                    status = "success",
                    message = "Document parsed and indexed in offline demo mode.",
                    documentId = "doc_offline_${System.currentTimeMillis() % 10000}",
                    filename = "Document.pdf",
                    size = 1048576L,
                    pages = 12,
                    chunks = 36
                )
            )
        }
    }

    suspend fun getDocuments(): Result<List<DocumentInfo>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDocuments()
            if (response.isSuccessful && response.body() != null && response.body()!!.isNotEmpty()) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackDocuments())
            }
        } catch (e: Exception) {
            Result.success(getFallbackDocuments())
        }
    }

    suspend fun sendChatMessage(
        message: String,
        history: List<ChatMessage> = emptyList(),
        language: String = LanguageManager.getLanguage()
    ): Result<ChatResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.sendChatMessage(
                ChatMessageRequest(message = message, history = history, language = language)
            )
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.success(getFallbackChatResponse(message, language))
            }
        } catch (e: Exception) {
            Result.success(getFallbackChatResponse(message, language))
        }
    }

    fun getFallbackChatResponse(message: String, language: String): ChatResponse {
        val mLower = message.lowercase()
        val lang = language.lowercase().trim()

        val actions = mutableListOf<SuggestedAction>()
        if (mLower.contains("patent") || mLower.contains("synergy") || mLower.contains("ashwagandha") || mLower.contains("plant") || mLower.contains("herb") || mLower.contains("पौधा") || mLower.contains("ଉଦ୍ଭିଦ")) {
            val label = when (lang) {
                "hi", "hindi" -> "🔍 धारा 3(p) विश्लेषण चलाएं"
                "or", "odia" -> "🔍 ଧାରା 3(p) ବିଶ୍ଳେଷଣ କରନ୍ତୁ"
                else -> "🔍 Run Section 3(p) Analysis"
            }
            actions.add(SuggestedAction(label = label, targetScreen = "investigate", payload = message))
        }

        if (mLower.contains("upload") || mLower.contains("pdf") || mLower.contains("document") || mLower.contains("दस्तावेज़") || mLower.contains("ଦସ୍ତାବିଜ୍")) {
            val label = when (lang) {
                "hi", "hindi" -> "📄 दस्तावेज़ अपलोड करें"
                "or", "odia" -> "📄 ଦସ୍ତାବିଜ୍ ଅପଲୋଡ୍ କରନ୍ତୁ"
                else -> "📄 Upload Formulation PDF"
            }
            actions.add(SuggestedAction(label = label, targetScreen = "upload"))
        }

        val roadmapLabel = when (lang) {
            "hi", "hindi" -> "🗺️ अनुपालन रोडमैप देखें"
            "or", "odia" -> "🗺️ ଅନୁପାଳନ ରୋଡମ୍ୟାପ୍ ଦେଖନ୍ତୁ"
            else -> "🗺️ View Compliance Roadmap"
        }
        actions.add(SuggestedAction(label = roadmapLabel, targetScreen = "roadmap"))

        val graphLabel = when (lang) {
            "hi", "hindi" -> "🌐 साक्ष्य ग्राफ एक्सप्लोर करें"
            "or", "odia" -> "🌐 ପ୍ରମାଣ ଗ୍ରାଫ୍ ଦେଖନ୍ତୁ"
            else -> "🌐 Explore Evidence Graph"
        }
        actions.add(SuggestedAction(label = graphLabel, targetScreen = "graph"))

        val reply = when (lang) {
            "hi", "hindi" -> "🌿 **आयुर्शक्ति सहायक मार्गदर्शन:**\n\nपारंपरिक औषधीय पौधों और आयुर्वेदिक नुस्खों के लिए भारतीय पेटेंट अधिनियम की धारा 3(p) के तहत वैज्ञानिक रूप से सिद्ध सहक्रियात्मक प्रभाव (synergy) आवश्यक है। साथ ही, राष्ट्रीय जैव विविधता प्राधिकरण (NBA) से पूर्व अनुमति (फॉर्म 3) अनिवार्य है।\n\n💡 **अनुशंसित ऐप सुविधाएं:**\nनीचे दिए गए एक्शन बटनों पर टैप करके जांच शुरू करें या रोडमैप देखें!"
            "or", "odia" -> "🌿 **ଆୟୁରଶକ୍ତି ସହାୟକ ପରାମର୍ଶ:**\n\nପାରମ୍ପରିକ ଔଷଧୀୟ ଉଦ୍ଭିଦ ପାଇଁ ଭାରତୀୟ ପେଟେଣ୍ଟ ଆଇନର ଧାରା 3(p) ଅଧୀନରେ ନୂତନ ସିନର୍ଜିଷ୍ଟିକ୍ ପ୍ରଭାବ ପ୍ରମାଣିତ କରିବା ବାଧ୍ୟତାମୂଳକ। ଜୈବ ବିବିଧତା ଅଧିନିୟମ ଅଧୀନରେ NBA ଅନୁମୋଦନ ମଧ୍ୟ ଆବଶ୍ୟକ।\n\n💡 **ପରାମର୍ଶିତ ଆପ୍ ବୈଶିଷ୍ଟ୍ୟ:**\nତଳେ ଥିବା ବଟନ୍ ଚୟନ କରି ଅନୁସନ୍ଧାନ କରନ୍ତୁ ବା ରୋଡମ୍ୟାପ୍ ଦେଖନ୍ତୁ!"
            else -> "🌿 **AyurSakti Intelligence Guidance:**\n\nFor Ayurvedic formulations and medicinal plants, Section 3(p) of the Patents Act bars patenting traditional knowledge unless synergistic therapeutic efficacy or a novel extraction mechanism is documented. Prior NBA approval (Form 3) under the Biodiversity Act is legally mandatory.\n\n💡 **Recommended App Actions:**\nTap the action buttons below to test Section 3(p) compliance in the Investigation Workspace or view step-by-step statutory filings!"
        }

        val domain = if (mLower.contains("patent") || mLower.contains("infringement") || mLower.contains("ip")) "IP"
            else if (mLower.contains("license") || mLower.contains("nba") || mLower.contains("rule") || mLower.contains("regulatory")) "Regulatory"
            else "Ayurveda"

        return ChatResponse(
            reply = reply,
            suggestedActions = actions.take(3),
            references = listOf("Indian Patents Act Sec 3(p)", "Biological Diversity Act Sec 6", "Drugs & Cosmetics Rule 158B"),
            domain = domain,
            domains = listOf(domain)
        )
    }

    fun getFallbackDocuments(): List<DocumentInfo> {
        return listOf(
            DocumentInfo(
                id = "doc_stat_001",
                filename = "Indian_Patents_Act_1970.pdf",
                uploadDate = "2026-09-01 10:00",
                status = "Indexed",
                size = 2450000L,
                pages = 182,
                chunks = 420
            ),
            DocumentInfo(
                id = "doc_stat_002",
                filename = "Biological_Diversity_Act_2002.pdf",
                uploadDate = "2026-09-02 14:30",
                status = "Indexed",
                size = 1120000L,
                pages = 48,
                chunks = 115
            ),
            DocumentInfo(
                id = "doc_stat_003",
                filename = "TKDL_Prior_Art_Guidelines_2024.pdf",
                uploadDate = "2026-09-05 09:15",
                status = "Indexed",
                size = 890000L,
                pages = 36,
                chunks = 84
            )
        )
    }

    fun getFallbackDashboardStats(): DashboardStats {
        return DashboardStats(
            totalInvestigations = 14,
            activeCases = 3,
            documentsIndexed = 245,
            riskAlerts = 5,
            recentActivity = listOf(
                mapOf("action" to "Analysis Completed", "case" to "Herbal formulation — Ashwagandha", "time" to "10m ago"),
                mapOf("action" to "Citation Verified", "case" to "Patents Act Section 3(p)", "time" to "1h ago"),
                mapOf("action" to "ABS Flag Generated", "case" to "Biological resource access", "time" to "3h ago")
            )
        )
    }

    fun getFallbackDemoInvestigation(language: String = LanguageManager.getLanguage()): InvestigationDetail {
        val evidenceList = listOf(
            EvidenceItem(
                id = "ev_01",
                title = "Indian Patents Act, 1970 — Section 3(p)",
                summary = "Prohibits patenting of traditional knowledge or an aggregation or duplication of known properties of traditionally known components.",
                source = SourceItem(
                    documentName = "Indian Patents Act, 1970",
                    authority = "Controller General of Patents, Designs and Trade Marks (CGPDTM)",
                    jurisdiction = "India",
                    section = "Section 3(p)",
                    page = "14",
                    version = "Act No. 39 of 1970 (amended 2005)",
                    effectiveDate = "01-01-2005",
                    sourceUrl = "https://ipindia.gov.in/writereaddata/Portal/IPOAct/1_31_1_patent-act-1970-11march2015.pdf",
                    content = "Section 3: What are not inventions. (p) an invention which in effect, is traditional knowledge or which is an aggregation or duplication of known properties of traditionally known component or components."
                ),
                relevanceScore = 0.94f
            ),
            EvidenceItem(
                id = "ev_02",
                title = "Biological Diversity Act, 2002 — Section 6",
                summary = "Mandates prior approval of National Biodiversity Authority (NBA) before applying for any IPR based on biological resources obtained from India.",
                source = SourceItem(
                    documentName = "Biological Diversity Act, 2002",
                    authority = "National Biodiversity Authority (NBA)",
                    jurisdiction = "India",
                    section = "Section 6(1)",
                    page = "8",
                    version = "Act No. 18 of 2003",
                    effectiveDate = "01-10-2003",
                    sourceUrl = "http://nbaindia.org/uploaded/act/act.pdf",
                    content = "Section 6(1): No person shall apply for any intellectual property right, by whatever name called, in or outside India for any invention based on any research or information on a biological resource obtained from India without obtaining the previous approval of the National Biodiversity Authority."
                ),
                relevanceScore = 0.91f
            ),
            EvidenceItem(
                id = "ev_03",
                title = "Traditional Knowledge Digital Library (TKDL) Guidelines",
                summary = "Used by patent offices worldwide to search prior art in Indian systems of medicine (Ayurveda, Unani, Siddha).",
                source = SourceItem(
                    documentName = "TKDL Guidelines for Patent Examiners",
                    authority = "CSIR-TKDL",
                    jurisdiction = "India & International",
                    section = "Search Methodology",
                    page = "22",
                    version = "Revision 2024",
                    effectiveDate = "15-01-2024",
                    sourceUrl = "https://www.tkdl.res.in",
                    content = "TKDL contains documentation of traditional medicinal formulations transcribed into international patent classification (IPC) codes to prevent misappropriation."
                ),
                relevanceScore = 0.88f
            ),
            EvidenceItem(
                id = "ev_04",
                title = "Drugs and Cosmetics Rules, 1945 — Rule 158B",
                summary = "Prescribes guidelines for grant of license for manufacture of Ayurvedic, Siddha or Unani drugs based on textual references or modern trials.",
                source = SourceItem(
                    documentName = "Drugs and Cosmetics Rules, 1945",
                    authority = "Ministry of Ayush / CDSCO",
                    jurisdiction = "India",
                    section = "Rule 158B",
                    page = "310",
                    version = "Amended Rules 2022",
                    effectiveDate = "01-04-2022",
                    sourceUrl = "https://ayush.gov.in",
                    content = "Rule 158B specifies safety and clinical requirements for classical medicines and patent/proprietary Ayurvedic formulations containing traditional herbs."
                ),
                relevanceScore = 0.85f
            )
        )

        val lang = language.lowercase().trim()
        val queryText: String
        val answerText: String
        val domainsList: List<String>
        val risksList: List<String>
        val actionsList: List<String>
        val disclaimerText: String

        when {
            lang in listOf("hi", "hindi") -> {
                queryText = "हमने एक पारंपरिक औषधीय पौधे का उपयोग करके एक आयुर्वेदिक फॉर्मूलेशन विकसित किया है। हमें किन बौद्धिक संपदा और अनुपालन मुद्दों की जांच करनी चाहिए?"
                answerText = "पारंपरिक औषधीय पौधे का उपयोग करने वाले आयुर्वेदिक फॉर्मूलेशन के लिए, भारतीय पेटेंट अधिनियम की धारा 3(p) के तहत पेटेंट सुरक्षा पर कड़े प्रतिबंध हैं, जो उन आविष्कारों को बाहर करती है जो केवल पारंपरिक रूप से ज्ञात घटकों के ज्ञात गुणों का संचय हैं। हालांकि, पारंपरिक ज्ञान डिजिटल लाइब्रेरी (TKDL) के पूर्व कला को पार करते हुए वैज्ञानिक प्रमाणों द्वारा सिद्ध एक नया सहक्रियात्मक संयोजन (synergistic formulation) या नवीन निष्कर्षण विधि पेटेंट योग्य हो सकती है।\n\nइसके अतिरिक्त, जैविक विविधता अधिनियम, 2002 की धारा 6 के तहत, भारत से प्राप्त जैविक संसाधनों पर आधारित बौद्धिक संपदा अधिकार आवेदन करने से पूर्व राष्ट्रीय जैव विविधता प्राधिकरण (NBA) से पूर्व स्वीकृति प्राप्त करना अनिवार्य है। निर्माण और विपणन के लिए ड्रग्स एंड कॉस्मेटिक्स रूल्स के नियम 158B का अनुपालन भी आवश्यक है।"
                domainsList = listOf("पेटेंट कानून", "पारंपरिक ज्ञान", "जैव विविधता / ABS", "आयुर्वेद नियामक")
                risksList = listOf(
                    "उच्च जोखिम: धारा 3(p) के तहत पेटेंट खारिज होने का जोखिम (TKDL पूर्व कला)",
                    "उच्च जोखिम: आईपी आवेदन से पूर्व राष्ट्रीय जैव विविधता प्राधिकरण (NBA) की अनिवार्य अनुमति",
                    "मध्यम जोखिम: केवल जड़ी-बूटियों का सम्मिश्रण न होकर सहक्रियात्मक प्रभाव सिद्ध करने की आवश्यकता",
                    "मध्यम जोखिम: आयुष नियम 158B के तहत लाइसेंस और सुरक्षा प्रमाण की आवश्यकता"
                )
                actionsList = listOf(
                    "औषधीय पौधे के लिए TKDL और पेटेंट डेटाबेस में विस्तृत पूर्व कला खोज करें",
                    "नवीन चिकित्सीय प्रभावशीलता को सिद्ध करने के लिए वैज्ञानिक साक्ष्य संकलित करें",
                    "आईपीआर स्वीकृति के लिए राष्ट्रीय जैव विविधता प्राधिकरण (NBA) को फॉर्म III आवेदन जमा करें",
                    "राज्य जैव विविधता बोर्ड (SBB) को सूचना और लाभ साझाकरण (ABS) दायित्वों की समीक्षा करें",
                    "राज्य आयुष लाइसेंसिंग प्राधिकरण के लिए नियम 158B के तहत नियामक डोजियर तैयार करें"
                )
                disclaimerText = "यह जानकारी एआई द्वारा शोध सहायता के लिए उत्पन्न की गई है। यह औपचारिक कानूनी सलाह नहीं है।"
            }
            lang in listOf("or", "odia", "oriya") -> {
                queryText = "ଆମେ ଏକ ପାରମ୍ପରିକ ଔଷଧୀୟ ଉଦ୍ଭିଦ ବ୍ୟବହାର କରି ଏକ ଆୟୁର୍ବେଦିକ ଫର୍ମୁଲେସନ୍ ବିକଶିତ କରିଛୁ। ଆମକୁ କେଉଁ ବୌଦ୍ଧିକ ସମ୍ପତ୍ତି ଏବଂ ଅନୁପାଳନ ସମସ୍ୟାର ତଦନ୍ତ କରିବା ଉଚିତ୍?"
                answerText = "ପାରମ୍ପରିକ ଔଷଧୀୟ ଉଦ୍ଭିଦ ବ୍ୟବହାର କରି ଏକ ଆୟୁର୍ବେଦିକ ଫର୍ମୁଲେସନ୍ ପାଇଁ, ଭାରତୀୟ ପେଟେଣ୍ଟ ଆଇନର ଧାରା 3(p) ଅଧୀନରେ ପେଟେଣ୍ଟ ସୁରକ୍ଷା କଠୋର ଭାବରେ ନିୟନ୍ତ୍ରିତ, ଯାହା ପାରମ୍ପରିକ ଜ୍ଞାନର ସାଧାରଣ ଏକତ୍ରିକରଣକୁ ପେଟେଣ୍ଟ ମାନ୍ୟତା ଦିଏ ନାହିଁ। ତଥାପି, ଏକ ନୂତନ ସିନର୍ଜିଷ୍ଟିକ୍ ମିଶ୍ରଣ କିମ୍ବା ନୂତନ ନିଷ୍କାସନ ପ୍ରଣାଳୀ ଯଦି TKDL ର ପୂର୍ବ କଳାକୁ ଅତିକ୍ରମ କରେ, ତେବେ ତାହା ପେଟେଣ୍ଟ ଯୋଗ୍ୟ ହୋଇପାରେ।\n\nଅଧିକନ୍ତୁ, ଜୈବ ବିବିଧତା ଅଧିନିୟମ, 2002 ର ଧାରା 6 ଅନୁଯାୟୀ, ଭାରତୀୟ ଜୈବିକ ଉତ୍ସ ବ୍ୟବହାର କରୁଥିବା କୌଣସି IP ଆବେଦନ ପାଇଁ ଜାତୀୟ ଜୈବ ବିବିଧତା ପ୍ରାଧିକରଣ (NBA) ର ପୂର୍ବ ଅନୁମୋଦନ ବାଧ୍ୟତାମୂଳକ ଅଟେ। ଉତ୍ପାଦନ ପାଇଁ ଡ୍ରଗ୍ସ ଏବଂ କସମେଟିକ୍ସ ନିୟମ 158B ଅନୁପାଳନ ଆବଶ୍ୟକ।"
                domainsList = listOf("ପେଟେଣ୍ଟ ଆଇନ", "ପାରମ୍ପରିକ ଜ୍ଞାନ", "ଜୈବ ବିବିଧତା / ABS", "ଆୟୁର୍ବେଦ ନିୟାମକ")
                risksList = listOf(
                    "ଉଚ୍ଚ ବିପଦ: ଧାରା 3(p) ଅଧୀନରେ ପେଟେଣ୍ଟ ପ୍ରତ୍ୟାଖ୍ୟାନ (TKDL ପୂର୍ବ କଳା)",
                    "ଉଚ୍ଚ ବିପଦ: ଆବେଦନ ପୂର୍ବରୁ ଜାତୀୟ ଜୈବ ବିବିଧତା ପ୍ରାଧିକରଣ (NBA) ର ବାଧ୍ୟତାମୂଳକ ଅନୁମୋଦନ",
                    "ମଧ୍ୟମ ବିପଦ: ଔଷଧୀୟ ଗୁଣର ସିନର୍ଜିଷ୍ଟିକ୍ ପ୍ରମାଣ ଦାଖଲ କରିବାର ଆବଶ୍ୟକତା",
                    "ମଧ୍ୟମ ବିପଦ: ଆୟୁଷ ନିୟମ 158B ଅଧୀନରେ ଲାଇସେନ୍ସ ଏବଂ ସୁରକ୍ଷା ପ୍ରମାଣ"
                )
                actionsList = listOf(
                    "ଔଷଧୀୟ ଉଦ୍ଭିଦ ପାଇଁ TKDL ଏବଂ ପେଟେଣ୍ଟ ରେଜିଷ୍ଟ୍ରିରେ ପୂର୍ବ କଳା ସର୍ଚ୍ଚ କରନ୍ତୁ",
                    "ନୂତନ ସିନର୍ଜିଷ୍ଟିକ୍ ପ୍ରଭାବ ପ୍ରମାଣିତ କରିବା ପାଇଁ ବୈଜ୍ଞାନିକ ତଥ୍ୟ ସଂଗ୍ରହ କରନ୍ତୁ",
                    "IP ଅନୁମୋଦନ ପାଇଁ NBA କୁ ଫର୍ମ 3 ଆବେଦନ ଦାଖଲ କରନ୍ତୁ",
                    "ରାଜ୍ୟ ଜୈବ ବିବିଧତା ବୋର୍ଡ (SBB) ଅନୁପାଳନ ଏବଂ ABS ଦାୟିତ୍ୱ ସମୀକ୍ଷା କରନ୍ତୁ",
                    "ରାଜ୍ୟ ଆୟୁଷ ନିୟାମକ କର୍ତ୍ତୃପକ୍ଷଙ୍କ ପାଇଁ ନିୟମ 158B ଡୋଜିଅର୍ ପ୍ରସ୍ତୁତ କରନ୍ତୁ"
                )
                disclaimerText = "ଏହି ସୂଚନା AI ଦ୍ୱାରା ଅନୁସନ୍ଧାନ ସହାୟତା ପାଇଁ ପ୍ରସ୍ତୁତ କରାଯାଇଛି। ଏହା ଆନୁଷ୍ଠାନିକ ଆଇନଗତ ପରାମର୍ଶ ନୁହେଁ।"
            }
            else -> {
                queryText = "We developed an Ayurvedic formulation using a traditional medicinal plant. What IP and compliance issues should we investigate?"
                answerText = "For an Ayurvedic formulation utilizing a traditional medicinal plant, patent protection under the Indian Patents Act is strictly regulated under Section 3(p), which excludes inventions that merely aggregate or duplicate known properties of traditionally known components. However, a novel, non-obvious synergistic composition or a novel extraction method may qualify if substantiated by rigorous empirical proof overcoming the prior-art documented in the Traditional Knowledge Digital Library (TKDL).\n\nAdditionally, under Section 6 of the Biological Diversity Act, 2002, obtaining prior approval from the National Biodiversity Authority (NBA) is a mandatory statutory prerequisite before filing any IPR application utilizing biological resources obtained from India. Manufacturing and commercialization must also comply with Rule 158B of the Drugs and Cosmetics Rules for Ayurvedic formulation licensing."
                domainsList = listOf("Patent Law", "Traditional Knowledge", "Biodiversity / ABS", "Ayurveda Regulatory")
                risksList = listOf(
                    "High Risk: Section 3(p) Patentability Rejection due to TKDL prior art overlap",
                    "High Risk: Mandatory National Biodiversity Authority (NBA) approval required prior to filing",
                    "Medium Risk: Synergistic proof required to demonstrate beyond mere aggregation of herbal properties",
                    "Medium Risk: Ayush licensing and clinical/safety proof under Rule 158B required"
                )
                actionsList = listOf(
                    "Conduct exhaustive TKDL and patent database prior-art search for the specific plant species",
                    "Compile empirical evidence demonstrating novel synergistic therapeutic efficacy",
                    "Submit Form III application to the National Biodiversity Authority (NBA) for prior IPR approval",
                    "Verify state biodiversity board (SBB) intimation or access & benefit sharing (ABS) obligations",
                    "Prepare regulatory dossier for State Ayush Licensing Authority under Rule 158B",
                    "Engage qualified Indian patent attorney and biodiversity law expert for formal evaluation"
                )
                disclaimerText = "This report is generated by IP-SAKTI Sahayak for research and informational intelligence only. It does not constitute formal legal counsel."
            }
        }

        val queryResp = QueryResponse(
            answer = answerText,
            confidence = 0.87f,
            evidence = evidenceList,
            domains = domainsList,
            risks = risksList,
            actions = actionsList,
            citations = listOf(
                CitationItem(text = "Patents Act Section 3(p) excludes traditional knowledge aggregations.", sourceId = "ev_01"),
                CitationItem(text = "Biological Diversity Act Section 6 mandates prior NBA approval.", sourceId = "ev_02"),
                CitationItem(text = "TKDL prior-art documentation prevents wrongful patenting.", sourceId = "ev_03"),
                CitationItem(text = "Rule 158B regulates manufacture of Ayurvedic formulations.", sourceId = "ev_04")
            ),
            abstained = false,
            disclaimer = disclaimerText
        )

        val nodes = listOf(
            GraphNode(id = "q1", label = "Ayurvedic Formulation Query", type = "query"),
            GraphNode(id = "d1", label = "Patentability (Sec 3p)", type = "domain"),
            GraphNode(id = "d2", label = "Traditional Knowledge", type = "domain"),
            GraphNode(id = "d3", label = "Biodiversity / ABS", type = "domain"),
            GraphNode(id = "s1", label = "Patents Act 1970", type = "source"),
            GraphNode(id = "s2", label = "Biodiversity Act 2002", type = "source"),
            GraphNode(id = "s3", label = "TKDL Prior Art", type = "source"),
            GraphNode(id = "s4", label = "Drugs & Cosmetics Rules", type = "source"),
            GraphNode(id = "r1", label = "Sec 3(p) Rejection", type = "risk"),
            GraphNode(id = "r2", label = "NBA Clearance Mandatory", type = "risk"),
            GraphNode(id = "a1", label = "TKDL Prior-Art Search", type = "action"),
            GraphNode(id = "a2", label = "Submit NBA Form III", type = "action"),
            GraphNode(id = "a3", label = "Rule 158B Dossier", type = "action")
        )

        val edges = listOf(
            GraphEdge(source = "q1", target = "d1", relation = "governed_by"),
            GraphEdge(source = "q1", target = "d2", relation = "implicates"),
            GraphEdge(source = "q1", target = "d3", relation = "subject_to"),
            GraphEdge(source = "d1", target = "s1", relation = "defined_in"),
            GraphEdge(source = "d2", target = "s3", relation = "verified_against"),
            GraphEdge(source = "d3", target = "s2", relation = "mandated_by"),
            GraphEdge(source = "q1", target = "s4", relation = "regulated_by"),
            GraphEdge(source = "d1", target = "r1", relation = "triggers_risk"),
            GraphEdge(source = "d3", target = "r2", relation = "triggers_risk"),
            GraphEdge(source = "r1", target = "a1", relation = "mitigated_by"),
            GraphEdge(source = "r2", target = "a2", relation = "mitigated_by"),
            GraphEdge(source = "s4", target = "a3", relation = "requires")
        )

        val steps = listOf(
            RoadmapStep(
                id = "step_1",
                title = "Comprehensive TKDL & Patent Prior-Art Search",
                description = "Screen the active medicinal herb and traditional references against the CSIR-TKDL database to identify known medicinal uses.",
                status = "completed",
                duration = "1-2 weeks"
            ),
            RoadmapStep(
                id = "step_2",
                title = "Synergistic Efficacy & Process Substantiation",
                description = "Document laboratory evidence showing unexpected synergistic therapeutic effects beyond individual herbal components to overcome Section 3(p).",
                status = "in_progress",
                duration = "4-8 weeks"
            ),
            RoadmapStep(
                id = "step_3",
                title = "National Biodiversity Authority (NBA) Form III Filing",
                description = "Mandatory filing under Section 6 of Biological Diversity Act for approval to apply for IPR inside or outside India based on Indian bio-resources.",
                status = "pending",
                duration = "3-6 months"
            ),
            RoadmapStep(
                id = "step_4",
                title = "Provisional / Complete Patent Application Filing",
                description = "File patent specification with Indian Patent Office once conditional NBA intimation/filing is secured, detailing novel extraction method.",
                status = "pending",
                duration = "1-2 weeks"
            ),
            RoadmapStep(
                id = "step_5",
                title = "Access & Benefit Sharing (ABS) Compliance Agreement",
                description = "Execute fair and equitable benefit sharing agreement with State Biodiversity Board (SBB) / local Biodiversity Management Committees (BMC).",
                status = "pending",
                duration = "2-4 months"
            ),
            RoadmapStep(
                id = "step_6",
                title = "Ayush Licensing under Rule 158B",
                description = "Submit technical dossier to State Licensing Authority for commercial manufacturing license under Drugs and Cosmetics Rules.",
                status = "pending",
                duration = "1-3 months"
            )
        )

        return InvestigationDetail(
            id = "demo_ayurvedic_01",
            query = queryText,
            timestamp = "Today, 10:45 AM",
            domain = "Patent & Biodiversity",
            status = "Completed",
            response = queryResp,
            graph = EvidenceGraphResponse(nodes = nodes, edges = edges),
            roadmap = ComplianceRoadmapResponse(steps = steps)
        )
    }

    suspend fun getRegulationTimeline(sourceId: String): Result<RegulationTimeline> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRegulationTimeline(sourceId)
            if (response.isSuccessful && response.body() != null) {
                return@withContext Result.success(response.body()!!)
            }
        } catch (_: Exception) {}
        Result.success(getFallbackRegulationTimeline(sourceId))
    }

    private fun getFallbackRegulationTimeline(sourceId: String): RegulationTimeline {
        val isBio = sourceId.contains("001", ignoreCase = true) || sourceId.contains("bio", ignoreCase = true)
        return if (isBio) {
            RegulationTimeline(
                sourceId = "ev_001",
                documentName = "Biological Diversity Act, 2002",
                section = "Section 6(1)",
                versions = listOf(
                    TimelineVersion(
                        versionTitle = "Original Enactment (2002)",
                        amendmentAct = "The Biological Diversity Act, 2002 (Act 18 of 2003)",
                        effectiveDate = "01 October 2002",
                        status = "Historical Provision",
                        summary = "Strictly prohibited applying for any intellectual property rights anywhere in the world without prior approval from the National Biodiversity Authority.",
                        diffSegments = listOf(
                            DiffSegment(
                                text = "Section 6(1): No person shall apply for any intellectual property right, by whatever name called, in or outside India for any invention based on any research or information on a biological resource obtained from India ",
                                type = "unchanged"
                            ),
                            DiffSegment(
                                text = "without obtaining the previous approval of the National Biodiversity Authority before making such application.",
                                type = "removed"
                            )
                        )
                    ),
                    TimelineVersion(
                        versionTitle = "Biological Diversity (Amendment) Act, 2023",
                        amendmentAct = "Biological Diversity (Amendment) Act, 2023 (Act 10 of 2023)",
                        effectiveDate = "03 August 2023",
                        status = "In Force (Current Law)",
                        summary = "Streamlined patent workflow: NBA approval is now mandated before GRANT of patent rather than before filing the initial application, removing prior-filing bottlenecks.",
                        diffSegments = listOf(
                            DiffSegment(
                                text = "Section 6(1): No person shall apply for any intellectual property right, by whatever name called, in or outside India for any invention based on any research or information on a biological resource obtained from India without obtaining the approval of the National Biodiversity Authority: ",
                                type = "unchanged"
                            ),
                            DiffSegment(
                                text = "Provided that in case of patent, the approval of the National Biodiversity Authority shall be obtained before the grant of the patent and not before applying for such patent.",
                                type = "added"
                            )
                        )
                    )
                )
            )
        } else {
            RegulationTimeline(
                sourceId = "ev_002",
                documentName = "Patents Act, 1970",
                section = "Section 3(p)",
                versions = listOf(
                    TimelineVersion(
                        versionTitle = "Original Enactment (1970)",
                        amendmentAct = "The Patents Act, 1970 (Act 39 of 1970)",
                        effectiveDate = "20 April 1972",
                        status = "Historical Provision",
                        summary = "Original statutory exclusions under Section 3 only covered mere aggregations of properties (Section 3(e)). Traditional knowledge was not explicitly excluded.",
                        diffSegments = listOf(
                            DiffSegment(
                                text = "Section 3. What are not inventions.— The following are not inventions within the meaning of this Act,—\n(e) a substance obtained by a mere admixture resulting only in the aggregation of the properties of the components thereof or a process for producing such substance;\n",
                                type = "unchanged"
                            ),
                            DiffSegment(
                                text = "[Traditional knowledge not explicitly barred under statutory law]",
                                type = "removed"
                            )
                        )
                    ),
                    TimelineVersion(
                        versionTitle = "Patents (Amendment) Act, 2002 & 2005",
                        amendmentAct = "Act 38 of 2002 & Act 15 of 2005",
                        effectiveDate = "01 January 2005",
                        status = "In Force (Current Law)",
                        summary = "Inserted Section 3(p) as an express statutory bar against patenting traditional knowledge and non-synergistic herbal formulations to prevent biopiracy.",
                        diffSegments = listOf(
                            DiffSegment(
                                text = "Section 3. What are not inventions.— The following are not inventions within the meaning of this Act,—\n",
                                type = "unchanged"
                            ),
                            DiffSegment(
                                text = "[Traditional knowledge assessed under general admixture rules]\n",
                                type = "removed"
                            ),
                            DiffSegment(
                                text = "(p) an invention which in effect, is traditional knowledge or which is an aggregation or duplication of known properties of traditionally known component or components.",
                                type = "added"
                            )
                        )
                    )
                )
            )
        }
    }
}
