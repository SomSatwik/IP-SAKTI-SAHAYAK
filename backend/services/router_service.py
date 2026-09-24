"""
IP-SAKTI SAHAYAK
Query Understanding & Intent Router Service
============================================
Parses user queries and case descriptions to:
1. Extract key entities (product name, ingredients, dosage form, intended use, claims, commercial intent).
2. Determine required intelligence pipelines (IP, Regulatory, International, Document).
3. Detect applicable jurisdictions (India, USA, EU, WIPO/PCT).
"""

import re
from typing import Dict, Any, List
from ..models import IntentRoutingResult, ProductDetails

class RouterService:
    KNOWN_HERBS = {
        "ashwagandha": "Withania somnifera",
        "brahmi": "Bacopa monnieri",
        "turmeric": "Curcuma longa",
        "curcumin": "Curcuma longa extract",
        "neem": "Azadirachta indica",
        "tulsi": "Ocimum sanctum",
        "amla": "Phyllanthus emblica",
        "shatavari": "Asparagus racemosus",
        "guduchi": "Tinospora cordifolia",
        "triphala": "Classical Triphala Compound",
        "trikatu": "Classical Trikatu Compound",
        "licorice": "Glycyrrhiza glabra",
        "yashtimadhu": "Glycyrrhiza glabra",
        "guggulu": "Commiphora mukul",
        "boswellia": "Boswellia serrata",
        "shallaki": "Boswellia serrata",
        "ginger": "Zingiber officinale",
        "shunthi": "Zingiber officinale"
    }

    DOSAGE_FORMS = [
        "tablet", "vati", "capsule", "syrup", "arishta", "asava", "oil", "taila",
        "ghrita", "ghee", "cream", "ointment", "lepa", "churna", "powder", "extract",
        "decoction", "kwatha", "tincture", "topical"
    ]

    IP_KEYWORDS = [
        "patent", "patenting", "patentability", "inventive step", "novelty",
        "prior art", "section 3(p)", "section 3(e)", "section 3(d)", "claims",
        "tkdl", "traditional knowledge digital library", "infringement",
        "cgpdtm", "ip india", "provisional", "pct", "form 1", "form 2", "form 18a"
    ]

    REGULATORY_KEYWORDS = [
        "regulatory", "regulation", "commercialize", "commercialization", "sell", "market",
        "license", "licensing", "sla", "state licensing authority", "ayush license",
        "rule 158b", "rule 158-b", "rule 161", "schedule t", "gmp", "manufacturing license",
        "fssai", "ayurveda aahara", "compliance", "cdsco", "clinical trial", "approval",
        "ayurvedic medicine", "proprietary medicine", "classical medicine", "nba", "biodiversity",
        "form iii", "form 3", "abs", "access and benefit sharing", "sbb"
    ]

    INTERNATIONAL_KEYWORDS = [
        "usa", "united states", "us", "fda", "dshea", "ndin", "uspto",
        "europe", "eu", "thmpd", "ema", "uk", "mhra", "export", "global",
        "international", "cross-border", "foreign", "abroad", "overseas"
    ]

    def route_query(self, query: str, context_details: Dict[str, Any] = None) -> IntentRoutingResult:
        q_lower = query.lower()
        context_details = context_details or {}

        # 1. Entity Extraction
        entities = self.extract_entities(query)
        if context_details.get("ingredients"):
            entities["ingredients"] = list(set(entities["ingredients"] + context_details["ingredients"]))
        if context_details.get("targetMarkets"):
            entities["jurisdictions"] = list(set(entities["jurisdictions"] + context_details["targetMarkets"]))

        # 2. Pipeline Requirement Assessment
        ip_hits = sum(1 for kw in self.IP_KEYWORDS if kw in q_lower)
        reg_hits = sum(1 for kw in self.REGULATORY_KEYWORDS if kw in q_lower)
        intl_hits = sum(1 for kw in self.INTERNATIONAL_KEYWORDS if kw in q_lower)

        # Default rules
        ip_required = ip_hits > 0 or ("protect" in q_lower or "patent" in q_lower)
        regulatory_required = reg_hits > 0 or any(w in q_lower for w in ["commercial", "sell", "market", "license", "licensing", "approval", "compliance", "gmp", "label"])
        international_required = intl_hits > 0 or any(j.lower() in ["usa", "us", "eu", "europe"] for j in entities["jurisdictions"])
        
        # Document and claim auditing trigger
        is_doc_query = any(k in q_lower for k in ["document", "label", "claim", "claims", "packaging", "brochure", "advertisement"])
        is_audit_action = any(k in q_lower for k in ["analyze", "analyzing", "audit", "auditing", "evaluate", "evaluating", "review", "check", "screen", "screening"])
        document_analysis = bool(context_details.get("documents")) or (is_doc_query and is_audit_action)
        if document_analysis:
            regulatory_required = True

        # Balanced routing: If neither is strongly hit but query describes a product/formulation
        if not ip_required and not regulatory_required:
            if entities["ingredients"] or "formulation" in q_lower or "product" in q_lower or "ayurved" in q_lower:
                ip_required = True
                regulatory_required = True
            else:
                ip_required = True

        # Determine Primary Intent
        if ip_required and regulatory_required:
            primary_intent = "combined_ip_regulatory"
        elif regulatory_required:
            primary_intent = "regulatory_only"
        elif ip_required:
            primary_intent = "ip_only"
        else:
            primary_intent = "general_intelligence"

        detected_jurisdictions = entities["jurisdictions"]
        if international_required and "USA" not in detected_jurisdictions and ("usa" in q_lower or "us" in q_lower or "united states" in q_lower):
            detected_jurisdictions.append("USA")

        return IntentRoutingResult(
            ip_required=ip_required,
            regulatory_required=regulatory_required,
            international_required=international_required,
            document_analysis=document_analysis,
            primary_intent=primary_intent,
            detected_jurisdictions=detected_jurisdictions,
            extracted_entities=entities
        )

    def extract_entities(self, query: str) -> Dict[str, Any]:
        q_lower = query.lower()

        # Ingredients
        found_herbs = []
        for herb_alias in self.KNOWN_HERBS.keys():
            if re.search(r"\b" + re.escape(herb_alias) + r"\b", q_lower):
                found_herbs.append(herb_alias.title())

        # Dosage form
        detected_form = "Tablet / Vati"
        for form in self.DOSAGE_FORMS:
            if re.search(r"\b" + re.escape(form) + r"\b", q_lower):
                detected_form = form.title()
                break

        # Jurisdictions
        jurisdictions = ["India"]
        if "usa" in q_lower or "united states" in q_lower or "u.s." in q_lower or "us " in q_lower:
            jurisdictions.append("USA")
        if "europe" in q_lower or "eu" in q_lower or "germany" in q_lower or "uk" in q_lower:
            jurisdictions.append("Europe (EU)")

        # Commercial Intent
        commercial_intent = any(kw in q_lower for kw in ["sell", "commercial", "market", "manufactur", "export", "sale", "launch"])

        # Product Title
        product_name = "Ayurvedic Herbal Formulation"
        if found_herbs:
            product_name = f"{' + '.join(found_herbs)} Synergistic Formulation"
        elif "formulation" in q_lower:
            product_name = "Ayurvedic Proprietary Formulation"

        # Claims extraction
        claims = []
        claim_patterns = [
            r"(?:cures?|cure for|treats?|manages?|prevents?|anti-[\w]+|remedy for) ([a-zA-Z\s]+?)(?:\.|\band\b|,|$)",
            r"(?:indicated for|supports|boosts) ([a-zA-Z\s]+?)(?:\.|\band\b|,|$)"
        ]
        for pattern in claim_patterns:
            matches = re.findall(pattern, q_lower)
            for m in matches:
                clean_claim = m.strip()
                if len(clean_claim) > 3 and clean_claim not in claims:
                    claims.append(clean_claim.title())

        for disease in ["diabetes", "cancer", "hypertension", "arthritis", "obesity", "cure"]:
            if disease in q_lower and not any(disease in c.lower() for c in claims):
                claims.append(f"Claim relating to {disease.title()}")

        return {
            "product_name": product_name,
            "ingredients": found_herbs,
            "dosage_form": detected_form,
            "jurisdictions": list(set(jurisdictions)),
            "commercial_intent": commercial_intent,
            "claims": claims
        }

    def create_product_details(self, entities: Dict[str, Any]) -> ProductDetails:
        return ProductDetails(
            name=entities.get("product_name", "Ayurvedic Herbal Formulation"),
            type="Classical Ayurvedic Medicine" if any(h in ["Triphala", "Trikatu"] for h in entities.get("ingredients", [])) else "Ayurvedic Proprietary Medicine",
            ingredients=entities.get("ingredients", []),
            dosage_form=entities.get("dosage_form", "Tablet / Vati"),
            intended_use="Therapeutic health management and revitalization",
            claims=entities.get("claims", []),
            commercial_intent=entities.get("commercial_intent", True)
        )

    def extract_product(self, query: str) -> ProductDetails:
        entities = self.extract_entities(query)
        return self.create_product_details(entities)

    route_intent = route_query

router_service = RouterService()
