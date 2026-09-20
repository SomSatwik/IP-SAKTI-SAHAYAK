"""
IP-SAKTI SAHAYAK
Jurisdiction Detector
=====================
Identifies applicable legal jurisdiction (India, International, or regional)
from statutory cues, treaty references, or explicit geographic mentions.
"""

import re
from typing import Dict, Any


class JurisdictionDetector:
    """Detects applicable territorial jurisdiction for legal analysis."""

    INDIAN_TERMS = [
        "india", "indian", "cgpdtm", "ip india", "ayush", "nba", "sbb", "bmc",
        "patents act 1970", "delhi high court", "madras high court", "tkdl",
        "biological diversity act", "form iii", "section 3(d)", "section 3(p)"
    ]

    INTL_TERMS = [
        "international", "wipo", "pct", "patent cooperation treaty", "trips",
        "nagoya protocol", "madrid", "hague", "wto", "who", "paris convention",
        "epc", "uspto", "epo", "jpo", "worldwide", "global"
    ]

    def detect(self, query: str) -> Dict[str, Any]:
        """Determine legal jurisdiction for the query."""
        text_lower = query.lower()

        indian_matches = sum(1 for term in self.INDIAN_TERMS if term in text_lower)
        intl_matches = sum(1 for term in self.INTL_TERMS if term in text_lower)

        # Specific international regions
        if "united states" in text_lower or "uspto" in text_lower or "u.s." in text_lower:
            return {"jurisdiction": "United States", "is_international": True, "requires_clarification": False}
        if "europe" in text_lower or "epo" in text_lower:
            return {"jurisdiction": "Europe", "is_international": True, "requires_clarification": False}

        if indian_matches > intl_matches:
            return {"jurisdiction": "India", "is_international": False, "requires_clarification": False}
        elif intl_matches > indian_matches:
            return {"jurisdiction": "International", "is_international": True, "requires_clarification": False}
        elif indian_matches > 0 and intl_matches > 0:
            return {"jurisdiction": "India & International", "is_international": True, "requires_clarification": False}

        # Default for IP-SAKTI Sahayak if unspecified
        return {
            "jurisdiction": "India",
            "is_international": False,
            "requires_clarification": False,
            "note": "Defaulted to Indian jurisdiction based on platform focus."
        }


# Global singleton instance
jurisdiction_detector = JurisdictionDetector()
