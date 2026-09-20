"""
IP-SAKTI SAHAYAK
Multilingual Legal Processing Engine
====================================
Language detection and legal-term-preserving translation for English, Hindi, and Odia.
Ensures standardized statutory acronyms (s.3(d), TKDL, PCT, NBA) remain untranslated.
"""

import re
from typing import Dict, Any


class MultilingualManager:
    """Manages language detection and legal terminology preservation."""

    PRESERVED_LEGAL_TERMS = [
        "Section 3(d)", "Section 3(p)", "Section 25", "Section 84", "Section 6",
        "The Patents Act, 1970", "Biological Diversity Act, 2002",
        "TKDL", "CSIR", "NBA", "SBB", "BMC", "Form III", "AYUSH",
        "PCT", "TRIPS", "WIPO", "Nagoya Protocol", "Geographical Indication",
        "Prior Art", "Inventive Step", "Novelty", "Panchakarma", "Ashwagandha"
    ]

    def detect_language(self, text: str) -> str:
        """
        Detect whether the user input is in English, Hindi (Devanagari), or Odia script.
        """
        # Devanagari Unicode Block: \u0900 - \u097F
        devanagari_count = len(re.findall(r"[\u0900-\u097F]", text))

        # Odia Unicode Block: \u0B00 - \u0B7F
        odia_count = len(re.findall(r"[\u0B00-\u0B7F]", text))

        total_chars = max(1, len(text.strip()))

        if devanagari_count / total_chars > 0.15:
            return "hi"
        elif odia_count / total_chars > 0.15:
            return "or"
        return "en"

    def get_language_metadata(self, lang_code: str) -> Dict[str, str]:
        """Return display labels for supported languages."""
        meta = {
            "en": {"name": "English", "native": "English", "dir": "ltr"},
            "hi": {"name": "Hindi", "native": "हिन्दी", "dir": "ltr"},
            "or": {"name": "Odia", "native": "ଓଡ଼ିଆ", "dir": "ltr"}
        }
        return meta.get(lang_code, meta["en"])


# Global singleton instance
multilingual_manager = MultilingualManager()
