"""
IP-SAKTI SAHAYAK
Cross-Encoder Reranker
======================
Reranks top candidate chunks into the most authoritative, legally precise evidence.
Features cross-encoder integration with a precision heuristic fallback.
"""

import logging
from typing import List, Dict, Any
from config import Config

logger = logging.getLogger(__name__)


class PrecisionReranker:
    """Reranks retrieved candidate chunks down to top evidence chunks."""

    def __init__(self):
        self.model_name = Config.RERANKER_MODEL
        self.model = None
        self.use_fallback = False
        self._load_model()

    def _load_model(self):
        """Lazy load cross-encoder if available."""
        try:
            from sentence_transformers import CrossEncoder
            self.model = CrossEncoder(self.model_name)
            self.use_fallback = False
            logger.info("Loaded CrossEncoder reranker: %s", self.model_name)
        except Exception as e:
            logger.info("CrossEncoder not loaded (%s). Using precision legal ranking.", str(e))
            self.use_fallback = True

    def rerank(
        self,
        query: str,
        candidates: List[Dict[str, Any]],
        top_k: int = 5
    ) -> List[Dict[str, Any]]:
        """
        Rerank candidates down to top_k evidence chunks.
        """
        if not candidates:
            return []

        # If we have 5 or fewer candidates, return them sorted by retrieval score
        if len(candidates) <= top_k and self.use_fallback:
            return sorted(candidates, key=lambda x: x.get("retrieval_score", 0), reverse=True)

        # Use CrossEncoder if available
        if not self.use_fallback and self.model is not None:
            try:
                pairs = [[query, c["text"]] for c in candidates]
                scores = self.model.predict(pairs)
                for idx, c in enumerate(candidates):
                    # Sigmoid or min-max normalization
                    raw = float(scores[idx])
                    norm = 1.0 / (1.0 + pow(2.71828, -raw))
                    c["rerank_score"] = round(norm, 4)
                return sorted(candidates, key=lambda x: x["rerank_score"], reverse=True)[:top_k]
            except Exception as e:
                logger.warning("CrossEncoder prediction error: %s. Using heuristic fallback.", str(e))

        # Precision Legal Heuristic Reranker
        # Balances retrieval score, section specificity, authority level, and token overlap
        query_words = set(query.lower().split())

        for c in candidates:
            base_score = c.get("retrieval_score", 0.5)
            text_lower = c["text"].lower()

            # Overlap factor
            matches = sum(1 for w in query_words if len(w) > 3 and w in text_lower)
            overlap_bonus = min(0.25, matches * 0.05)

            # Statutory section bonus
            sec = c.get("section", "")
            section_bonus = 0.15 if sec and sec != "General" else 0.0

            # Authority level bonus (LEVEL_1 primary acts receive highest weight)
            auth_level = c.get("authority_level", "LEVEL_3")
            auth_bonus = 0.10 if auth_level == "LEVEL_1" else (0.05 if auth_level == "LEVEL_2" else 0.0)

            c["rerank_score"] = round(min(1.0, base_score + overlap_bonus + section_bonus + auth_bonus), 4)

        return sorted(candidates, key=lambda x: x["rerank_score"], reverse=True)[:top_k]


# Global singleton instance
reranker = PrecisionReranker()
