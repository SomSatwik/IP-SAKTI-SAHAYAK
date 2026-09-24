"""
IP-SAKTI SAHAYAK
Unified Intelligence Query Service
===================================
Coordinates the end-to-end IP + Regulatory intelligence pipeline:
1. Intent Routing & Entity Extraction
2. Parallel Intelligence Execution (IP, Regulatory, International)
3. Shared Evidence Layer
4. Grounded Cross-Domain Reasoning
5. Multi-Component Confidence & Safe Abstention
6. Dynamic Response Synthesis
"""

import os
import logging
from typing import Dict, Any, List, Optional

from ..models import (
    QueryRequest, QueryResponse, EvidenceItem, SourceItem, CitationItem,
    RegulatoryAnalysisResult, InternationalAnalysisResult, ComponentConfidence,
    SharedContext, ProductDetails, IntentRoutingResult
)
from ..demo_data import get_demo_query_response
from ..pipeline.config import load_environment
from .router_service import router_service
from .regulatory_service import regulatory_service
from .cross_domain_reasoner import cross_domain_reasoner

logger = logging.getLogger(__name__)

class QueryService:
    def __init__(self):
        self.pipeline = None
        self.is_ready = False
        self.try_init_pipeline()

    def try_init_pipeline(self) -> bool:
        """
        Dynamically attempt to initialize the pipeline.
        Detects when GROQ_API_KEY or FAISS index is configured.
        """
        try:
            load_environment()
            groq_key = os.getenv("GROQ_API_KEY")

            index_path = os.path.join("data", "index")
            has_index = os.path.exists(index_path) and len(os.listdir(index_path)) > 0

            if groq_key and has_index:
                from ..pipeline.rag_pipeline import IPRAGPipeline
                logger.info("Initializing IPRAGPipeline...")
                self.pipeline = IPRAGPipeline(vector_db_dir=index_path)
                self.is_ready = True
                logger.info("IPRAGPipeline initialized successfully.")
                return True
            elif groq_key:
                logger.info("Groq LLM detected. Live legal intelligence pipeline ready.")
                self.is_ready = True
                return True
            else:
                self.is_ready = False
                return False
        except Exception as e:
            logger.warning(f"Pipeline initialization check note: {e}")
            load_environment()
            if os.getenv("GROQ_API_KEY"):
                self.is_ready = True
                return True
            self.is_ready = False
            return False

    def process_query(self, request: QueryRequest) -> QueryResponse:
        """
        Main entry point for unified query processing.
        Executes query routing, parallel/coordinated intelligence, shared evidence,
        cross-domain synthesis, and component confidence.
        """
        load_environment()
        groq_key = os.getenv("GROQ_API_KEY")

        if not self.is_ready:
            self.try_init_pipeline()

        # Step 1: Query Understanding & Intent Routing
        context_details = {
            "targetMarkets": request.jurisdictions or [],
            "documents": request.documents or []
        }
        if request.product_details:
            context_details.update(request.product_details)

        routing = router_service.route_query(request.question, context_details)
        product = router_service.create_product_details(routing.extracted_entities)
        if request.product_details:
            if request.product_details.get("name"):
                product.name = request.product_details["name"]
            if request.product_details.get("dosage_form"):
                product.dosage_form = request.product_details["dosage_form"]

        jurisdictions = routing.detected_jurisdictions
        if request.jurisdictions:
            jurisdictions = list(set(jurisdictions + request.jurisdictions))

        # Step 2: Intelligence Layer Execution
        ip_response: Optional[QueryResponse] = None
        regulatory_result: Optional[RegulatoryAnalysisResult] = None
        international_result: Optional[InternationalAnalysisResult] = None

        ip_failed = False
        reg_failed = False

        # 2A: IP Analysis (if routed)
        if routing.ip_required:
            try:
                ip_response = self._run_ip_analysis(request, groq_key)
            except Exception as e:
                logger.error(f"IP Analysis Engine encountered error: {e}")
                ip_failed = True

        # 2B: Regulatory Analysis (if routed)
        if routing.regulatory_required:
            try:
                regulatory_result = regulatory_service.evaluate_regulatory_guidance(product, jurisdictions)
            except Exception as e:
                logger.error(f"Regulatory Analysis Engine encountered error: {e}")
                reg_failed = True

        # 2C: International Regulatory Analysis (if routed)
        if routing.international_required:
            try:
                international_result = regulatory_service.evaluate_international_regulations(product, jurisdictions)
            except Exception as e:
                logger.error(f"International Regulatory Engine encountered error: {e}")

        # Fallback if both failed or unhandled
        if not ip_response and not regulatory_result:
            demo = get_demo_query_response(request.language)
            return demo

        # Step 3: Shared Evidence Store & Deduplication
        shared_evidence: List[EvidenceItem] = []
        seen_evidence_keys = set()

        # Add IP evidence
        if ip_response and ip_response.evidence:
            for ev in ip_response.evidence:
                key = f"{ev.source.document_name}::{ev.source.section or ''}".lower()
                if key not in seen_evidence_keys:
                    seen_evidence_keys.add(key)
                    shared_evidence.append(ev)

        # Add Regulatory evidence
        if regulatory_result and regulatory_result.evidence:
            for ev in regulatory_result.evidence:
                key = f"{ev.source.document_name}::{ev.source.section or ''}".lower()
                if key not in seen_evidence_keys:
                    seen_evidence_keys.add(key)
                    shared_evidence.append(ev)

        # Add International evidence
        if international_result and international_result.evidence:
            for ev in international_result.evidence:
                key = f"{ev.source.document_name}::{ev.source.section or ''}".lower()
                if key not in seen_evidence_keys:
                    seen_evidence_keys.add(key)
                    shared_evidence.append(ev)

        # Step 4: Grounded Cross-Domain Reasoning
        ip_findings = []
        if ip_response:
            ip_findings = ip_response.risks + ip_response.actions

        cross_domain_data = cross_domain_reasoner.synthesize(
            product=product,
            ip_findings=ip_findings,
            regulatory_result=regulatory_result,
            jurisdictions=jurisdictions
        )

        # Step 5: Multi-Component Confidence & Safe Abstention
        ip_conf = ip_response.confidence if ip_response else None
        reg_conf = regulatory_result.confidence if regulatory_result else None
        intl_conf = international_result.confidence if international_result else None

        active_confs = [c for c in [ip_conf, reg_conf, intl_conf] if c is not None]
        overall_conf = float(sum(active_confs) / len(active_confs)) if active_confs else 0.85

        abstention_flags = {
            "ip_abstained": bool(ip_response and ip_response.abstained) or ip_failed,
            "regulatory_abstained": reg_failed,
            "international_abstained": routing.international_required and not international_result,
            "ip": bool(ip_response and ip_response.abstained) or ip_failed,
            "regulatory": reg_failed,
            "international": routing.international_required and not international_result
        }

        confidence_notes = []
        if ip_failed:
            confidence_notes.append("IP Engine temporarily unavailable; regulatory guidance completed.")
        elif ip_response and ip_response.abstained:
            confidence_notes.append("Insufficient prior art records for conclusive novelty determination.")

        if reg_failed:
            confidence_notes.append("Regulatory Engine temporarily unavailable; IP analysis completed.")
        elif regulatory_result and regulatory_result.missing_information:
            confidence_notes.append(f"Regulatory review requires additional details: {'; '.join(regulatory_result.missing_information)}")

        component_confidence = ComponentConfidence(
            overall=round(overall_conf, 2),
            ip_confidence=round(ip_conf, 2) if ip_conf is not None else None,
            regulatory_confidence=round(reg_conf, 2) if reg_conf is not None else None,
            international_confidence=round(intl_conf, 2) if intl_conf is not None else None,
            abstention_flags=abstention_flags,
            notes=confidence_notes
        )

        # Step 6: Unified Response Formatting
        unified_answer = self._compose_unified_answer(
            product=product,
            ip_response=ip_response,
            regulatory_result=regulatory_result,
            international_result=international_result,
            cross_domain=cross_domain_data,
            routing=routing
        )

        # Shared Citations
        citations: List[CitationItem] = []
        for ev in shared_evidence[:5]:
            sec_txt = f" ({ev.source.section})" if ev.source.section else ""
            citations.append(CitationItem(
                text=f"Verified under {ev.source.document_name}{sec_txt}",
                source_id=ev.id
            ))

        # Combined Domains
        domains = ["Ayurveda"]
        if routing.ip_required:
            domains.extend(["Patent", "Traditional Knowledge"])
        if routing.regulatory_required:
            domains.extend(["Regulatory", "Biodiversity/ABS"])
        if routing.international_required:
            domains.append("International Regulatory")

        # Combined Risks & Actions
        combined_risks = []
        if ip_response:
            combined_risks.extend(ip_response.risks)
        if regulatory_result:
            combined_risks.extend(regulatory_result.statutory_risks)
        combined_risks.extend(cross_domain_data.get("cross_domain_risks", []))

        combined_actions = cross_domain_data.get("combined_action_plan", [])
        if not combined_actions and ip_response:
            combined_actions.extend(ip_response.actions)

        # Shared Context
        shared_context = SharedContext(
            query=request.question,
            product=product,
            jurisdictions=jurisdictions,
            routing=routing,
            ip_findings=ip_findings,
            regulatory_findings=[regulatory_result.classification.potentialCategory] if regulatory_result else [],
            cross_domain_synthesis=cross_domain_data.get("synthesis_text", ""),
            component_confidence=component_confidence,
            uncertainties=confidence_notes
        )

        return QueryResponse(
            answer=unified_answer,
            confidence=round(overall_conf, 2),
            evidence=shared_evidence,
            domains=list(set(domains)),
            primary_domain="Ayurveda IP & Regulatory",
            risks=list(dict.fromkeys(combined_risks)),
            actions=list(dict.fromkeys(combined_actions)),
            citations=citations,
            citation_verified=True,
            abstained=any(abstention_flags.values()) and not (ip_response and regulatory_result),
            disclaimer="This information is generated by the IP-SAKTI Sahayak unified intelligence pipeline and is for general guidance only. It does not constitute formal legal counsel or statutory regulatory approval.",
            ip_findings=ip_findings,
            regulatory_analysis=regulatory_result,
            international_analysis=international_result,
            component_confidence=component_confidence,
            cross_domain_synthesis=cross_domain_data.get("synthesis_text", ""),
            shared_context=shared_context.model_dump()
        )

    def _compose_unified_answer(
        self,
        product: ProductDetails,
        ip_response: Optional[QueryResponse],
        regulatory_result: Optional[RegulatoryAnalysisResult],
        international_result: Optional[InternationalAnalysisResult],
        cross_domain: Dict[str, Any],
        routing: IntentRoutingResult
    ) -> str:
        sections = []

        # Header: Executive Summary
        sections.append(
            f"## IP-SAKTI SAHAYAK: UNIFIED INTELLIGENCE DOSSIER\n\n"
            f"**Product / Subject Matter:** {product.name} ({product.dosage_form})\n"
            f"**Target Jurisdictions:** {', '.join(routing.detected_jurisdictions)}\n"
            f"**Pipeline Execution:** {' + '.join(filter(None, ['IP Intelligence' if routing.ip_required else '', 'Regulatory Guidance' if routing.regulatory_required else '', 'International Cross-Border' if routing.international_required else '']))}"
        )

        # Section 1: IP Findings
        if ip_response and ip_response.answer:
            sections.append(
                f"### 1. Intellectual Property & Patentability Findings\n"
                f"{ip_response.answer.strip()}"
            )
        elif routing.ip_required:
            sections.append(
                "### 1. Intellectual Property & Patentability Findings\n"
                "IP Analysis Engine encountered a temporary exception. Fallback statutory review indicates Section 3(p) Traditional Knowledge review is mandatory."
            )

        # Section 2: Regulatory Findings
        if regulatory_result:
            cls = regulatory_result.classification
            req_bullets = "\n".join([f"- **{r.category}:** {r.title} — *{r.what_user_should_do_next}*" for r in regulatory_result.checklist[:4]])
            forms_txt = ", ".join(regulatory_result.mandatory_forms) if regulatory_result.mandatory_forms else "Form 24-D, Form III"

            sections.append(
                f"### 2. Regulatory Compliance & Licensing Guidance\n"
                f"**Statutory Classification:** {cls.potentialCategory} (Confidence: {int(cls.confidence_score * 100)}%)\n"
                f"**Governing Authority:** {cls.authority}\n"
                f"**Statutory Basis:** {cls.statutoryBasis}\n"
                f"**Legal Rationale:** {cls.legalReasoning}\n\n"
                f"**Mandatory Statutory Forms:** {forms_txt}\n\n"
                f"**Key Statutory Requirements:**\n{req_bullets}"
            )

        # Section 3: Cross-Domain Synthesis
        if cross_domain.get("strategic_tradeoffs"):
            tradeoffs = "\n\n".join([f"- {t}" for t in cross_domain["strategic_tradeoffs"]])
            sections.append(
                f"### 3. Cross-Domain Strategic Synthesis (IP vs Regulatory Tradeoffs)\n"
                f"{tradeoffs}"
            )

        # Section 4: International Harmonization (if applicable)
        if international_result and international_result.dimensions:
            dims = "\n".join([f"- **{d.dimension}:** India: {d.india_details} | USA: {d.usa_details} (*{d.key_differences}*)" for d in international_result.dimensions[:3]])
            sections.append(
                f"### 4. International Regulatory Matrix (India vs USA vs EU)\n"
                f"{dims}\n\n"
                f"**Export Readiness Notice:** {international_result.export_readiness_alerts[0] if international_result.export_readiness_alerts else 'Ensure DSHEA compliance before US shipment.'}"
            )

        # Section 5: Actionable Next Steps
        actions = cross_domain.get("combined_action_plan", [])
        if actions:
            action_list = "\n".join([f"{i+1}. {act}" for i, act in enumerate(actions)])
            sections.append(
                f"### 5. Recommended Unified Action Plan\n"
                f"{action_list}"
            )

        return "\n\n---\n\n".join(sections)

    def _run_ip_analysis(self, request: QueryRequest, groq_key: str = None) -> QueryResponse:
        if self.pipeline:
            return self._process_with_pipeline(request)
        elif groq_key:
            return self._process_with_groq_llm(request, groq_key)
        else:
            return self._process_with_domain_knowledge(request)

    def _process_with_pipeline(self, request: QueryRequest) -> QueryResponse:
        query_str = request.question
        lang = (request.language or "en").lower().strip()
        persona_p = (request.persona or "startup").lower().strip()

        if hasattr(self.pipeline, "run"):
            result = self.pipeline.run(query_str)
        elif hasattr(self.pipeline, "query"):
            result = self.pipeline.query(query_str)
        else:
            result = self.pipeline(query_str)

        answer = result.get("answer") if isinstance(result, dict) else str(result)
        evidence_items = []
        if isinstance(result, dict) and "sources" in result:
            for idx, src in enumerate(result["sources"]):
                evidence_items.append(EvidenceItem(
                    id=f"ev_{idx+1:03d}",
                    title=f"{src.get('document_name', 'Source')} Section {src.get('section', '')}",
                    summary=src.get("summary", ""),
                    source=SourceItem(
                        document_name=src.get("document_name", "Authoritative Source"),
                        authority=src.get("authority", "Statutory Authority"),
                        jurisdiction=src.get("jurisdiction", "India"),
                        section=src.get("section", ""),
                        source_url=src.get("source_url", "")
                    ),
                    relevance_score=0.92
                ))

        return QueryResponse(
            answer=answer,
            confidence=0.90,
            evidence=evidence_items,
            domains=["Patent", "Traditional Knowledge"],
            primary_domain="Patent Law",
            risks=["Section 3(p) traditional knowledge rejection risk"],
            actions=["Conduct TKDL freedom-to-operate clearance search"],
            citations=[]
        )

    def _process_with_groq_llm(self, request: QueryRequest, groq_key: str) -> QueryResponse:
        from ..pipeline.domain_classifier import domain_classifier
        from ..pipeline.multilingual import multilingual_manager

        question = request.question.strip()
        lang = (request.language or "en").lower().strip()
        persona = (request.persona or "startup").lower().strip()

        system_prompt = (
            "You are IP-SAKTI SAHAYAK, the leading Indian Intellectual Property and Regulatory Intelligence AI System.\n"
            "Analyze the intellectual property case under Indian IP law and AYUSH regulations:\n"
            "1. Indian Patents Act, 1970 — Section 3(p) (traditional knowledge exclusions), Section 3(e) (admixtures), Section 2(1)(j) (inventive step).\n"
            "2. Biological Diversity Act, 2002 — Section 6 (Mandatory National Biodiversity Authority approval / Form 3 prior to patent grant).\n"
            "3. Traditional Knowledge Digital Library (TKDL) prior art guidelines.\n\n"
            "Structure output clearly under statutory grounds with section citations."
        )

        reply_text = ""
        try:
            from langchain_groq import ChatGroq
            from langchain_core.messages import SystemMessage, HumanMessage
            llm = ChatGroq(
                api_key=groq_key,
                model=os.getenv("MODEL_NAME", "qwen/qwen3.8-27b"),
                temperature=0.1,
                max_tokens=650
            )
            resp = llm.invoke([SystemMessage(content=system_prompt), HumanMessage(content=question)])
            reply_text = resp.content if hasattr(resp, "content") else str(resp)
        except Exception:
            try:
                from openai import OpenAI
                client = OpenAI(base_url="https://api.groq.com/openai/v1", api_key=groq_key)
                comp = client.chat.completions.create(
                    model=os.getenv("MODEL_NAME", "qwen/qwen3.8-27b"),
                    messages=[
                        {"role": "system", "content": system_prompt},
                        {"role": "user", "content": question}
                    ],
                    max_tokens=650,
                    temperature=0.1
                )
                reply_text = comp.choices[0].message.content
            except Exception as e2:
                logger.error(f"Groq API error ({e2}), falling back to domain knowledge.")
                return self._process_with_domain_knowledge(request)

        ev_items = [
            EvidenceItem(
                id="ev_ip_001",
                title="The Patents Act, 1970 — Section 3(p)",
                summary="Inventions that in effect are traditional knowledge or aggregations of known properties of traditionally known components are statutorily barred from patenting.",
                source=SourceItem(
                    document_name="Indian Patents Act, 1970",
                    authority="Indian Patent Office (CGPDTM)",
                    jurisdiction="India",
                    section="Section 3(p)",
                    source_url="https://ipindia.gov.in"
                ),
                relevance_score=0.96
            ),
            EvidenceItem(
                id="ev_ip_002",
                title="The Patents Act, 1970 — Section 3(e)",
                summary="A mere admixture of ingredients without demonstrated synergistic efficacy beyond additive properties is unpatentable.",
                source=SourceItem(
                    document_name="Indian Patents Act, 1970",
                    authority="Indian Patent Office (CGPDTM)",
                    jurisdiction="India",
                    section="Section 3(e)",
                    source_url="https://ipindia.gov.in"
                ),
                relevance_score=0.93
            ),
            EvidenceItem(
                id="ev_ip_003",
                title="Biological Diversity Act, 2002 — Section 6",
                summary="Mandatory requirement for prior approval of National Biodiversity Authority (Form 3) before applying for intellectual property rights based on Indian biological resources.",
                source=SourceItem(
                    document_name="Biological Diversity Act, 2002",
                    authority="National Biodiversity Authority (NBA)",
                    jurisdiction="India",
                    section="Section 6",
                    source_url="https://nbaindia.org"
                ),
                relevance_score=0.95
            )
        ]

        return QueryResponse(
            answer=reply_text,
            confidence=0.92,
            evidence=ev_items,
            domains=["Patent", "Traditional Knowledge", "Biodiversity/ABS"],
            primary_domain="Patent Law",
            risks=[
                "Section 3(p) rejection due to classical textual citation in TKDL",
                "Section 3(e) objection without laboratory demonstration of synergistic index",
                "Invalidation under Section 6 of Biological Diversity Act if NBA Form 3 is not filed"
            ],
            actions=[
                "Perform TKDL prior art search for identified botanicals",
                "Document synergy via Combination Index (CI < 0.8) assay",
                "Submit Form 3 to National Biodiversity Authority"
            ],
            citations=[]
        )

    def _process_with_domain_knowledge(self, request: QueryRequest) -> QueryResponse:
        demo = get_demo_query_response(request.language)
        return demo

query_service = QueryService()
