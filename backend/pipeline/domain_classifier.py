"""
IP-SAKTI SAHAYAK
Domain Classifier
=================
Identifies intellectual property, Ayurveda, and regulatory domains from query intent
using statutory keywords, semantic heuristics, and specialized taxonomy.
"""

import re
from typing import Dict, List, Any, Optional


class DomainClassifier:
    """Classifies user queries into Ayurveda, IP, and Regulatory domains."""

    DOMAINS = [
        "Ayurveda",
        "IP",
        "Regulatory",
        "Patent",
        "Traditional Knowledge",
        "Biodiversity & ABS",
        "Geographical Indications",
        "Trademarks",
        "Copyright",
        "Plant Varieties"
    ]

    KEYWORD_MAP = {
        "Ayurveda": [
            "ayurveda", "ayurvedic", "herbal", "formulation", "ashwagandha", "neem",
            "turmeric", "curcumin", "charaka", "sushruta", "ashtanga", "rasa", "bhasma",
            "ayush", "ism", "panchakarma", "medicinal plant", "vpk", "tulsi", "triphala",
            "guduchi", "shatavari", "brahmi", "amla", "herbs", "botanical"
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
        "IP": [
            "patent", "patents act", "section 3(d)", "3(d)", "inventive step", "novelty",
            "prior art", "claims", "specification", "infringement", "prosecution", "cgpdtm",
            "section 25", "pre-grant", "post-grant", "compulsory license", "section 84",
            "trademark", "copyright", "geographical indication", "gi tag", "industrial design"
        ],
        "Regulatory": [
            "regulatory", "regulation", "ayush", "cdsco", "fssai", "license", "licensing",
            "rule 158b", "rule 158-b", "pharmacopoeia", "gmp", "compliance", "approval",
            "drugs and cosmetics", "schedule t", "clinical trial"
        ]
    }

    def classify(self, query: str) -> Dict[str, Any]:
        """
        Determine the primary and secondary domains for a user query.
        Returns a dictionary with primary domain (Ayurveda/IP/Regulatory),
        confidence, and detected domains.
        """
        text_lower = query.lower()
        domain_scores: Dict[str, float] = {
            "Ayurveda": 0.0,
            "IP": 0.0,
            "Regulatory": 0.0
        }

        # Check keyword matches
        for cat, keywords in self.KEYWORD_MAP.items():
            mapped_target = "IP" if cat in ["IP", "Traditional Knowledge"] else ("Regulatory" if cat in ["Regulatory", "Biodiversity & ABS"] else "Ayurveda")
            for kw in keywords:
                if re.search(r"\b" + re.escape(kw) + r"\b", text_lower):
                    domain_scores[mapped_target] += 2.0
                elif kw in text_lower:
                    domain_scores[mapped_target] += 1.0

        sorted_domains = sorted(domain_scores.items(), key=lambda x: x[1], reverse=True)
        top_domain, top_score = sorted_domains[0]

        if top_score == 0:
            top_domain = "Ayurveda"

        detected = [d for d, s in sorted_domains if s > 0]
        if not detected:
            detected = ["Ayurveda", "IP"]

        return {
            "primary_domain": top_domain,
            "all_detected": detected,
            "scores": domain_scores,
            "is_traditional_knowledge": "Traditional Knowledge" in text_lower or "3(p)" in text_lower or "tkdl" in text_lower,
            "is_biodiversity": "biodiversity" in text_lower or "nba" in text_lower or "abs" in text_lower
        }


# Global singleton instance
domain_classifier = DomainClassifier()
