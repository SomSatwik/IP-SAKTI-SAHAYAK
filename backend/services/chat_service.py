import os
import logging
from typing import List
from dotenv import load_dotenv

from ..models import ChatMessageRequest, ChatResponse, SuggestedAction
from .query_service import query_service

logger = logging.getLogger(__name__)

class ChatService:
    def __init__(self):
        pass

    def get_chat_response(self, request: ChatMessageRequest) -> ChatResponse:
        message = request.message.strip()
        requested_lang = (request.language or "").lower().strip()
        from ..pipeline.multilingual import multilingual_manager
        detected_lang = multilingual_manager.detect_language(message)
        lang = requested_lang if requested_lang in ["hi", "hindi", "or", "odia", "en"] else detected_lang
        
        # 1. Try real LLM if GROQ_API_KEY is present
        load_dotenv(override=True)
        groq_key = os.getenv("GROQ_API_KEY")
        if groq_key:
            try:
                from langchain_groq import ChatGroq
                from langchain_core.messages import SystemMessage, HumanMessage, AIMessage

                llm = ChatGroq(
                    api_key=groq_key,
                    model="llama-3.3-70b-versatile",
                    temperature=0.3,
                    max_tokens=800
                )

                lang_instruction = multilingual_manager.get_system_prompt_instruction(lang)
                persona_instruction = self._get_persona_prompt_adjustment(request.persona or "startup")
                system_prompt = (
                    "You are AyurSakti, the expert Ayurvedic & IP Intelligence Assistant within the IP-SAKTI SAHAYAK mobile app. "
                    "You help users with their Ayurvedic formulations, traditional medicinal plants, Section 3(p) patent exclusions, "
                    "TKDL prior art, Biological Diversity Act (NBA approvals), and AYUSH Rule 158B licensing regulations. "
                    "Always provide precise, grounded guidance. At the end of your reply, explicitly recommend relevant features of the IP-SAKTI app "
                    "(e.g., 'Use our Investigation Workspace to test Section 3(p) compliance', 'View the Compliance Roadmap for step-by-step filings', "
                    "'Check the Evidence Graph for regulatory connections', or 'Upload your formulation monograph in Document Intelligence'). "
                    f"\n\nLanguage Guideline: {lang_instruction}"
                    f"\n\nPersona Register: {persona_instruction}"
                )

                messages = [SystemMessage(content=system_prompt)]
                # Add past 4 messages from history
                for h in request.history[-4:]:
                    if h.sender == "user":
                        messages.append(HumanMessage(content=h.text))
                    else:
                        messages.append(AIMessage(content=h.text))

                messages.append(HumanMessage(content=message))
                ai_resp = llm.invoke(messages)
                reply_text = ai_resp.content if hasattr(ai_resp, "content") else str(ai_resp)

                actions = self._generate_suggested_actions(message, lang)
                from ..pipeline.domain_classifier import domain_classifier
                classification = domain_classifier.classify(message)
                return ChatResponse(
                    reply=reply_text,
                    suggested_actions=actions,
                    references=["Indian Patents Act, 1970", "Biological Diversity Act, 2002", "Drugs & Cosmetics Rules, 1945"],
                    domain=classification.get("primary_domain", "Ayurveda"),
                    domains=classification.get("all_detected", ["Ayurveda"])
                )
            except Exception as e:
                logger.warning(f"Live chat LLM failed ({e}), using domain assistant.")

        # 2. Domain Knowledge Base Assistant (Multi-lingual & Offline)
        return self._generate_domain_response(message, lang)

    def _get_persona_prompt_adjustment(self, persona: str) -> str:
        p = (persona or "startup").lower().strip()
        if p == "practitioner":
            return (
                "User Persona: AYURVEDIC PRACTITIONER / VAIDYA. "
                "Tone: Clinical, grounded in classical Samhitas (Charaka, Sushruta) and Ayurvedic Pharmacopoeia (API). "
                "Emphasize patient safety, therapeutic indications, classical terminology (Rasa, Virya, Vipaka), and Schedule T GMP compliance."
            )
        elif p == "researcher":
            return (
                "User Persona: ACADEMIC / RESEARCHER. "
                "Tone: Rigorous, scientific, pharmacologically precise. "
                "Emphasize Section 3(p) prior art exclusions, Combination Index (Chou-Talalay synergy), characterization (HPTLC/HPLC), and clinical trial protocol validity."
            )
        elif p == "msme":
            return (
                "User Persona: MSME MANUFACTURER. "
                "Tone: Pragmatic, statutory, cost-sensitive. "
                "Emphasize 80% patent fee concessions via Form 28, MSME subsidies, State Licensing Authority (SLA) Rule 158B licensing, and AYUSH Standard/Premium Mark quality standards."
            )
        elif p == "cultivator":
            return (
                "User Persona: HERBAL CULTIVATOR / FPO. "
                "Tone: Accessible, agricultural, biodiversity-protective. "
                "Emphasize Biological Diversity Act 2002 exemptions (Normally Traded Commodities), fair Access and Benefit Sharing (ABS) terms, State Biodiversity Board (SBB) intimation, and BMC agreements."
            )
        else:
            return (
                "User Persona: AYUSH STARTUP / FOUNDER. "
                "Tone: Strategic, high-velocity, commercialization-focused. "
                "Emphasize Form 18A expedited patent examination, DPIIT Startup India benefits, seed grants (BIRAC/AYUSH), 90-day fast-track NBA clearance, and IP defensibility."
            )

    def _generate_suggested_actions(self, message: str, lang: str) -> List[SuggestedAction]:
        m_lower = message.lower()
        actions = []

        # Feature recommendations based on context
        if any(w in m_lower for w in ["patent", "synergy", "extract", "plant", "formulation", "ashwagandha", "turmeric", "tulsi", "neem"]):
            label = "🔍 Run Section 3(p) Analysis"
            if lang in ["hi", "hindi"]: label = "🔍 धारा 3(p) विश्लेषण चलाएं"
            elif lang in ["or", "odia", "oriya"]: label = "🔍 ଧାରା 3(p) ବିଶ୍ଳେଷଣ କରନ୍ତୁ"
            actions.append(SuggestedAction(
                label=label,
                target_screen="investigate",
                payload=message
            ))

        if any(w in m_lower for w in ["pdf", "monograph", "paper", "document", "upload", "file", "text", "book"]):
            label = "📄 Upload Formulation PDF"
            if lang in ["hi", "hindi"]: label = "📄 फॉर्मूलेशन दस्तावेज़ अपलोड करें"
            elif lang in ["or", "odia", "oriya"]: label = "📄 ଦସ୍ତାବିଜ୍ ଅପଲୋଡ୍ କରନ୍ତୁ"
            actions.append(SuggestedAction(
                label=label,
                target_screen="upload"
            ))

        # Compliance roadmap recommendation
        label_map = "🗺️ View Compliance Roadmap"
        if lang in ["hi", "hindi"]: label_map = "🗺️ अनुपालन रोडमैप देखें"
        elif lang in ["or", "odia", "oriya"]: label_map = "🗺️ ଅନୁପାଳନ ରୋଡମ୍ୟାପ୍ ଦେଖନ୍ତୁ"
        actions.append(SuggestedAction(
            label=label_map,
            target_screen="roadmap"
        ))

        # Evidence graph recommendation
        label_graph = "🌐 Explore Evidence Graph"
        if lang in ["hi", "hindi"]: label_graph = "🌐 साक्ष्य ग्राफ एक्सप्लोर करें"
        elif lang in ["or", "odia", "oriya"]: label_graph = "🌐 ପ୍ରମାଣ ଗ୍ରାଫ୍ ଦେଖନ୍ତୁ"
        actions.append(SuggestedAction(
            label=label_graph,
            target_screen="graph"
        ))

        return actions[:3]

    def _generate_domain_response(self, message: str, lang: str) -> ChatResponse:
        m_lower = message.lower()
        actions = self._generate_suggested_actions(message, lang)

        # ----------------------------------------------------
        # HINDI RESPONSES
        # ----------------------------------------------------
        if lang in ["hi", "hindi"]:
            if any(k in m_lower for k in ["अश्वगंधा", "ashwagandha", "तुलसी", "tulsi", "नीम", "neem", "हल्दी", "turmeric", "पौधा", "जड़ी"]):
                reply = (
                    "🌿 **पारंपरिक औषधीय पौधों के लिए आईपी नियम:**\n\n"
                    "1. **पेटेंट पात्रता (धारा 3(p)):** भारतीय पेटेंट अधिनियम की धारा 3(p) के तहत किसी ज्ञात औषधीय पौधे (जैसे अश्वगंधा या तुलसी) के ज्ञात गुणों का पेटेंट नहीं कराया जा सकता। पेटेंट प्राप्त करने के लिए आपको वैज्ञानिक रूप से सिद्ध **नया सहक्रियात्मक प्रभाव (synergistic efficacy)** या एक **नवीन निष्कर्षण विधि** दर्शानी होगी।\n\n"
                    "2. **राष्ट्रीय जैव विविधता प्राधिकरण (NBA):** भारत से प्राप्त जैविक संसाधनों के उपयोग के लिए आईपीआर आवेदन करने से पहले NBA की पूर्व स्वीकृति अनिवार्य है (धारा 6)।\n\n"
                    "💡 **अनुशंसित ऐप सुविधा:**\n"
                    "• नीचे दिए गए **'धारा 3(p) विश्लेषण चलाएं'** बटन पर टैप करें ताकि आप सीधे जांच कार्यक्षेत्र में इसका कानूनी मूल्यांकन कर सकें!\n"
                    "• चरणबद्ध फाइलिंग समयसीमा के लिए **'अनुपालन रोडमैप'** देखें।"
                )
            elif any(k in m_lower for k in ["लाइसेंस", "license", "158b", "आयुष", "ayush", "दवा"]):
                reply = (
                    "📜 **आयुष औषधि निर्माण लाइसेंस (नियम 158B):**\n\n"
                    "औषधि एवं प्रसाधन सामग्री नियम, 1945 के नियम 158B के तहत आयुर्वेदिक फॉर्मूलेशन को दो श्रेणियों में लाइसेंस दिया जाता है:\n"
                    "• **शास्त्रीय औषधियां (Classical Medicines):** आधिकारिक संहिताओं (चरक, सुश्रुत आदि) में उल्लिखित नुस्खे।\n"
                    "• **पेटेंट या प्रोप्राइटरी फॉर्मूलेशन:** नए घटक या अनुपात, जिसके लिए सुरक्षा और प्रभावकारिता का वैज्ञानिक डेटा आवश्यक है।\n\n"
                    "💡 **अनुशंसित ऐप सुविधा:**\n"
                    "• अपने फॉर्मूलेशन का तकनीकी विवरण हमारे **'दस्तावेज़ आसूचना'** अनुभाग में अपलोड करें और इंडेक्स करें!"
                )
            else:
                reply = (
                    "🙏 नमस्ते! मैं **आयुर्शक्ति सहायक** हूँ — आपका आयुर्वेदिक और बौद्धिक संपदा सलाहकार।\n\n"
                    "मैं आपके आयुर्वेदिक फॉर्मूलेशन, जड़ी-बूटियों, पेटेंट धारा 3(p), टीकेडीएल (TKDL) पूर्व कला, और जैव विविधता अनुपालन के प्रश्नों को हल करने में मदद करता हूँ।\n\n"
                    "💡 **आप इन सुविधाओं का उपयोग कर सकते हैं:**\n"
                    "1. **जांच कार्यक्षेत्र:** अपने आयुर्वेदिक फॉर्मूलेशन का बहु-डोमेन कानूनी विश्लेषण करें।\n"
                    "2. **साक्ष्य ग्राफ:** वैधानिक नियमों और जोखिमों का इंटरैक्टिव संबंध देखें।\n"
                    "3. **अनुपालन रोडमैप:** अनुसंधान से फाइलिंग तक की समयसीमा देखें।"
                )
            return ChatResponse(
                reply=reply,
                suggested_actions=actions,
                references=["Patents Act 1970 Sec 3(p)", "Biological Diversity Act 2002 Sec 6", "Drugs & Cosmetics Rules Rule 158B"]
            )

        # ----------------------------------------------------
        # ODIA RESPONSES
        # ----------------------------------------------------
        elif lang in ["or", "odia", "oriya"]:
            if any(k in m_lower for k in ["ଅଶ୍ୱଗନ୍ଧା", "ashwagandha", "ତୁଳସୀ", "tulsi", "ନିମ୍ବ", "neem", "ହଳଦୀ", "turmeric", "ଉଦ୍ଭିଦ", "plant"]):
                reply = (
                    "🌿 **ପାରମ୍ପରିକ ଔଷଧୀୟ ଉଦ୍ଭିଦ ପାଇଁ IP ନିୟମାବଳୀ:**\n\n"
                    "1. **ପେଟେଣ୍ଟ ନିୟମ (ଧାରା 3(p)):** ଭାରତୀୟ ପେଟେଣ୍ଟ ଅଧିନିୟମର ଧାରା 3(p) ଅନୁଯାୟୀ କୌଣସି ପାରମ୍ପରିକ ଔଷଧୀୟ ଉଦ୍ଭିଦ (ଯଥା ଅଶ୍ୱଗନ୍ଧା ବା ତୁଳସୀ) ର ସାଧାରଣ ବ୍ୟବହାରକୁ ପେଟେଣ୍ଟ ମିଳିପାରିବ ନାହିଁ। ଆପଣଙ୍କୁ ବୈଜ୍ଞାନିକ ପ୍ରମାଣ ଦ୍ୱାରା ନୂତନ **ସିନର୍ଜିଷ୍ଟିକ୍ ପ୍ରଭାବ** କିମ୍ବା ଏକ **ନୂତନ ନିଷ୍କାସନ ପ୍ରଣାଳୀ** ପ୍ରମାଣ କରିବାକୁ ହେବ।\n\n"
                    "2. **ଜାତୀୟ ଜୈବ ବିବିଧତା ପ୍ରାଧିକରଣ (NBA):** ଭାରତୀୟ ଜୈବିକ ଉତ୍ସ ଉପରେ ଆଧାରିତ କୌଣସି IP ଆବେଦନ ପାଇଁ NBA ର ପୂର୍ବ ଅନୁମୋଦନ ବାଧ୍ୟତାମୂଳକ (ଧାରା 6)।\n\n"
                    "💡 **ପରାମର୍ଶିତ ଆପ୍ ବୈଶିଷ୍ଟ୍ୟ:**\n"
                    "• ସିଧାସଳଖ ଆଇନଗତ ବିଶ୍ଳେଷଣ କରିବା ପାଇଁ ତଳେ ଥିବା **'ଧାରା 3(p) ବିଶ୍ଳେଷଣ କରନ୍ତୁ'** ବଟନ୍ ଚୟନ କରନ୍ତୁ!\n"
                    "• ପର୍ଯ୍ୟାୟକ୍ରମେ ଫାଇଲିଂ ପ୍ରକ୍ରିୟା ପାଇଁ **'ଅନୁପାଳନ ରୋଡମ୍ୟାପ୍'** ଦେଖନ୍ତୁ।"
                )
            else:
                reply = (
                    "🙏 ନମସ୍କାର! ମୁଁ **ଆୟୁରଶକ୍ତି ସହାୟକ** — ଆପଣଙ୍କର ଆୟୁର୍ବେଦିକ ଏବଂ ବୌଦ୍ଧିକ ସମ୍ପତ୍ତି (IP) ପରାମର୍ଶଦାତା।\n\n"
                    "ମୁଁ ଆପଣଙ୍କ ଆୟୁର୍ବେଦିକ ଫର୍ମୁଲେସନ୍, ପାରମ୍ପରିକ ଉଦ୍ଭିଦ, ପେଟେଣ୍ଟ ଧାରା 3(p), TKDL ପୂର୍ବ କଳା ଏବଂ ଜୈବ ବିବିଧତା ଅନୁପାଳନ ସମ୍ବନ୍ଧୀୟ ସମସ୍ତ ସନ୍ଦେହ ଦୂର କରିବାରେ ସାହାଯ୍ୟ କରେ।\n\n"
                    "💡 **ଏହି ଆପ୍ ର ଉପଯୋଗୀ ବୈଶିଷ୍ଟ୍ୟଗୁଡ଼ିକ:**\n"
                    "1. **ଅନୁସନ୍ଧାନ କାର୍ଯ୍ୟକ୍ଷେତ୍ର:** ଆପଣଙ୍କ ଫର୍ମୁଲେସନ୍ ର ପେଟେଣ୍ଟ ଯୋଗ୍ୟତା ପରୀକ୍ଷା କରନ୍ତୁ।\n"
                    "2. **ପ୍ରମାଣ ଗ୍ରାଫ୍:** ଆଇନଗତ ନିୟମାବଳୀର ଇଣ୍ଟରାକ୍ଟିଭ୍ ସଂଯୋଗ ଦେଖନ୍ତୁ।\n"
                    "3. **ଅନୁପାଳନ ରୋଡମ୍ୟାପ୍:** ଗବେଷଣାରୁ ଆବେଦନ ପର୍ଯ୍ୟନ୍ତ ନିୟାମକ ପଦକ୍ଷେପ ଦେଖନ୍ତୁ।"
                )
            return ChatResponse(
                reply=reply,
                suggested_actions=actions,
                references=["Patents Act 1970 Sec 3(p)", "Biological Diversity Act 2002 Sec 6"]
            )

        # ----------------------------------------------------
        # ENGLISH RESPONSES
        # ----------------------------------------------------
        if any(k in m_lower for k in ["ashwagandha", "tulsi", "neem", "turmeric", "curcumin", "herb", "plant", "ayurvedic", "formulation"]):
            reply = (
                "🌿 **Ayurvedic Formulation & IP Guidelines:**\n\n"
                "1. **Patentability Barriers (Section 3(p)):** Under Section 3(p) of the Indian Patents Act, traditional knowledge or mere admixtures of known herb properties cannot be patented. To secure a patent, you must demonstrate a **statistically significant synergistic effect** between components or a **novel, proprietary extraction mechanism**.\n\n"
                "2. **TKDL Prior Art Checking:** Ensure the herb combinations are not already disclosed in the Traditional Knowledge Digital Library (TKDL).\n\n"
                "3. **Biological Diversity Act (NBA):** Prior approval from the National Biodiversity Authority (Form 3) is legally mandated before applying for any IPR based on Indian bio-resources.\n\n"
                "💡 **Recommended App Actions:**\n"
                "• Tap **'Run Section 3(p) Analysis'** below to instantly screen this formulation in the Investigation Workspace!\n"
                "• Check the **'Compliance Roadmap'** for step-by-step statutory filing guidance."
            )
        elif any(k in m_lower for k in ["158b", "rule", "license", "licensing", "ayush", "drug", "manufacture"]):
            reply = (
                "📜 **Ayush Manufacturing & Licensing (Rule 158B):**\n\n"
                "Under Rule 158B of the Drugs and Cosmetics Rules, 1945, requirements differ based on formulation classification:\n"
                "• **Classical Ayurvedic Medicines:** Formulations documented in recognized Ayurvedic authoritative texts require textual citation and standard pharmacopoeial quality tests.\n"
                "• **Patent / Proprietary Ayurvedic Medicines:** New combinations or indications require pilot clinical trials and acute toxicity data to obtain State Ayush Licensing Authority approval.\n\n"
                "💡 **Recommended App Action:**\n"
                "• Tap **'Upload Formulation PDF'** below to ingest and index your formulation monograph in our Document Intelligence engine!"
            )
        elif any(k in m_lower for k in ["nba", "biodiversity", "abs", "benefit sharing", "sbb"]):
            reply = (
                "⚖️ **Biological Diversity & ABS Mandates:**\n\n"
                "• **Section 6 Approval:** You must submit Form 3 to the National Biodiversity Authority (NBA) before filing a patent application.\n"
                "• **Access & Benefit Sharing (ABS):** Commercial utilization triggers an obligation to enter into an ABS agreement, allocating 0.1%–0.5% of ex-factory sale proceeds to local Biodiversity Management Committees (BMCs).\n\n"
                "💡 **Recommended App Action:**\n"
                "• Open the **'Evidence Graph'** below to visually trace how the Biodiversity Act connects to your formulation!"
            )
        else:
            reply = (
                "🌿 **Welcome to AyurSakti Intelligence Assistant!**\n\n"
                "I am your dedicated conversational guide for Ayurvedic IP, traditional knowledge protection, and regulatory clearance.\n\n"
                "You can ask me about:\n"
                "• Can I patent my herbal or Ayurvedic formulation?\n"
                "• Overcoming Section 3(p) objections and proving synergy\n"
                "• Navigating TKDL prior-art citations\n"
                "• National Biodiversity Authority (NBA) approval process\n"
                "• Ayush licensing requirements under Rule 158B\n\n"
                "💡 **Explore Key App Features:**\n"
                "Use the action chips below to launch an investigation, view the compliance roadmap, or index regulatory documents!"
            )

        from ..pipeline.domain_classifier import domain_classifier
        classification = domain_classifier.classify(message)
        return ChatResponse(
            reply=reply,
            suggested_actions=actions,
            references=["Indian Patents Act, 1970 (Sec 3p)", "Biological Diversity Act, 2002 (Sec 6)", "Drugs & Cosmetics Rules (Rule 158B)"],
            domain=classification.get("primary_domain", "Ayurveda"),
            domains=classification.get("all_detected", ["Ayurveda"])
        )

chat_service = ChatService()
