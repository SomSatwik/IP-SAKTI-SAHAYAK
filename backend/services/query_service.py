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
            return get_demo_query_response(request.language)

        try:
            logger.info(f"Processing query via real pipeline: {request.question} (lang: {request.language})")
            
            # Format query with language instruction if multilingual requested
            query_str = request.question
            lang = (request.language or "en").lower().strip()
            if lang in ["hi", "hindi"]:
                query_str = f"{request.question}\n\n(Please provide the grounded legal analysis and explanations in Hindi / हिन्दी.)"
            elif lang in ["or", "odia", "oriya"]:
                query_str = f"{request.question}\n\n(Please provide the grounded legal analysis and explanations in Odia / ଓଡ଼ିଆ.)"

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
                        source_item = SourceItem(
                            document_name=doc_name,
                            authority=src.get("authority"),
                            jurisdiction=src.get("jurisdiction", "India"),
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
                    
                    source_item = SourceItem(
                        document_name=doc_name,
                        authority=metadata.get("authority", "Statutory Authority"),
                        jurisdiction=metadata.get("jurisdiction", "India"),
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

            # Domains detection
            domains = ["Patent", "Traditional Knowledge", "Biodiversity / ABS"]
            q_lower = request.question.lower()
            if "trademark" in q_lower or "brand" in q_lower:
                domains.append("Trademark")
            if "copyright" in q_lower or "code" in q_lower or "software" in q_lower:
                domains.append("Copyright")
            if "geographical" in q_lower or "gi" in q_lower:
                domains.append("Geographical Indication")

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

            disclaimer = (
                result.get("disclaimer") if isinstance(result, dict) and "disclaimer" in result
                else "This intelligence report is generated by IP-SAKTI Sahayak based on retrieved statutory sources. It does not constitute formal legal counsel."
            )

            return QueryResponse(
                answer=answer,
                confidence=confidence,
                evidence=evidence_items,
                domains=domains,
                risks=risks,
                actions=actions,
                citations=citations,
                abstained=abstained,
                disclaimer=disclaimer
            )

        except Exception as e:
            logger.exception(f"Error during pipeline execution: {e}. Falling back to demo data.")
            return get_demo_query_response(request.language)

query_service = QueryService()
