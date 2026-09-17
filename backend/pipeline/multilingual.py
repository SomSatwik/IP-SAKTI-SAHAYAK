"""
IP-SAKTI SAHAYAK
Multilingual Legal Processing Pipeline
====================================
Language detection and legal-term-preserving translation for English, Hindi, and Odia.
Ensures standardized statutory acronyms (s.3(d), s.3(p), TKDL, PCT, NBA) remain untranslated.
"""

import re
import logging
from typing import Dict, Any, List

logger = logging.getLogger(__name__)

class MultilingualManager:
    """Manages language detection and statutory legal terminology preservation."""

    PRESERVED_LEGAL_TERMS = [
        "Section 3(d)", "Section 3(p)", "Section 25", "Section 84", "Section 6",
        "The Patents Act, 1970", "Biological Diversity Act, 2002",
        "TKDL", "CSIR", "NBA", "SBB", "BMC", "Form III", "AYUSH",
        "PCT", "TRIPS", "WIPO", "Nagoya Protocol", "Geographical Indication",
        "Prior Art", "Inventive Step", "Novelty", "Panchakarma", "Ashwagandha",
        "Curcuma longa", "Azadirachta indica", "Ocimum sanctum", "Withania somnifera"
    ]

    def detect_language(self, text: str) -> str:
        """
        Detect whether the user input is in English ('en'), Hindi ('hi'), or Odia ('or').
        """
        if not text or not text.strip():
            return "en"

        # Devanagari Unicode Block: \u0900 - \u097F
        devanagari_count = len(re.findall(r"[\u0900-\u097F]", text))

        # Odia Unicode Block: \u0B00 - \u0B7F
        odia_count = len(re.findall(r"[\u0B00-\u0B7F]", text))

        total_chars = max(1, len(text.strip()))

        if devanagari_count / total_chars > 0.12:
            return "hi"
        elif odia_count / total_chars > 0.12:
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

    def get_system_prompt_instruction(self, lang_code: str) -> str:
        """Generate language-specific system prompt instruction."""
        normalized = lang_code.lower()
        if normalized in ["hi", "hindi"]:
            return (
                "Respond in clear, professional Hindi (हिन्दी). "
                "Keep statutory legal terms, act numbers, and biological names intact: "
                "e.g. 'Section 3(p)', 'The Patents Act, 1970', 'TKDL', 'Form III', 'NBA'."
            )
        elif normalized in ["or", "odia", "oriya"]:
            return (
                "Respond in clear, professional Odia (ଓଡ଼ିଆ). "
                "Keep statutory legal terms, act numbers, and biological names intact: "
                "e.g. 'Section 3(p)', 'The Patents Act, 1970', 'TKDL', 'Form III', 'NBA'."
            )
        return (
            "Respond in authoritative, clear Indian English. "
            "Cite relevant sections like Section 3(p) of the Patents Act, 1970 and Section 6 of the Biological Diversity Act, 2002."
        )

# Global singleton instance
multilingual_manager = MultilingualManager()
