from datetime import datetime
import uuid
from .models import (
    QueryResponse, EvidenceItem, SourceItem, CitationItem,
    EvidenceGraphResponse, GraphNode, GraphEdge,
    ComplianceRoadmapResponse, RoadmapStep,
    InvestigationDetail, DashboardStats
)

DEMO_INVESTIGATION_ID = "inv_ayurveda_001"

demo_source_1 = SourceItem(
    document_name="Biological Diversity Act, 2002",
    authority="National Biodiversity Authority",
    jurisdiction="India",
    section="Section 3 & 4",
    page="12",
    version="2002",
    effective_date="2002-10-01",
    source_url="http://nbaindia.org/content/25/19/1/act.html",
    content="Prior approval of NBA is required before applying for intellectual property rights involving biological resources obtained from India."
)

demo_source_2 = SourceItem(
    document_name="Patents Act, 1970",
    authority="Indian Patent Office",
    jurisdiction="India",
    section="Section 3(p)",
    page="18",
    version="Amended 2005",
    effective_date="1970-09-19",
    source_url="https://ipindia.gov.in/patents-act-1970.htm",
    content="An invention which in effect, is traditional knowledge or which is an aggregation or duplication of known properties of traditionally known component or components is not patentable."
)

demo_source_3 = SourceItem(
    document_name="Traditional Knowledge Digital Library (TKDL) Guidelines",
    authority="CSIR & AYUSH",
    jurisdiction="India",
    section="General",
    page="1",
    version="2023",
    content="The TKDL acts as a prior art database to prevent misappropriation of Indian traditional knowledge."
)

demo_source_4 = SourceItem(
    document_name="Guidelines on Access and Benefit Sharing (ABS)",
    authority="National Biodiversity Authority",
    jurisdiction="India",
    section="Regulation 2",
    version="2014",
    content="Benefit sharing obligation applies to persons who access biological resources for commercial utilization or bio-survey and bio-utilization."
)

demo_evidence = [
    EvidenceItem(
        id="ev_001", title="NBA Approval Requirement", summary="Must obtain prior approval from the National Biodiversity Authority (NBA) before filing any IP application based on Indian biological resources.", source=demo_source_1, relevance_score=0.95
    ),
    EvidenceItem(
        id="ev_002", title="Traditional Knowledge Patentability Exclusion", summary="Section 3(p) of the Patents Act prevents patenting of traditional knowledge unless there is a substantial synergistic effect or novel process.", source=demo_source_2, relevance_score=0.92
    ),
    EvidenceItem(
        id="ev_003", title="TKDL Prior Art Verification", summary="The formulation may face objections if the plant's medicinal properties are already documented in the Traditional Knowledge Digital Library.", source=demo_source_3, relevance_score=0.88
    ),
    EvidenceItem(
        id="ev_004", title="Access and Benefit Sharing (ABS)", summary="Commercial utilization of the formulation will trigger ABS obligations under the 2014 Guidelines.", source=demo_source_4, relevance_score=0.85
    )
]

demo_citations = [
    CitationItem(text="Prior approval of NBA is required before applying for IPR", source_id="ev_001"),
    CitationItem(text="Traditional knowledge is excluded from patentability under Section 3(p)", source_id="ev_002"),
    CitationItem(text="Benefit sharing applies to commercial utilization", source_id="ev_004")
]

demo_query_response = QueryResponse(
    answer="Based on your query regarding an Ayurvedic formulation using a traditional medicinal plant, there are three primary legal compliance and IP considerations in India.\\n\\nFirst, under the **Biological Diversity Act, 2002**, you must obtain prior approval from the National Biodiversity Authority (NBA) before applying for any intellectual property rights (such as patents) anywhere in the world for an invention based on biological resources obtained from India (Section 6).\\n\\nSecond, under the **Patents Act, 1970**, Section 3(p) specifically bars the patenting of traditional knowledge or mere aggregations of known properties of traditionally known components. To secure a patent, you must demonstrate a substantial synergistic effect or a novel extraction/formulation process that goes beyond traditional practices.\\n\\nThird, you must comply with **Access and Benefit Sharing (ABS)** guidelines, meaning you will likely need to share commercial benefits with the local communities from whom the knowledge or resources were sourced.\\n\\nBefore filing for IP, it is highly recommended to cross-check the traditional medicinal plant in the Traditional Knowledge Digital Library (TKDL) to anticipate potential prior art objections.",
    confidence=0.82,
    evidence=demo_evidence,
    domains=["Patent", "Traditional Knowledge", "Biodiversity/ABS"],
    risks=["Rejection of patent under Section 3(p)", "Penalties for not obtaining NBA approval prior to IP filing", "ABS liabilities on commercialization"],
    actions=["File Form 3 with NBA for IP application approval", "Conduct a freedom-to-operate and prior-art search including TKDL", "Document synergistic effects of the specific formulation to overcome Sec 3(p) objections"],
    citations=demo_citations,
    abstained=False,
    disclaimer="DEMO/SAMPLE DATA: This information is generated by AI and is for general guidance only. It does not constitute legal advice."
)

demo_graph = EvidenceGraphResponse(
    nodes=[
        GraphNode(id="ayurvedic_formulation", label="Ayurvedic Formulation", type="Query"),
        GraphNode(id="nba_approval", label="NBA Approval (Form 3)", type="Requirement"),
        GraphNode(id="sec_3p", label="Section 3(p) Patent Act", type="Risk"),
        GraphNode(id="tkdl", label="TKDL Database", type="Database"),
        GraphNode(id="abs", label="Benefit Sharing (ABS)", type="Compliance")
    ],
    edges=[
        GraphEdge(source="ayurvedic_formulation", target="nba_approval", relation="requires"),
        GraphEdge(source="ayurvedic_formulation", target="sec_3p", relation="faces_rejection_risk"),
        GraphEdge(source="sec_3p", target="tkdl", relation="verified_against"),
        GraphEdge(source="ayurvedic_formulation", target="abs", relation="triggers")
    ]
)

demo_roadmap = ComplianceRoadmapResponse(
    steps=[
        RoadmapStep(id="step1", title="Initial Assessment & Prior Art Search", description="Search TKDL and patent databases for existing knowledge on the medicinal plant.", status="completed", duration="1-2 weeks"),
        RoadmapStep(id="step2", title="Formulation Efficacy Study", description="Document synergistic effects that differentiate the formulation from traditional knowledge.", status="in_progress", duration="2-3 months"),
        RoadmapStep(id="step3", title="NBA Application (Form 3)", description="Apply to the National Biodiversity Authority for permission to file an IP application.", status="pending", duration="3-6 months"),
        RoadmapStep(id="step4", title="Patent Drafting", description="Draft patent focusing on novel processes and synergy to avoid Section 3(p) objections.", status="pending", duration="1 month"),
        RoadmapStep(id="step5", title="Patent Filing", description="File patent with Indian Patent Office once NBA approval is conditionally secured.", status="pending", duration="1 day"),
        RoadmapStep(id="step6", title="ABS Agreement Formulation", description="Draft and sign benefit-sharing agreements with relevant state biodiversity boards/communities.", status="pending", duration="2-4 months")
    ]
)

demo_query_response_hi = QueryResponse(
    answer="पारंपरिक औषधीय पौधे का उपयोग करने वाले आयुर्वेदिक फॉर्मूलेशन के संबंध में आपके प्रश्न के आधार पर, भारत में तीन प्राथमिक कानूनी अनुपालन और बौद्धिक संपदा (IP) विचार हैं:\n\nपहला, **जैविक विविधता अधिनियम, 2002** के तहत, भारत से प्राप्त जैविक संसाधनों पर आधारित किसी भी आविष्कार के लिए विश्व में कहीं भी बौद्धिक संपदा अधिकार (जैसे पेटेंट) आवेदन करने से पहले राष्ट्रीय जैव विविधता प्राधिकरण (NBA) से पूर्व अनुमति प्राप्त करना अनिवार्य है (धारा 6)।\n\nदूसरा, **भारतीय पेटेंट अधिनियम, 1970** की धारा 3(p) स्पष्ट रूप से पारंपरिक ज्ञान या ज्ञात घटकों के ज्ञात गुणों के मात्र संचय के पेटेंट पर रोक लगाती है। पेटेंट प्राप्त करने के लिए, आपको पारंपरिक प्रथाओं से परे पर्याप्त सहक्रियात्मक प्रभाव (synergistic effect) या एक नई निष्कर्षण प्रक्रिया सिद्ध करनी होगी।\n\nतीसरा, आपको **पहुंच और लाभ साझाकरण (ABS)** दिशानिर्देशों का पालन करना होगा, जिसका अर्थ है कि आपको स्थानीय समुदायों के साथ वाणिज्यिक लाभ साझा करने की आवश्यकता होगी।\n\nपेटेंट आवेदन करने से पहले, पूर्व कला आपत्तियों से बचने के लिए पारंपरिक ज्ञान डिजिटल लाइब्रेरी (TKDL) में पौधे की जांच करने की सिफारिश की जाती है।",
    confidence=0.87,
    evidence=demo_evidence,
    domains=["पेटेंट कानून", "पारंपरिक ज्ञान", "जैव विविधता / ABS", "आयुर्वेद नियामक"],
    risks=[
        "उच्च जोखिम: धारा 3(p) के तहत पेटेंट खारिज होने का जोखिम (TKDL पूर्व कला)",
        "उच्च जोखिम: आईपी आवेदन से पूर्व NBA अनुमति न लेने पर दंडात्मक कार्रवाई",
        "मध्यम जोखिम: वाणिज्यिकीकरण पर लाभ साझाकरण (ABS) की देयता"
    ],
    actions=[
        "आईपी आवेदन की पूर्व स्वीकृति के लिए NBA को फॉर्म 3 जमा करें",
        "TKDL और पेटेंट डेटाबेस में विस्तृत पूर्व कला खोज करें",
        "धारा 3(p) आपत्तियों को दूर करने के लिए फॉर्मूलेशन के सहक्रियात्मक प्रभाव का वैज्ञानिक दस्तावेज तैयार करें"
    ],
    citations=demo_citations,
    abstained=False,
    disclaimer="यह जानकारी एआई द्वारा शोध सहायता के लिए उत्पन्न की गई है। यह औपचारिक कानूनी सलाह नहीं है।"
)

demo_query_response_or = QueryResponse(
    answer="ପାରମ୍ପରିକ ଔଷଧୀୟ ଉଦ୍ଭିଦ ବ୍ୟବହାର କରି ଏକ ଆୟୁର୍ବେଦିକ ଫର୍ମୁଲେସନ୍ ସମ୍ବନ୍ଧରେ, ଭାରତରେ ତିନୋଟି ପ୍ରମୁଖ ଆଇନଗତ ଅନୁପାଳନ ଏବଂ ବୌଦ୍ଧିକ ସମ୍ପତ୍ତି (IP) ନିୟମାବଳୀ ରହିଛି:\n\nପ୍ରଥମତଃ, **ଜୈବ ବିବିଧତା ଅଧିନିୟମ, 2002** ଅନୁଯାୟୀ, ଭାରତରୁ ପ୍ରାପ୍ତ ଜୈବିକ ଉତ୍ସ ଉପରେ ଆଧାରିତ କୌଣସି ଅନୁସନ୍ଧାନ କିମ୍ବା ଆବିଷ୍କାର ପାଇଁ ପେଟେଣ୍ଟ ଆବେଦନ କରିବା ପୂର୍ବରୁ ଜାତୀୟ ଜୈବ ବିବିଧତା ପ୍ରାଧିକରଣ (NBA) ର ପୂର୍ବ ଅନୁମୋଦନ ବାଧ୍ୟତାମୂଳକ (ଧାରା 6)।\n\nଦ୍ୱିତୀୟତଃ, **ଭାରତୀୟ ପେଟେଣ୍ଟ ଅଧିନିୟମ, 1970** ର ଧାରା 3(p) ଅନୁଯାୟୀ ପାରମ୍ପରିକ ଜ୍ଞାନ ବା ପୂର୍ବରୁ ଜଣାଶୁଣା ଗୁଣଗୁଡ଼ିକର ଏକତ୍ରିକରଣ ପାଇଁ ପେଟେଣ୍ଟ ପ୍ରଦାନ ନିଷିଦ୍ଧ। ପେଟେଣ୍ଟ ହାସଲ କରିବାକୁ ହେଲେ ଆପଣଙ୍କୁ ପାରମ୍ପରିକ ପଦ୍ଧତି ଠାରୁ ଊର୍ଦ୍ଧ୍ୱରେ ଏକ ନୂତନ ସିନର୍ଜିଷ୍ଟିକ୍ ପ୍ରଭାବ କିମ୍ବା ନୂତନ ନିଷ୍କାସନ ପ୍ରଣାଳୀ ପ୍ରମାଣ କରିବାକୁ ହେବ।\n\nତୃତୀୟତଃ, ଆପଣଙ୍କୁ **ପହଞ୍ଚ ଏବଂ ଲାଭ ଭାଗବଣ୍ଟା (ABS)** ନିୟମ ପାଳନ କରିବାକୁ ପଡ଼ିବ, ଯାହା ଅଧୀନରେ ସ୍ଥାନୀୟ ସମ୍ପ୍ରଦାୟ ସହିତ ବାଣିଜ୍ୟିକ ଲାଭ ଅଂଶୀଦାର କରିବାକୁ ହେବ।\n\nପେଟେଣ୍ଟ ଦାଖଲ କରିବା ପୂର୍ବରୁ ପାରମ୍ପରିକ ଜ୍ଞାନ ଡିଜିଟାଲ୍ ଲାଇବ୍ରେରୀ (TKDL) ରେ ଏହି ଉଦ୍ଭିଦର ଯାଞ୍ଚ କରିବାକୁ ଦୃଢ଼ ପରାମର୍ଶ ଦିଆଯାଉଛି।",
    confidence=0.87,
    evidence=demo_evidence,
    domains=["ପେଟେଣ୍ଟ ଆଇନ", "ପାରମ୍ପରିକ ଜ୍ଞାନ", "ଜୈବ ବିବିଧତା / ABS", "ଆୟୁର୍ବେଦ ନିୟାମକ"],
    risks=[
        "ଉଚ୍ଚ ବିପଦ: ଧାରା 3(p) ଅଧୀନରେ ପେଟେଣ୍ଟ ପ୍ରତ୍ୟାଖ୍ୟାନ ହେବାର ଆଶଙ୍କା (TKDL ପୂର୍ବ କଳା)",
        "ଉଚ୍ଚ ବିପଦ: IP ଆବେଦନ ପୂର୍ବରୁ NBA ଅନୁମୋଦନ ନ ପାଇବାର ଦଣ୍ଡବିଧାନ",
        "ମଧ୍ୟମ ବିପଦ: ବାଣିଜ୍ୟିକ ଉପଯୋଗ ଉପରେ ABS ଦାୟିତ୍ୱ"
    ],
    actions=[
        "IP ଆବେଦନ ଅନୁମୋଦନ ପାଇଁ NBA କୁ ଫର୍ମ 3 ଦାଖଲ କରନ୍ତୁ",
        "TKDL ଏବଂ ପେଟେଣ୍ଟ ରେଜିଷ୍ଟ୍ରିରେ ପୂର୍ବ କଳା ସର୍ଚ୍ଚ କରନ୍ତୁ",
        "ଧାରା 3(p) ଆପତ୍ତି ଦୂର କରିବା ପାଇଁ ସିନର୍ଜିଷ୍ଟିକ୍ ପ୍ରଭାବର ପ୍ରମାଣ ପ୍ରସ୍ତୁତ କରନ୍ତୁ"
    ],
    citations=demo_citations,
    abstained=False,
    disclaimer="ଏହି ସୂଚନା AI ଦ୍ୱାରା ଅନୁସନ୍ଧାନ ସହାୟତା ପାଇଁ ପ୍ରସ୍ତୁତ କରାଯାଇଛି। ଏହା ଆନୁଷ୍ଠାନିକ ଆଇନଗତ ପରାମର୍ଶ ନୁହେଁ।"
)

def get_demo_query_response(language: str = "en") -> QueryResponse:
    lang = (language or "en").lower().strip()
    if lang in ["hi", "hindi"]:
        return demo_query_response_hi
    elif lang in ["or", "odia", "oriya"]:
        return demo_query_response_or
    return demo_query_response

def get_demo_investigation(language: str = "en", custom_id: str = DEMO_INVESTIGATION_ID) -> InvestigationDetail:
    resp = get_demo_query_response(language)
    lang = (language or "en").lower().strip()
    
    query_text = "We developed an Ayurvedic formulation using a traditional medicinal plant. What IP and compliance issues should we investigate?"
    if lang in ["hi", "hindi"]:
        query_text = "हमने एक पारंपरिक औषधीय पौधे का उपयोग करके एक आयुर्वेदिक फॉर्मूलेशन विकसित किया है। हमें किन बौद्धिक संपदा और अनुपालन मुद्दों की जांच करनी चाहिए?"
    elif lang in ["or", "odia", "oriya"]:
        query_text = "ଆମେ ଏକ ପାରମ୍ପରିକ ଔଷଧୀୟ ଉଦ୍ଭିଦ ବ୍ୟବହାର କରି ଏକ ଆୟୁର୍ବେଦିକ ଫର୍ମୁଲେସନ୍ ବିକଶିତ କରିଛୁ। ଆମକୁ କେଉଁ ବୌଦ୍ଧିକ ସମ୍ପତ୍ତି ଏବଂ ଅନୁପାଳନ ସମସ୍ୟାର ତଦନ୍ତ କରିବା ଉଚିତ୍?"

    return InvestigationDetail(
        id=custom_id,
        query=query_text,
        timestamp=datetime.now(),
        domain="Patent & Biodiversity",
        status="Completed",
        response=resp,
        graph=demo_graph,
        roadmap=demo_roadmap
    )

demo_investigation = get_demo_investigation("en")

demo_dashboard_stats = DashboardStats(
    total_investigations=14,
    active_cases=3,
    documents_indexed=245,
    risk_alerts=5,
    recent_activity=[
        {"type": "investigation", "id": DEMO_INVESTIGATION_ID, "title": "Ayurvedic Formulation IP Check", "time": "2 hours ago"},
        {"type": "document", "id": "doc_102", "title": "Patents Act 1970 added to index", "time": "1 day ago"},
        {"type": "alert", "id": "alt_09", "title": "New biodiversity compliance rule detected", "time": "3 days ago"}
    ]
)
