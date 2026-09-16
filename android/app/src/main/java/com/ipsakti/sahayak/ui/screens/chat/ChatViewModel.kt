package com.ipsakti.sahayak.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipsakti.sahayak.data.manager.LanguageManager
import com.ipsakti.sahayak.data.model.ChatMessage
import com.ipsakti.sahayak.data.model.SuggestedAction
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val suggestedPrompts: List<String> = emptyList()
)

class ChatViewModel(
    private val repository: IpSaktiRepository = IpSaktiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        initializeChat()
    }

    private fun initializeChat() {
        val lang = LanguageManager.getLanguage()
        val greetingText = when (lang) {
            "hi" -> "🙏 **नमस्ते! मैं आयुर्शक्ति सहायक हूँ।**\n\nमैं आपकी पारंपरिक जड़ी-बूटियों (अश्वगंधा, तुलसी, नीम आदि), पेटेंट धारा 3(p), टीकेडीएल (TKDL) पूर्व कला, और आयुष लाइसेंसिंग की शंकाओं को दूर करने में मदद करता हूँ।\n\nमैं आपको इस ऐप की सुविधाओं की सिफारिश भी कर सकता हूँ। मुझसे कोई भी प्रश्न पूछें!"
            "or" -> "🙏 **ନମସ୍କାର! ମୁଁ ଆୟୁରଶକ୍ତି ସହାୟକ।**\n\nମୁଁ ଆପଣଙ୍କ ପାରମ୍ପରିକ ଔଷଧୀୟ ଉଦ୍ଭିଦ, ପେଟେଣ୍ଟ ଧାରା 3(p), TKDL ପୂର୍ବ କଳା ଏବଂ ଆୟୁଷ ଲାଇସେନ୍ସିଂ ସମ୍ବନ୍ଧୀୟ ସମସ୍ତ ସନ୍ଦେହ ଦୂର କରିବାରେ ସାହାଯ୍ୟ କରେ।\n\nମୋତେ ଯେକୌଣସି ପ୍ରଶ୍ନ ପଚାରନ୍ତୁ!"
            else -> "🌿 **Hello! I am AyurSakti, your Ayurvedic & IP Intelligence Guide.**\n\nI can help resolve your doubts about medicinal plants (Ashwagandha, Tulsi, Neem, etc.), Section 3(p) patent exclusions, TKDL prior art, and AYUSH regulations.\n\nI can also recommend and take you directly to relevant features across the IP-SAKTI app! Ask me anything below."
        }

        val initialActions = listOf(
            SuggestedAction(
                label = if (lang == "hi") "🔍 धारा 3(p) जांच" else if (lang == "or") "🔍 ଧାରା 3(p) ଯାଞ୍ଚ" else "🔍 Test Patentability",
                targetScreen = "investigate",
                payload = "Can I patent an Ayurvedic formulation with Ashwagandha and Tulsi?"
            ),
            SuggestedAction(
                label = if (lang == "hi") "📄 दस्तावेज़ अपलोड" else if (lang == "or") "📄 ଦସ୍ତାବିଜ୍ ଅପଲୋଡ୍" else "📄 Upload Monograph",
                targetScreen = "upload"
            ),
            SuggestedAction(
                label = if (lang == "hi") "🗺️ अनुपालन रोडमैप" else if (lang == "or") "🗺️ ଅନୁପାଳନ ରୋଡମ୍ୟାପ୍" else "🗺️ Compliance Roadmap",
                targetScreen = "roadmap"
            )
        )

        val prompts = when (lang) {
            "hi" -> listOf(
                "क्या मैं अश्वगंधा सिरप का पेटेंट करा सकता हूँ?",
                "आयुष नियम 158B लाइसेंसिंग क्या है?",
                "धारा 3(p) के तहत सिंड्रोम और सहक्रिया कैसे सिद्ध करें?",
                "टीकेडीएल (TKDL) पेटेंट को कैसे रोकता है?"
            )
            "or" -> listOf(
                "ଅଶ୍ୱଗନ୍ଧା ସିରପ୍ ପାଇଁ ପେଟେଣ୍ଟ ମିଳିପାରିବ କି?",
                "ଆୟୁଷ ନିୟମ 158B ଲାଇସେନ୍ସିଂ କ'ଣ?",
                "ଧାରା 3(p) ଆପତ୍ତି କିପରି ଦୂର କରିବେ?",
                "ଜୈବ ବିବିଧତା NBA ଅନୁମୋଦନ କିପରି ପାଇବେ?"
            )
            else -> listOf(
                "Can I patent an Ashwagandha + Tulsi formulation?",
                "What is Ayush Rule 158B manufacturing license?",
                "How to prove synergistic efficacy under Section 3(p)?",
                "How does TKDL database prevent biopiracy?"
            )
        }

        val greeting = ChatMessage(
            id = "msg_init",
            sender = "assistant",
            text = greetingText,
            timestamp = getCurrentTime(),
            actions = initialActions
        )

        _uiState.value = _uiState.value.copy(
            messages = listOf(greeting),
            suggestedPrompts = prompts
        )
    }

    fun onInputChanged(newText: String) {
        _uiState.value = _uiState.value.copy(inputText = newText)
    }

    fun sendMessage(customText: String? = null) {
        val textToSend = (customText ?: _uiState.value.inputText).trim()
        if (textToSend.isEmpty() || _uiState.value.isLoading) return

        val userMessage = ChatMessage(
            id = "msg_user_${System.currentTimeMillis()}",
            sender = "user",
            text = textToSend,
            timestamp = getCurrentTime()
        )

        val updatedMessages = _uiState.value.messages + userMessage
        _uiState.value = _uiState.value.copy(
            messages = updatedMessages,
            inputText = "",
            isLoading = true
        )

        val lang = LanguageManager.getLanguage()
        viewModelScope.launch {
            val result = repository.sendChatMessage(
                message = textToSend,
                history = updatedMessages,
                language = lang
            )

            result.onSuccess { response ->
                val assistantMessage = ChatMessage(
                    id = "msg_bot_${System.currentTimeMillis()}",
                    sender = "assistant",
                    text = response.reply,
                    timestamp = getCurrentTime(),
                    actions = response.suggestedActions
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + assistantMessage,
                    isLoading = false
                )
            }.onFailure { err ->
                val fallback = repository.getFallbackChatResponse(textToSend, lang)
                val assistantMessage = ChatMessage(
                    id = "msg_bot_${System.currentTimeMillis()}",
                    sender = "assistant",
                    text = fallback.reply,
                    timestamp = getCurrentTime(),
                    actions = fallback.suggestedActions
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + assistantMessage,
                    isLoading = false
                )
            }
        }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
    }
}
