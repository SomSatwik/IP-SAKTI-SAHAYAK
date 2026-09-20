"""
IP-SAKTI SAHAYAK
End-to-End Hybrid RAG Orchestration Pipeline
============================================
Coordinates domain classification, jurisdiction detection, hybrid search (FAISS+BM25),
real-time authoritative source fetching, reranking, LLM grounding, citation verification,
confidence scoring, and safe abstention.
"""

import logging
import time
from typing import Dict, Any, Optional

from ai.domain_classifier import domain_classifier
from ai.jurisdiction import jurisdiction_detector
from ai.multilingual import multilingual_manager
from ai.llm import groq_client
from ai.prompts import build_rag_prompt
from ai.confidence import confidence_scorer
from ai.citation_verifier import citation_verifier
from ai.abstention import abstention_engine

from rag.hybrid_search import hybrid_searcher
from rag.reranker import reranker
from rag.web_sources import web_source_manager
from database.db import get_db_session
from database.models import QueryLog, Citation

logger = logging.getLogger(__name__)


class RAGPipeline:
    """Master pipeline managing the lifecycle of an IP/regulatory inquiry."""

    def process_query(
        self,
        query: str,
        conversation_id: Optional[str] = None,
        preferred_jurisdiction: Optional[str] = None,
        preferred_language: Optional[str] = None
    ) -> Dict[str, Any]:
        """
        Execute full pipeline from query to grounded answer with verified citations.
        """
        start_time = time.time()

        # Step 1: Detect Language
        detected_lang = multilingual_manager.detect_language(query)
        active_lang = preferred_language if preferred_language in ("en", "hi", "or") else detected_lang

        # Step 2: Classify Domain
        domain_info = domain_classifier.classify(query)
        primary_domain = domain_info["primary_domain"]

        # Step 3: Detect Jurisdiction
        if preferred_jurisdiction and preferred_jurisdiction.lower() != "auto detect":
            jurisdiction = preferred_jurisdiction
        else:
            jurisdiction_info = jurisdiction_detector.detect(query)
            jurisdiction = jurisdiction_info["jurisdiction"]

        # Step 4: Hybrid Retrieval (FAISS + BM25)
        local_candidates = hybrid_searcher.search(
            query=query,
            top_k=15,
            jurisdiction_filter=jurisdiction if jurisdiction != "India & International" else None,
            domain_filter=primary_domain
        )

        # Step 5: Real-time official web source supplementation if local candidates are sparse
        if len(local_candidates) < 3:
            live_sources = web_source_manager.search_official_sources(query, max_results=3)
            all_candidates = local_candidates + live_sources
        else:
            all_candidates = local_candidates

        # Step 6: Reranking (top candidates -> top 5 evidence chunks)
        top_evidence = reranker.rerank(query=query, candidates=all_candidates, top_k=5)

        # Initial confidence assessment
        tentative_conf = confidence_scorer.calculate(top_evidence, {"validity_score": 1.0}, query)

        # Step 7: Safe Abstention Evaluation
        abstention_check = abstention_engine.evaluate_abstention(
            query=query,
            evidence_chunks=top_evidence,
            confidence_score=tentative_conf["score"]
        )

        if abstention_check["should_abstain"]:
            latency_ms = int((time.time() - start_time) * 1000)
            abstention_answer = abstention_check["abstention_response"]

            # Log to DB
            self._log_query(
                conversation_id=conversation_id,
                query=query,
                domain=primary_domain,
                jurisdiction=jurisdiction,
                language=active_lang,
                answer=abstention_answer,
                confidence=tentative_conf["score"],
                conf_level="VERY LOW",
                is_abstained=True,
                reason=abstention_check["reason"],
                latency_ms=latency_ms,
                citations=[]
            )

            return {
                "success": True,
                "answer": abstention_answer,
                "confidence": tentative_conf["score"],
                "confidence_percentage": tentative_conf["percentage"],
                "confidence_level": "VERY LOW",
                "confidence_explanation": tentative_conf["explanation"],
                "is_abstained": True,
                "abstention_reason": abstention_check["reason"],
                "domain": primary_domain,
                "jurisdiction": jurisdiction,
                "language": active_lang,
                "sources": [],
                "citations": [],
                "latency_ms": latency_ms
            }

        # Step 8: Construct Grounded Prompt & Invoke Groq LLM
        prompt = build_rag_prompt(
            query=query,
            evidence_chunks=top_evidence,
            detected_domain=primary_domain,
            detected_jurisdiction=jurisdiction,
            preferred_language=active_lang
        )

        llm_result = groq_client.generate_response(prompt=prompt)

        if not llm_result["success"]:
            # Fallback message
            raw_answer = f"### DIRECT ANSWER\n{llm_result['content']}"
        else:
            raw_answer = llm_result["content"]

        # Step 9: Verify Citations & Grounding
        verification_result = citation_verifier.verify(
            answer=raw_answer,
            retrieved_evidence=top_evidence
        )

        final_answer = verification_result["sanitized_answer"]
        verified_sources = verification_result["verified_sources"]

        # Step 10: Final Calibrated Confidence Scoring
        final_confidence = confidence_scorer.calculate(
            retrieved_chunks=top_evidence,
            citation_info=verification_result,
            query=query
        )

        latency_ms = int((time.time() - start_time) * 1000)

        # Step 11: Persist Query Log & Citations to DB
        self._log_query(
            conversation_id=conversation_id,
            query=query,
            domain=primary_domain,
            jurisdiction=jurisdiction,
            language=active_lang,
            answer=final_answer,
            confidence=final_confidence["score"],
            conf_level=final_confidence["level"],
            is_abstained=False,
            reason=None,
            latency_ms=latency_ms,
            citations=verified_sources
        )

        return {
            "success": True,
            "answer": final_answer,
            "confidence": final_confidence["score"],
            "confidence_percentage": final_confidence["percentage"],
            "confidence_level": final_confidence["level"],
            "confidence_explanation": final_confidence["explanation"],
            "caveat": final_confidence["caveat"],
            "is_abstained": False,
            "domain": primary_domain,
            "jurisdiction": jurisdiction,
            "language": active_lang,
            "sources": verified_sources,
            "latency_ms": latency_ms
        }

    def _log_query(
        self,
        conversation_id: Optional[str],
        query: str,
        domain: str,
        jurisdiction: str,
        language: str,
        answer: str,
        confidence: float,
        conf_level: str,
        is_abstained: bool,
        reason: Optional[str],
        latency_ms: int,
        citations: list
    ):
        """Asynchronously/safely write query telemetry to SQLite."""
        try:
            with get_db_session() as session:
                log_entry = QueryLog(
                    conversation_id=conversation_id or "default_session",
                    query_text=query,
                    detected_domain=domain,
                    detected_jurisdiction=jurisdiction,
                    language=language,
                    answer_text=answer,
                    confidence_score=confidence,
                    confidence_level=conf_level,
                    is_abstained=is_abstained,
                    abstention_reason=reason,
                    latency_ms=latency_ms
                )
                session.add(log_entry)
                session.flush()

                for c in citations:
                    citation_record = Citation(
                        query_log_id=log_entry.id,
                        citation_index=c.get("citation_index", 1),
                        document_title=c.get("title", ""),
                        authority=c.get("authority", ""),
                        jurisdiction=c.get("jurisdiction", ""),
                        section=c.get("section", ""),
                        source_url=c.get("source_url", ""),
                        text_snippet=c.get("text_snippet", ""),
                        is_verified=True
                    )
                    session.add(citation_record)
        except Exception as e:
            logger.warning("Could not persist query log to DB: %s", str(e))


# Global singleton instance
rag_pipeline = RAGPipeline()
