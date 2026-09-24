from pydantic import BaseModel, Field, ConfigDict
from typing import List, Optional, Dict, Any
from datetime import datetime

# ==========================================================
# BASE QUERY & JURISDICTION MODELS
# ==========================================================

class QueryRequest(BaseModel):
    question: str
    mode: str = 'quick'
    language: str = 'en'
    persona: Optional[str] = 'startup'
    jurisdictions: Optional[List[str]] = None
    product_details: Optional[Dict[str, Any]] = None
    documents: Optional[List[str]] = None

class SourceItem(BaseModel):
    document_name: str
    authority: Optional[str] = None
    jurisdiction: Optional[str] = None
    section: Optional[str] = None
    page: Optional[str] = None
    version: Optional[str] = None
    effective_date: Optional[str] = None
    source_url: Optional[str] = None
    content: Optional[str] = None

class CitationItem(BaseModel):
    text: str
    source_id: str

class EvidenceItem(BaseModel):
    id: str
    title: str
    summary: str
    source: SourceItem
    relevance_score: float

# ==========================================================
# REGULATORY GUIDANCE & CLASSIFICATION MODELS
# ==========================================================

class ProductClassification(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    potential_category: str = Field(default="Ayurvedic Proprietary Medicine", alias="potentialCategory")
    confidence_score: float = 0.88
    legal_reasoning: str = ""
    statutory_basis: str = ""
    authority: str = "State Ayush Licensing Authority (SLA)"
    governing_rules: str = "Drugs & Cosmetics Act, 1940 (Rule 158B)"
    unresolved_questions: List[str] = []
    requires_expert_verification: bool = False

    @property
    def potentialCategory(self) -> str:
        return self.potential_category

    @property
    def statutoryBasis(self) -> str:
        return self.statutory_basis

    @property
    def governingRules(self) -> str:
        return self.governing_rules

    @property
    def confidenceScore(self) -> float:
        return self.confidence_score

    @property
    def legalReasoning(self) -> str:
        return self.legal_reasoning

class RegulatoryRequirement(BaseModel):
    id: str
    category: str = "Licensing"  # Licensing, GMP, Safety, Labelling, Claims, Export/ABS
    title: str
    description: str
    status: str = "needs_verification"  # complete, needs_verification, missing, not_applicable, insufficient_evidence
    authority: str
    source_document: str
    section: Optional[str] = None
    publication_date: Optional[str] = None
    confidence: float = 0.90
    evidence_passage: str = ""
    what_user_should_do_next: str = ""
    source_url: Optional[str] = None

class TrustReliabilityReport(BaseModel):
    evidence_found: bool = True
    authoritative_source_verified: bool = True
    source_freshness_verified: bool = True
    conflicting_sources_count: int = 0
    retrieval_confidence: float = 0.92
    evidence_coverage: float = 0.88
    answer_confidence: float = 0.90
    safe_abstention_triggered: bool = False
    abstention_reason: Optional[str] = None
    human_escalation_recommended: bool = False
    escalation_notes: Optional[str] = None

class RegulatoryAnalysisResult(BaseModel):
    required: bool = True
    status: str = "Completed"
    classification: ProductClassification = ProductClassification()
    checklist: List[RegulatoryRequirement] = []
    checklist_completion_pct: int = 0
    statutory_triggers: List[Dict[str, Any]] = []
    mandatory_forms: List[str] = []
    statutory_risks: List[str] = []
    missing_information: List[str] = []
    trust_report: TrustReliabilityReport = TrustReliabilityReport()
    confidence: float = 0.88
    evidence: List[EvidenceItem] = []
    disclaimer: str = "This information is evidence-backed regulatory intelligence derived from statutory sources and does not constitute formal legal approval."

# ==========================================================
# INTERNATIONAL REGULATORY MODELS
# ==========================================================

class InternationalComparisonDimension(BaseModel):
    dimension: str
    india_details: str
    usa_details: str
    eu_details: Optional[str] = None
    key_differences: str

class InternationalAnalysisResult(BaseModel):
    required: bool = False
    target_countries: List[str] = ["India"]
    dimensions: List[InternationalComparisonDimension] = []
    export_readiness_alerts: List[str] = []
    country_specific_cautions: Dict[str, str] = {}
    confidence: float = 0.85
    evidence: List[EvidenceItem] = []

# ==========================================================
# ROUTING & SHARED CONTEXT MODELS
# ==========================================================

class IntentRoutingResult(BaseModel):
    ip_required: bool = True
    regulatory_required: bool = True
    international_required: bool = False
    document_analysis: bool = False
    primary_intent: str = "combined_ip_regulatory"
    detected_jurisdictions: List[str] = ["India"]
    extracted_entities: Dict[str, Any] = {}

class ProductDetails(BaseModel):
    name: str = "Ayurvedic Formulation"
    type: str = "Polyherbal Formulation"
    ingredients: List[str] = []
    dosage_form: str = "Tablet / Vati"
    intended_use: str = ""
    claims: List[str] = []
    commercial_intent: bool = True

class RegulatoryEvaluateRequest(BaseModel):
    product: Optional[ProductDetails] = None
    query: Optional[str] = None
    jurisdictions: Optional[List[str]] = ["India"]

class InternationalCompareRequest(BaseModel):
    product: Optional[ProductDetails] = None
    query: Optional[str] = None
    target_countries: Optional[List[str]] = ["India", "USA"]

class ComponentConfidence(BaseModel):
    overall: float = 0.85
    ip_confidence: Optional[float] = None
    regulatory_confidence: Optional[float] = None
    international_confidence: Optional[float] = None
    abstention_flags: Dict[str, bool] = {}
    notes: List[str] = []

class SharedContext(BaseModel):
    query: str
    product: ProductDetails = ProductDetails()
    jurisdictions: List[str] = ["India"]
    routing: IntentRoutingResult = IntentRoutingResult()
    ip_findings: List[str] = []
    regulatory_findings: List[str] = []
    cross_domain_synthesis: str = ""
    component_confidence: ComponentConfidence = ComponentConfidence()
    uncertainties: List[str] = []

# ==========================================================
# QUERY & RESPONSE MODELS
# ==========================================================

class QueryResponse(BaseModel):
    answer: str
    confidence: float
    evidence: List[EvidenceItem] = []
    domains: List[str] = []
    primary_domain: Optional[str] = None
    risks: List[str] = []
    actions: List[str] = []
    citations: List[CitationItem] = []
    citation_verified: bool = True
    abstained: bool = False
    disclaimer: str = "This information is generated by AI and is for general guidance only. It does not constitute legal advice."
    # Extended unified intelligence fields
    ip_findings: Optional[List[str]] = []
    regulatory_analysis: Optional[RegulatoryAnalysisResult] = None
    international_analysis: Optional[InternationalAnalysisResult] = None
    component_confidence: Optional[ComponentConfidence] = None
    cross_domain_synthesis: Optional[str] = None
    shared_context: Optional[Dict[str, Any]] = None

class GraphNode(BaseModel):
    id: str
    label: str
    type: str
    group: Optional[str] = None
    color: Optional[str] = None

class GraphEdge(BaseModel):
    source: str
    target: str
    relation: str

class EvidenceGraphResponse(BaseModel):
    nodes: List[GraphNode]
    edges: List[GraphEdge]

class RoadmapStep(BaseModel):
    id: str
    title: str
    description: str
    status: str = "pending"
    duration: Optional[str] = None
    authority: Optional[str] = None
    form_required: Optional[str] = None
    evidence_source: Optional[str] = None

class ComplianceRoadmapResponse(BaseModel):
    steps: List[RoadmapStep]

class InvestigationSummary(BaseModel):
    id: str
    query: str
    timestamp: datetime
    domain: str
    status: str

class InvestigationDetail(InvestigationSummary):
    response: QueryResponse
    graph: EvidenceGraphResponse
    roadmap: ComplianceRoadmapResponse
    regulatory_analysis: Optional[RegulatoryAnalysisResult] = None
    international_analysis: Optional[InternationalAnalysisResult] = None
    shared_context: Optional[Dict[str, Any]] = None

class DocumentInfo(BaseModel):
    id: str
    filename: str
    upload_date: str
    status: str
    size: int
    pages: Optional[int] = 0
    chunks: Optional[int] = 0

class DocumentUploadResponse(BaseModel):
    status: str
    message: str
    document_id: str
    filename: Optional[str] = None
    size: Optional[int] = None
    pages: Optional[int] = None
    chunks: Optional[int] = None
    extracted_entities: Optional[Dict[str, Any]] = None
    claim_risks: Optional[List[Dict[str, Any]]] = None

class DashboardStats(BaseModel):
    total_investigations: int
    active_cases: int
    documents_indexed: int
    risk_alerts: int
    recent_activity: List[Dict[str, Any]]

class HealthResponse(BaseModel):
    status: str
    mode: str
    version: str
    pipeline_ready: bool

class SuggestedAction(BaseModel):
    label: str
    target_screen: str  # "investigate", "upload", "roadmap", "graph", "evidence"
    payload: Optional[str] = None

class ChatMessage(BaseModel):
    id: str
    sender: str  # "user" or "assistant"
    text: str
    timestamp: str
    actions: List[SuggestedAction] = []
    domain: Optional[str] = None
    domains: List[str] = []

class ChatMessageRequest(BaseModel):
    message: str
    history: List[ChatMessage] = []
    language: str = "en"
    persona: Optional[str] = "startup"

class ChatResponse(BaseModel):
    reply: str
    suggested_actions: List[SuggestedAction] = []
    references: List[str] = []
    domain: Optional[str] = None
    domains: List[str] = []

class PatentRecord(BaseModel):
    patent_number: str
    title: str
    applicant: str
    status: str
    filing_date: Optional[str] = None
    jurisdiction: Optional[str] = "India"
    ipc_class: Optional[str] = None
    abstract: Optional[str] = None

class BotanicalInfo(BaseModel):
    name: str
    scientific_name: str
    traditional_uses: str
    classical_texts: str
    sec_3p_risk: str

class PriorArtSearchRequest(BaseModel):
    query: str

class PriorArtSearchResponse(BaseModel):
    query: str
    total_found: int
    detected_botanicals: List[BotanicalInfo] = []
    patents: List[PatentRecord] = []
    patentability_barriers: List[Dict[str, str]] = []
    conclusion_status: str = "Search completed"
    disclaimer: str = "This patent search is preliminary and for guidance only. It does not constitute formal legal certification."

class RegulationUpdate(BaseModel):
    id: Optional[str] = None
    source_name: str
    title: str
    notification_number: str
    category: str
    summary: str
    source_url: str
    issued_date: str
    status: Optional[str] = "In Force"

class CraftCombineRequest(BaseModel):
    ingredients: List[str]

class CraftCombineResponse(BaseModel):
    discovered: bool
    title: str
    sanskrit_name: Optional[str] = None
    ingredients: List[str] = []
    status: str
    classical_source: Optional[str] = None
    tkdl_status: str
    patentability_risk_pct: int
    synergy_score_pct: int
    therapeutic_category: str
    statutory_rationale: str
    statutory_requirements: List[str] = []
    suggested_queries: List[str] = []
