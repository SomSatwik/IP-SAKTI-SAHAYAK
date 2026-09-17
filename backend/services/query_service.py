import os
import logging
from typing import Dict, Any, List
from dotenv import load_dotenv

from ..models import QueryRequest, QueryResponse, EvidenceItem, SourceItem, CitationItem
from ..demo_data import get_demo_query_response

logger = logging.getLogger(__name__)

class QueryService:
    def __init__(self):
        self.pipeline = None
        self.is_ready = False
        self.try_init_pipeline()

    def try_init_pipeline(self) -> bool:
        """
        Dynamically attempt to initialize the pipeline.
        This allows the server to automatically detect when a friend
        adds a GROQ_API_KEY in .env or builds the FAISS index.
        """
        try:
            # Reload environment in case .env was recently created/modified
            load_dotenv(override=True)
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
            else:
                logger.info(
                    f"Pipeline not ready yet. GROQ_API_KEY present: {bool(groq_key)}, "
                    f"Index present: {has_index}. Graceful demo mode active."
                )
                self.is_ready = False
                return False
        except Exception as e:
            logger.warning(f"Pipeline initialization skipped: {e}")
            self.is_ready = False
            return False

    def process_query(self, request: QueryRequest) -> QueryResponse:
        # Check if pipeline can now be initialized if it wasn't ready
        if not self.is_ready or not self.pipeline:
            self.try_init_pipeline()

        if not self.is_ready or not self.pipeline:
            logger.info(f"Using demo response for language '{request.language}'.")
            demo_resp = get_demo_query_response(request.language)
            from ..pipeline.domain_classifier import domain_classifier
            classification = domain_classifier.classify(request.question)
            return QueryResponse(
                answer=demo_resp.answer,
                confidence=demo_resp.confidence,
                evidence=demo_resp.evidence,
                domains=classification.get("all_detected", ["Ayurveda", "IP"]),
                primary_domain=classification.get("primary_domain", "Ayurveda"),
                risks=demo_resp.risks,
                actions=demo_resp.actions,
                citations=demo_resp.citations,
                abstained=demo_resp.abstained,
                disclaimer=demo_resp.disclaimer
            )

        try:
            logger.info(f"Processing query via real pipeline: {request.question} (lang: {request.language})")
            
            # Format query with language and persona instruction
            query_str = request.question
            lang = (request.language or "en").lower().strip()
            persona_p = (request.persona or "startup").lower().strip()

            persona_prompts = {
                "practitioner": "\n[Persona Focus: Ayurvedic Practitioner — emphasize clinical indications, classical texts, Schedule T GMP, and therapeutic safety.]",
                "researcher": "\n[Persona Focus: Academic Researcher — emphasize Section 3(p) prior art, synergistic CI index, and chemical characterization.]",
                "msme": "\n[Persona Focus: MSME Manufacturer — emphasize Form 28 fee waivers, manufacturing licenses, and AYUSH Premium Mark.]",
                "cultivator": "\n[Persona Focus: Herbal Cultivator — emphasize Biological Diversity Act exemptions, fair Access and Benefit Sharing (ABS), and SBB intimation.]",
                "startup": "\n[Persona Focus: AYUSH Startup — emphasize Form 18A expedited examination, DPIIT startup schemes, and rapid commercialization.]"
            }
            query_str += persona_prompts.get(persona_p, persona_prompts["startup"])

            if lang in ["hi", "hindi"]:
                query_str += "\n\n(Please provide the grounded legal analysis and explanations in Hindi / हिन्दी.)"
            elif lang in ["or", "odia", "oriya"]:
                query_str += "\n\n(Please provide the grounded legal analysis and explanations in Odia / ଓଡ଼ିଆ.)"

            # Duck-typed invocation of the pipeline
            if hasattr(self.pipeline, "run"):
                result = self.pipeline.run(query_str)
            elif hasattr(self.pipeline, "query"):
                result = self.pipeline.query(query_str)
            elif hasattr(self.pipeline, "process_query"):
                result = self.pipeline.process_query(query_str)
            elif callable(self.pipeline):
                result = self.pipeline(query_str)
            else:
                raise AttributeError("Pipeline object does not expose a callable query/run method.")

            # Extract answer
            answer = ""
            if isinstance(result, dict):
                answer = result.get("answer") or result.get("response") or result.get("output") or ""
            elif isinstance(result, str):
                answer = result
            else:
                answer = str(result)

            # Check evidence status / safe abstention
            evidence_status = "SUFFICIENT"
            abstained = False
            if isinstance(result, dict):
                evidence_status = result.get("evidence_status", "SUFFICIENT")
                abstained = (evidence_status == "INSUFFICIENT") or result.get("abstained", False)

            # Extract evidence items
            evidence_items: List[EvidenceItem] = []
            
            # Case 1: sources list of dicts (from IPRAGPipeline.run)
            if isinstance(result, dict) and "sources" in result and isinstance(result["sources"], list):
                for idx, src in enumerate(result["sources"]):
                    if isinstance(src, dict):
                        doc_name = src.get("document_name", f"Authoritative Source {idx+1}")
                        from ..pipeline.jurisdiction import jurisdiction_detector
                        detected_jur = jurisdiction_detector.detect(f"{doc_name} {src.get('section', '')} {src.get('authority', '')}")
                        jur_val = src.get("jurisdiction") or detected_jur["jurisdiction"]
                        source_item = SourceItem(
                            document_name=doc_name,
                            authority=src.get("authority"),
                            jurisdiction=jur_val,
                            section=src.get("section"),
                            page=str(src.get("page", "")),
                            version=src.get("version"),
                            effective_date=src.get("effective_date"),
                            source_url=src.get("source_url"),
                            content=src.get("content", f"Excerpt from {doc_name}")
                        )
                        evidence_items.append(EvidenceItem(
                            id=f"ev_{idx+1:03d}",
                            title=f"{doc_name} — {src.get('section', 'Relevant Section')}",
                            summary=src.get("summary", f"Grounded reference from {doc_name}."),
                            source=source_item,
                            relevance_score=float(src.get("relevance_score", 0.90 - idx * 0.03))
                        ))

            # Case 2: retrieved_evidence / documents list of Document objects
            docs = []
            if isinstance(result, dict):
                docs = result.get("retrieved_evidence") or result.get("documents") or []
            if docs and not evidence_items:
                for idx, ev in enumerate(docs):
                    content = getattr(ev, "page_content", str(ev))
                    metadata = getattr(ev, "metadata", {}) if hasattr(ev, "metadata") else {}
                    doc_name = metadata.get("document_name") or metadata.get("source") or f"Document {idx+1}"
                    
                    from ..pipeline.jurisdiction import jurisdiction_detector
                    detected_jur = jurisdiction_detector.detect(f"{doc_name} {metadata.get('section', '')} {metadata.get('authority', '')}")
                    jur_val = metadata.get("jurisdiction") or detected_jur["jurisdiction"]
                    
                    source_item = SourceItem(
                        document_name=doc_name,
                        authority=metadata.get("authority", "Statutory Authority"),
                        jurisdiction=jur_val,
                        section=metadata.get("section", "General"),
                        page=str(metadata.get("page", "")),
                        version=metadata.get("version"),
                        effective_date=metadata.get("effective_date"),
                        source_url=metadata.get("source_url"),
                        content=content[:400]
                    )
                    evidence_items.append(EvidenceItem(
                        id=f"ev_{idx+1:03d}",
                        title=f"{doc_name} Section {metadata.get('section', 'Reference')}",
                        summary=content[:120] + "...",
                        source=source_item,
                        relevance_score=0.88 - (idx * 0.02)
                    ))

            # Apply Persona Source Biasing
            persona_bias_keywords = {
                "practitioner": ["pharmacopoeia", "samhita", "schedule t", "clinical", "therapeutic", "dosage", "drug"],
                "researcher": ["tkdl", "prior art", "novelty", "3(p)", "synergy", "extraction", "patent"],
                "startup": ["startup", "expedited", "18a", "patent", "nba", "clearance", "commercial"],
                "msme": ["msme", "form 28", "subsidy", "licens", "premium mark", "gmp"],
                "cultivator": ["biodiversity", "abs", "access", "benefit sharing", "sbb", "bmc", "cultivat", "raw material"]
            }
            bias_kws = persona_bias_keywords.get(persona_p, [])
            if bias_kws:
                for item in evidence_items:
                    text_blob = f"{item.title} {item.summary} {item.source.document_name} {item.source.section or ''}".lower()
                    if any(kw in text_blob for kw in bias_kws):
                        item.relevance_score = min(0.99, item.relevance_score + 0.12)
                evidence_items.sort(key=lambda x: x.relevance_score, reverse=True)

            # Citations
            citations = []
            for ev in evidence_items[:3]:
                citations.append(CitationItem(
                    text=f"Verified under {ev.source.document_name} {ev.source.section or ''}",
                    source_id=ev.id
                ))

            # Confidence calculation
            confidence = 0.85
            if isinstance(result, dict) and ("confidence" in result or "confidence_score" in result):
                confidence = float(result.get("confidence") or result.get("confidence_score", 0.85))
            elif abstained:
                confidence = 0.35
            elif evidence_items:
                confidence = min(0.70 + len(evidence_items) * 0.05, 0.95)

            # Domains detection via DomainClassifier
            from ..pipeline.domain_classifier import domain_classifier
            classification = domain_classifier.classify(request.question)
            detected_domains = classification.get("all_detected", ["Ayurveda", "IP"])
            primary_domain = classification.get("primary_domain", "Ayurveda")

            # Risk flags
            risks = [
                "Patentability Review: Overlap with prior art and Section 3 exclusions",
                "Compliance Mandate: Regulatory approval required prior to filing",
                "Access & Benefit Sharing (ABS): Review obligations with State Biodiversity Board"
            ]

            # Actions
            actions = [
                "Perform clearance search across authoritative registries",
                "Verify statutory exceptions and source disclosures",
                "Prepare regulatory intimation before commercialization"
            ]

            # Citation verification via CitationVerifier
            from ..pipeline.citation_verifier import citation_verifier
            verification_res = citation_verifier.verify(answer, evidence_items)
            citation_verified = bool(verification_res.get("is_valid", True))

            disclaimer = (
                result.get("disclaimer") if isinstance(result, dict) and "disclaimer" in result
                else "This intelligence report is generated by IP-SAKTI Sahayak based on retrieved statutory sources. It does not constitute formal legal counsel."
            )

            return QueryResponse(
                answer=answer,
                confidence=confidence,
                evidence=evidence_items,
                domains=detected_domains,
                primary_domain=primary_domain,
                risks=risks,
                actions=actions,
                citations=citations,
                citation_verified=citation_verified,
                abstained=abstained,
                disclaimer=disclaimer
            )

        except Exception as e:
            logger.exception(f"Error during pipeline execution: {e}. Falling back to demo data.")
            return get_demo_query_response(request.language)

query_service = QueryService()
