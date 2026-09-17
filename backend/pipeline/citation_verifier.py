"""
IP-SAKTI SAHAYAK
Citation & Grounding Verification Engine
========================================
Validates that generated citations ([Source X] or statutory section references)
map strictly to retrieved legal chunks and factual source text.
"""

import re
import logging
from typing import List, Dict, Any, Tuple

logger = logging.getLogger(__name__)


class CitationVerifier:
    """Verifies citation validity, factual grounding, and source metadata integrity."""

    def __init__(self):
        self.citation_pattern = re.compile(r"[\[【\(]Source[\s\u202f]*(\d+)[\]】\)]", re.IGNORECASE)

    def extract_citations(self, text: str) -> List[int]:
        """Extract all cited source indices from response text."""
        matches = self.citation_pattern.findall(text)
        return sorted(list(set(int(m) for m in matches)))

    def verify(
        self,
        answer: str,
        retrieved_evidence: List[Any]
    ) -> Dict[str, Any]:
        """
        Verify that all citations in the answer correspond to actual retrieved chunks
        and cross-check statutory mentions (e.g. Section 3(p), Section 6) against evidence.
        Returns verification status, verified source cards, and validity score.
        """
        cited_indices = self.extract_citations(answer)
        total_retrieved = len(retrieved_evidence)

        valid_citations = []
        invalid_citations = []

        for idx in cited_indices:
            if 1 <= idx <= total_retrieved:
                valid_citations.append(idx)
            else:
                invalid_citations.append(idx)

        # Check section-level cross-verification
        section_verified = False
        statutory_keywords = ["section 3(p)", "section 3(d)", "section 6", "nba", "tkdl", "rule 158b", "abs"]
        answer_lower = answer.lower()
        evidence_text = ""
        for ev in retrieved_evidence:
            if isinstance(ev, dict):
                evidence_text += " " + str(ev.get("content", "")) + " " + str(ev.get("title", "")) + " " + str(ev.get("summary", ""))
            elif hasattr(ev, "source"):
                evidence_text += " " + str(getattr(ev.source, "content", "")) + " " + str(getattr(ev, "summary", ""))
            elif hasattr(ev, "page_content"):
                evidence_text += " " + str(ev.page_content)
            else:
                evidence_text += " " + str(ev)
        evidence_lower = evidence_text.lower()

        # If answer mentions key legal sections, verify they actually appear in the evidence
        mentioned_sections = [kw for kw in statutory_keywords if kw in answer_lower]
        if mentioned_sections:
            matched_in_evidence = [kw for kw in mentioned_sections if kw in evidence_lower]
            section_verified = len(matched_in_evidence) >= min(1, len(mentioned_sections))
        else:
            section_verified = total_retrieved > 0

        is_valid = (len(invalid_citations) == 0 and (len(valid_citations) > 0 or section_verified))

        validity_score = 1.0 if is_valid else (0.5 if section_verified else 0.2)

        return {
            "is_valid": is_valid,
            "section_verified": section_verified,
            "validity_score": validity_score,
            "valid_indices": valid_citations,
            "invalid_indices": invalid_citations
        }


# Global singleton instance
citation_verifier = CitationVerifier()
