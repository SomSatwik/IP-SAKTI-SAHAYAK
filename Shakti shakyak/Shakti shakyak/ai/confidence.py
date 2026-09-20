"""
IP-SAKTI SAHAYAK
Multi-Signal Confidence Scoring Engine
======================================
Synthesizes dense semantic score, BM25 keyword score, reranker rank,
source authority tier, and citation verification into a calibrated 0-100% score.
"""

from typing import List, Dict, Any
from config import Config


class ConfidenceScorer:
    """Calculates factual and grounding confidence score for answers."""

    def calculate(
        self,
        retrieved_chunks: List[Dict[str, Any]],
        citation_info: Dict[str, Any],
        query: str
    ) -> Dict[str, Any]:
        """
        Compute weighted confidence score and classify into confidence tier.
        """
        if not retrieved_chunks:
            return {
                "score": 0.10,
                "percentage": 10,
                "level": "VERY LOW",
                "explanation": "No authoritative legal sources were identified matching the query."
            }

        # 1. Retrieval & Reranker Score Component (Weight: 40%)
        top_chunk = retrieved_chunks[0]
        retrieval_score = top_chunk.get("rerank_score") or top_chunk.get("retrieval_score", 0.5)

        # 2. Source Authority Level Component (Weight: 25%)
        # Level 1 = 1.0, Level 2 = 0.85, Level 3 = 0.75, Level 4 = 0.60, Level 5 = 0.40
        auth_level = top_chunk.get("authority_level", "LEVEL_3")
        auth_weights = {
            "LEVEL_1": 1.0,
            "LEVEL_2": 0.88,
            "LEVEL_3": 0.75,
            "LEVEL_4": 0.60,
            "LEVEL_5": 0.40
        }
        authority_score = auth_weights.get(auth_level, 0.70)

        # 3. Citation & Grounding Validity Component (Weight: 20%)
        citation_validity = citation_info.get("validity_score", 0.7)

        # 4. Corroboration Component (Weight: 15%)
        # Multiple independent sources agreeing increases confidence
        corroboration_score = min(1.0, 0.5 + (len(retrieved_chunks) * 0.1))

        # Weighted aggregate
        composite_score = (
            (retrieval_score * 0.40) +
            (authority_score * 0.25) +
            (citation_validity * 0.20) +
            (corroboration_score * 0.15)
        )

        composite_score = round(max(0.10, min(0.98, composite_score)), 2)
        percentage = int(composite_score * 100)

        if composite_score >= Config.CONFIDENCE_HIGH:
            level = "HIGH"
            explanation = "Supported by primary statutory provisions from official legal authorities."
        elif composite_score >= Config.CONFIDENCE_MEDIUM:
            level = "MEDIUM"
            explanation = "Supported by relevant regulatory guidelines and institutional legal sources."
        elif composite_score >= Config.CONFIDENCE_LOW:
            level = "LOW"
            explanation = "Limited direct evidence found; interpretation requires independent verification."
        else:
            level = "VERY LOW"
            explanation = "Insufficient or ambiguous authoritative legal evidence found."

        return {
            "score": composite_score,
            "percentage": percentage,
            "level": level,
            "explanation": explanation,
            "caveat": "Confidence reflects evidence retrieval quality and statutory authority, not a binding legal guarantee."
        }


# Global singleton instance
confidence_scorer = ConfidenceScorer()
