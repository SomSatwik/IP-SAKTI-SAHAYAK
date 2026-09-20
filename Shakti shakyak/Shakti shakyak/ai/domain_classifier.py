"""
IP-SAKTI SAHAYAK
Domain Classifier
=================
Identifies intellectual property and regulatory domains from query intent
using statutory keywords, semantic heuristics, and specialized taxonomy.
"""

import re
from typing import Dict, List, Tuple


class DomainClassifier:
    """Classifies user queries into specific IP and Traditional Knowledge domains."""

    DOMAINS = [
        "Patent",
        "Ayurveda",
        "Traditional Knowledge",
        "Biodiversity & ABS",
        "Geographical Indications",
        "Trademarks",
        "Copyright",
        "Industrial Designs",
        "Plant Varieties",
        "Trade Secrets",
        "International IP & Treaties",
        "Regulatory & Pharmacopoeia",
        "General IP"
    ]

    KEYWORD_MAP = {
        "Ayurveda": [
            "ayurveda", "ayurvedic", "herbal", "formulation", "ashwagandha", "neem",
            "turmeric", "curcumin", "charaka", "sushruta", "ashtanga", "rasa", "bhasma",
            "ayush", "ism", "panchakarma", "medicinal plant", "vpk"
        ],
        "Traditional Knowledge": [
            "traditional knowledge", "tkdl", "prior art", "biopiracy", "indigenous",
            "folklore", "ancient texts", "3(p)", "section 3(p)", "misappropriation"
        ],
        "Biodiversity & ABS": [
            "biodiversity", "biological diversity", "nba", "national biodiversity authority",
            "sbb", "state biodiversity board", "bmc", "access and benefit sharing", "abs",
            "section 6", "form iii", "form 3", "nagoya", "pic", "mat", "benefit sharing"
        ],
        "Patent": [
            "patent", "patents act", "section 3(d)", "3(d)", "inventive step", "novelty",
            "prior art", "claims", "specification", "infringement", "prosecution", "cgpdtm",
            "section 25", "pre-grant", "post-grant", "compulsory license", "section 84"
        ],
        "Geographical Indications": [
            "geographical indication", "gi tag", "gi act", "origin", "darjeeling",
            "basmati", "saffron", "goods", "geographical origin"
        ],
        "Trademarks": [
            "trademark", "brand", "logo", "mark", "madrid", "madrid protocol", "infringement",
            "class 5", "nice classification", "passing off"
        ],
        "Plant Varieties": [
            "plant variety", "farmers' rights", "ppv&fr", "breeder", "crop variety",
            "farmer rights", "seed", "propagation"
        ],
        "Industrial Designs": [
            "industrial design", "design act", "ornamental", "aesthetic", "shape", "configuration"
        ],
        "Copyright": [
            "copyright", "literary", "dramatic", "musical", "fair dealing", "author"
        ],
        "Trade Secrets": [
            "trade secret", "confidential information", "nda", "undisclosed information"
        ],
        "International IP & Treaties": [
            "pct", "patent cooperation treaty", "trips", "wipo", "wto", "paris convention",
            "hague", "international application", "isa", "wipo lex"
        ]
    }

    def classify(self, query: str) -> Dict[str, Any]:
        """
        Determine the primary and secondary domains for a user query.
        Returns a dictionary with primary domain, confidence, and detected domains.
        """
        text_lower = query.lower()
        domain_scores: Dict[str, float] = {d: 0.0 for d in self.DOMAINS}

        for domain, keywords in self.KEYWORD_MAP.items():
            for kw in keywords:
                if re.search(r"\b" + re.escape(kw) + r"\b", text_lower):
                    # Exact phrase matches get high score
                    domain_scores[domain] += 2.0
                elif kw in text_lower:
                    domain_scores[domain] += 1.0

        # Sort domains by score
        sorted_domains = sorted(domain_scores.items(), key=lambda x: x[1], reverse=True)
        top_domain, top_score = sorted_domains[0]

        if top_score == 0:
            top_domain = "General IP"

        # Check for multi-domain synergy, e.g. Ayurveda + Patent or Biodiversity + Patent
        detected = [d for d, s in sorted_domains if s > 0][:3]
        if not detected:
            detected = ["General IP"]

        return {
            "primary_domain": top_domain,
            "all_detected": detected,
            "is_traditional_knowledge": "Traditional Knowledge" in detected or "Ayurveda" in detected,
            "is_biodiversity": "Biodiversity & ABS" in detected
        }


# Global singleton instance
domain_classifier = DomainClassifier()
