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
        "biological diversity act", "form iii", "form 3", "section 3(d)", "section 3(p)",
        "drugs and cosmetics", "rule 158b"
    ]

    INTL_TERMS = [
        "international", "wipo", "pct", "patent cooperation treaty", "trips",
        "nagoya protocol", "madrid", "hague", "wto", "who", "paris convention",
        "epc", "uspto", "epo", "jpo", "worldwide", "global", "export"
    ]

    def detect(self, query: str) -> Dict[str, Any]:
        """Determine legal jurisdiction for the query or source item."""
        text_lower = query.lower()

        indian_matches = sum(1 for term in self.INDIAN_TERMS if term in text_lower)
        intl_matches = sum(1 for term in self.INTL_TERMS if term in text_lower)

        # Specific international regions
        if "united states" in text_lower or "uspto" in text_lower or "u.s." in text_lower:
            return {
                "jurisdiction": "United States (USPTO)",
                "badge_label": "International (US)",
                "is_international": True
            }
        if "europe" in text_lower or "epo" in text_lower:
            return {
                "jurisdiction": "Europe (EPO)",
                "badge_label": "International (EPO)",
                "is_international": True
            }
        if "pct" in text_lower or "wipo" in text_lower or "trips" in text_lower:
            return {
                "jurisdiction": "International (WIPO/PCT)",
                "badge_label": "International (WIPO/PCT)",
                "is_international": True
            }

        if intl_matches > indian_matches:
            return {
                "jurisdiction": "International (WIPO/PCT)",
                "badge_label": "International (WIPO/PCT)",
                "is_international": True
            }
        elif indian_matches > 0 and intl_matches > 0:
            return {
                "jurisdiction": "India & International (WIPO/PCT)",
                "badge_label": "India & International (WIPO/PCT)",
                "is_international": True
            }

        return {
            "jurisdiction": "India",
            "badge_label": "India-Specific",
            "is_international": False
        }


# Global singleton instance
jurisdiction_detector = JurisdictionDetector()
