"""
IP-SAKTI SAHAYAK
Prior-Art Discovery Engine for Traditional Formulations
======================================================
Analyzes herbal/Ayurvedic formulations, extracts key active botanicals and therapeutic
claims, queries indexed patents and TKDL references, and evaluates Section 3(p) / 3(e) risks.
"""

import re
import logging
from typing import Dict, Any, List
from rag.hybrid_search import hybrid_searcher

logger = logging.getLogger(__name__)


class PriorArtSearcher:
    """Discovers potentially relevant prior art for traditional and natural formulations."""

    # Curated Botanical & Traditional Knowledge Index
    KNOWN_BOTANICAL_PRIOR_ART = {
        "ashwagandha": {
            "scientific_name": "Withania somnifera",
            "traditional_uses": "Rasayana, stress relief, adaptogen, vitality, anti-inflammatory",
            "classical_texts": "Charaka Samhita, Bhavaprakasha Nighantu",
            "tkdl_ipc_classes": "A61K 36/81, A61P 25/00",
            "sec_3p_risk": "High if claimed for general vitality or stress relief without synergistic novelty."
        },
        "turmeric": {
            "scientific_name": "Curcuma longa (Curcumin)",
            "traditional_uses": "Wound healing, antiseptic, anti-inflammatory, digestive tonic",
            "classical_texts": "Sushruta Samhita, Ashtanga Hridaya",
            "tkdl_ipc_classes": "A61K 36/9066, A61P 31/00",
            "sec_3p_risk": "Very High (Historic USPTO Turmeric Patent revocation landmark case)."
        },
        "neem": {
            "scientific_name": "Azadirachta indica",
            "traditional_uses": "Antifungal, antibacterial, dental hygiene, insecticide",
            "classical_texts": "Charaka Samhita, Atharva Veda",
            "tkdl_ipc_classes": "A61K 36/58, A01N 65/26",
            "sec_3p_risk": "Very High (Historic EPO Neem Patent revocation case)."
        },
        "tulsi": {
            "scientific_name": "Ocimum sanctum",
            "traditional_uses": "Respiratory wellness, immunomodulatory, antimicrobial, fever reducer",
            "classical_texts": "Charaka Samhita, Dhanvantari Nighantu",
            "tkdl_ipc_classes": "A61K 36/53, A61P 11/00",
            "sec_3p_risk": "High for cold, cough, and immune claims."
        },
        "triphala": {
            "scientific_name": "Emblica officinalis + Terminalia chebula + Terminalia bellirica",
            "traditional_uses": "Digestive regulation, antioxidant, ophthalmic health",
            "classical_texts": "Charaka Samhita (Sutrasthana), Sharangdhara Samhita",
            "tkdl_ipc_classes": "A61K 36/185, A61P 1/00",
            "sec_3p_risk": "Definitive bar under Section 3(e) and 3(p) as a classical admixture."
        }
    }

    def analyze_formulation(self, formulation_description: str) -> Dict[str, Any]:
        """
        Analyze an invention description and identify prior-art overlaps.
        """
        text_lower = formulation_description.lower()

        # 1. Identify botanical ingredients
        detected_ingredients = []
        for herb, details in self.KNOWN_BOTANICAL_PRIOR_ART.items():
            if herb in text_lower or details["scientific_name"].lower() in text_lower:
                detected_ingredients.append({
                    "name": herb.capitalize(),
                    "scientific_name": details["scientific_name"],
                    "traditional_uses": details["traditional_uses"],
                    "classical_texts": details["classical_texts"],
                    "sec_3p_risk": details["sec_3p_risk"]
                })

        # 2. Query Hybrid Knowledge Store for Patent and Statutory Citations
        retrieved_prior_art = hybrid_searcher.search(
            query=formulation_description,
            top_k=5,
            domain_filter="Traditional Knowledge"
        )

        # 3. Assess Section 3(p) & 3(e) Patentability Hurdles
        patentability_barriers = []
        if detected_ingredients:
            patentability_barriers.append({
                "statute": "Section 3(p), The Patents Act, 1970",
                "risk_level": "CRITICAL",
                "explanation": "Claims based on traditionally known medicinal herbs are barred unless an inventive, non-obvious synergistic therapeutic ratio is experimentally proven."
            })

        if len(detected_ingredients) > 1:
            patentability_barriers.append({
                "statute": "Section 3(e), The Patents Act, 1970",
                "risk_level": "HIGH",
                "explanation": "Mere admixtures of known botanical substances resulting only in the aggregation of their individual properties are strictly unpatentable."
            })

        patentability_barriers.append({
            "statute": "Section 6, Biological Diversity Act, 2002",
            "risk_level": "MANDATORY COMPLIANCE",
            "explanation": "Mandatory prior approval (Form III) from the National Biodiversity Authority is required before patent grant in India or abroad."
        })

        return {
            "success": True,
            "query_analyzed": formulation_description,
            "detected_botanicals": detected_ingredients,
            "patentability_barriers": patentability_barriers,
            "retrieved_prior_art_chunks": [
                {
                    "title": c.get("document_title"),
                    "section": c.get("section"),
                    "snippet": c.get("text")[:240] + "...",
                    "url": c.get("source_url")
                }
                for c in retrieved_prior_art
            ],
            "conclusion_status": "Potentially relevant prior art identified.",
            "disclaimer": "This search is preliminary and does not constitute a definitive legal opinion on novelty or inventive step."
        }


# Global singleton instance
prior_art_searcher = PriorArtSearcher()
