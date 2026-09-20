"""
IP-SAKTI SAHAYAK
Prior-Art Discovery Service
===========================
Discovers prior-art patents, botanical classifications, and Section 3(p)/3(e)
patentability hurdles for Ayurvedic formulations and natural ingredients.
"""

import re
from typing import Dict, Any, List
from ..models import PatentRecord, BotanicalInfo, PriorArtSearchResponse


class PriorArtService:
    """Discovers and filters prior art patents for herbal formulations."""

    KNOWN_BOTANICALS = {
        "ashwagandha": {
            "scientific_name": "Withania somnifera",
            "traditional_uses": "Rasayana, adaptogen, vitality, neuroprotection, anti-stress",
            "classical_texts": "Charaka Samhita (Chikitsasthana), Bhavaprakasha Nighantu",
            "sec_3p_risk": "High if claimed for general vitality or stress relief without synergistic novelty."
        },
        "turmeric": {
            "scientific_name": "Curcuma longa (Curcumin)",
            "traditional_uses": "Wound healing, antiseptic, anti-inflammatory, digestive tonic",
            "classical_texts": "Sushruta Samhita (Sutrasthana), Ashtanga Hridaya",
            "sec_3p_risk": "Very High (Landmark CSIR/TKDL revocation of USPTO Patent 5,401,504)."
        },
        "neem": {
            "scientific_name": "Azadirachta indica",
            "traditional_uses": "Antifungal, antibacterial, dental hygiene, natural pesticide",
            "classical_texts": "Charaka Samhita, Atharva Veda",
            "sec_3p_risk": "Very High (Landmark EPO Patent 0436257 revocation based on Indian TK)."
        },
        "tulsi": {
            "scientific_name": "Ocimum sanctum (Holy Basil)",
            "traditional_uses": "Respiratory health, immunomodulation, antimicrobial, adaptogen",
            "classical_texts": "Charaka Samhita, Dhanvantari Nighantu",
            "sec_3p_risk": "High for cold, cough, and general immune booster claims."
        },
        "triphala": {
            "scientific_name": "Emblica officinalis + Terminalia chebula + Terminalia bellirica",
            "traditional_uses": "Digestive regulation, ophthalmic health, antioxidant",
            "classical_texts": "Charaka Samhita (Sutrasthana), Sharangdhara Samhita",
            "sec_3p_risk": "Definitive bar under Section 3(e) and 3(p) as a classical multi-herb admixture."
        },
        "guduchi": {
            "scientific_name": "Tinospora cordifolia (Giloy)",
            "traditional_uses": "Immunostimulant, antipyretic, anti-inflammatory, liver protectant",
            "classical_texts": "Charaka Samhita, Sushruta Samhita, Raj Nighantu",
            "sec_3p_risk": "High for fever, immunity, or hepatoprotective claims without synergistic ratios."
        },
        "shatavari": {
            "scientific_name": "Asparagus racemosus",
            "traditional_uses": "Female reproductive tonic, galactagogue, adaptogen",
            "classical_texts": "Charaka Samhita, Ashtanga Sangraha",
            "sec_3p_risk": "High for traditional galactagogue and hormonal balance indications."
        },
        "brahmi": {
            "scientific_name": "Bacopa monnieri",
            "traditional_uses": "Medhya Rasayana, cognitive enhancement, memory, anxiolytic",
            "classical_texts": "Charaka Samhita, Sushruta Samhita",
            "sec_3p_risk": "High for memory or cognition improvement unless novel delivery system proven."
        },
        "amla": {
            "scientific_name": "Phyllanthus emblica (Indian Gooseberry)",
            "traditional_uses": "Vitamin C rich rasayana, digestive, hair wellness, antioxidant",
            "classical_texts": "Charaka Samhita, Sushruta Samhita",
            "sec_3p_risk": "High for general antioxidant and rejuvenation claims."
        }
    }

    CURATED_PATENTS: List[Dict[str, Any]] = [
        {
            "patent_number": "IN 342158",
            "title": "A Synergistic Herbal Composition of Withania somnifera and Bacopa monnieri for Enhanced Cognitive Function",
            "applicant": "Council of Scientific and Industrial Research (CSIR)",
            "status": "Granted",
            "filing_date": "2018-04-12",
            "jurisdiction": "India",
            "ipc_class": "A61K 36/81",
            "abstract": "A synergistic herbal composition comprising standardized hydro-alcoholic extracts of Withania somnifera and Bacopa monnieri in a specific 3:2 ratio demonstrating statistically validated neuroprotective efficacy beyond individual additive effects.",
            "keywords": ["ashwagandha", "brahmi", "synergy", "cognitive"]
        },
        {
            "patent_number": "US 5,401,504",
            "title": "Use of Turmeric in Wound Healing",
            "applicant": "University of Mississippi Medical Center",
            "status": "Revoked under Sec 3(p) / Prior Art",
            "filing_date": "1993-12-28",
            "jurisdiction": "United States (USPTO)",
            "ipc_class": "A61K 36/9066",
            "abstract": "Claimed the administration of an effective amount of turmeric for healing topical wounds. Successfully revoked by CSIR and TKDL by proving antiquity in Sushruta Samhita.",
            "keywords": ["turmeric", "curcumin", "wound healing"]
        },
        {
            "patent_number": "EP 0436257",
            "title": "Method for Controlling Fungi on Plants by the Aid of a Hydrophobic Extracted Neem Oil",
            "applicant": "W.R. Grace & Co.",
            "status": "Revoked under Prior Art (TKDL)",
            "filing_date": "1990-12-20",
            "jurisdiction": "Europe (EPO)",
            "ipc_class": "A01N 65/00",
            "abstract": "Claimed fungicidal effect of neem oil. Revoked by EPO Opposition Division after proof of ancient Indian traditional usage submitted by Indian authorities.",
            "keywords": ["neem", "antifungal", "pesticide"]
        },
        {
            "patent_number": "IN 201941032145",
            "title": "Novel Phytosomal Formulation of Ocimum sanctum with Enhanced Bioavailability for Respiratory Disorders",
            "applicant": "Dabur Research Foundation",
            "status": "Pending Examination",
            "filing_date": "2019-08-08",
            "jurisdiction": "India",
            "ipc_class": "A61K 9/127",
            "abstract": "Formulation overcoming Section 3(p) objections by establishing a novel nanostructured lipid carrier delivery system for Tulsi extracts exhibiting 400% improved pharmacokinetic uptake.",
            "keywords": ["tulsi", "respiratory", "phytosome", "bioavailability"]
        },
        {
            "patent_number": "IN 202111045231",
            "title": "Triphala-Derived Standardized Phenolic Fractions for Metabolic Syndrome Management",
            "applicant": "Patanjali Research Institute",
            "status": "Opposed under Sec 25(1)",
            "filing_date": "2021-10-05",
            "jurisdiction": "India",
            "ipc_class": "A61K 36/185",
            "abstract": "Composition extracted from Triphala fruits. Currently facing pre-grant opposition under Section 3(p) and Section 3(e) alleging mere admixture of classical formulations.",
            "keywords": ["triphala", "metabolic", "amla"]
        },
        {
            "patent_number": "IN 389201",
            "title": "Standardized Aqueous Extract of Tinospora cordifolia (Giloy) for Immunomodulation and Process Thereof",
            "applicant": "National Institute of Pharmaceutical Education and Research (NIPER)",
            "status": "Granted",
            "filing_date": "2017-06-22",
            "jurisdiction": "India",
            "ipc_class": "A61K 36/59",
            "abstract": "Granted process patent specifically claiming a unique dual-solvent fractionating process that isolates high-potency cordifolioside polysaccharide fractions.",
            "keywords": ["guduchi", "giloy", "immunity", "extract"]
        },
        {
            "patent_number": "IN 202221067890",
            "title": "Synergistic Polyherbal Composition of Shatavari and Ashwagandha for Postpartum Vitality",
            "applicant": "Himalaya Wellness Company",
            "status": "Pending Examination",
            "filing_date": "2022-11-19",
            "jurisdiction": "India",
            "ipc_class": "A61K 36/896",
            "abstract": "A tablet formulation containing microencapsulated Asparagus racemosus and Withania somnifera with validated hormonal modulation data in animal models.",
            "keywords": ["shatavari", "ashwagandha", "vitality", "postpartum"]
        }
    ]

    def search(self, query: str) -> PriorArtSearchResponse:
        """Search patents and evaluate traditional knowledge overlap."""
        q_lower = query.lower()

        # 1. Detect botanicals
        detected: List[BotanicalInfo] = []
        for name, data in self.KNOWN_BOTANICALS.items():
            if name in q_lower or data["scientific_name"].lower() in q_lower:
                detected.append(BotanicalInfo(
                    name=name.capitalize(),
                    scientific_name=data["scientific_name"],
                    traditional_uses=data["traditional_uses"],
                    classical_texts=data["classical_texts"],
                    sec_3p_risk=data["sec_3p_risk"]
                ))

        # Dynamic discovery via Groq LLM if botanical is not in curated list
        if not detected:
            ai_botanical = self._detect_with_groq(query)
            if ai_botanical:
                detected.extend(ai_botanical)

        # 2. Match patents
        matching_patents: List[PatentRecord] = []
        for p in self.CURATED_PATENTS:
            # Check match by query keyword or detected botanicals
            p_text = f"{p['title']} {p['abstract']} {p['applicant']} {' '.join(p['keywords'])}".lower()
            matched = any(kw in p_text for kw in q_lower.split()) or any(b.name.lower() in p_text for b in detected)
            if matched or not detected and len(matching_patents) < 3:
                matching_patents.append(PatentRecord(
                    patent_number=p["patent_number"],
                    title=p["title"],
                    applicant=p["applicant"],
                    status=p["status"],
                    filing_date=p.get("filing_date"),
                    jurisdiction=p.get("jurisdiction", "India"),
                    ipc_class=p.get("ipc_class"),
                    abstract=p.get("abstract")
                ))

        if not matching_patents:
            # Fallback to top curated patents for education/demonstration
            matching_patents = [
                PatentRecord(
                    patent_number=p["patent_number"],
                    title=p["title"],
                    applicant=p["applicant"],
                    status=p["status"],
                    filing_date=p.get("filing_date"),
                    jurisdiction=p.get("jurisdiction", "India"),
                    ipc_class=p.get("ipc_class"),
                    abstract=p.get("abstract")
                )
                for p in self.CURATED_PATENTS[:3]
            ]

        # 3. Assess patentability barriers
        barriers = []
        if detected:
            barriers.append({
                "statute": "Section 3(p), The Patents Act, 1970",
                "risk_level": "HIGH",
                "explanation": f"Invention uses traditionally known herbs ({', '.join(b.name for b in detected)}). Synergistic therapeutic data or a novel extraction mechanism must be documented to overcome Section 3(p) objections."
            })
        if len(detected) > 1:
            barriers.append({
                "statute": "Section 3(e), The Patents Act, 1970",
                "risk_level": "CRITICAL",
                "explanation": "Mere admixture of two or more known botanical substances resulting only in the aggregation of their properties is strictly unpatentable."
            })
        barriers.append({
            "statute": "Section 6, Biological Diversity Act, 2002",
            "risk_level": "MANDATORY COMPLIANCE",
            "explanation": "Prior approval from National Biodiversity Authority (NBA Form 3) is required before patent grant in India or abroad."
        })

        return PriorArtSearchResponse(
            query=query,
            total_found=len(matching_patents),
            detected_botanicals=detected,
            patents=matching_patents,
            patentability_barriers=barriers,
            conclusion_status="Prior art records and TKDL references retrieved.",
            disclaimer="This patent search is preliminary and for guidance only. A formal freedom-to-operate (FTO) search by an IP attorney is required before commercialization."
        )

    def _detect_with_groq(self, query: str) -> List[BotanicalInfo]:
        import os
        import json
        from ..pipeline.config import load_environment
        load_environment()
        groq_key = os.getenv("GROQ_API_KEY")
        if not groq_key:
            return []

        prompt = (
            f"Analyze the following user query for any medicinal plant, herb, or natural biological ingredients: '{query}'. "
            "If any plant/herb is present, return a JSON list of objects with fields: "
            "'name', 'scientific_name', 'traditional_uses', 'classical_texts' (e.g. Charaka Samhita, Sushruta Samhita, Bhavaprakasha), "
            "and 'sec_3p_risk' (assessment under Indian Patents Act Section 3(p)). "
            "Return ONLY a valid JSON array of objects. If no plants or biological ingredients are mentioned, return []."
        )

        try:
            from langchain_groq import ChatGroq
            llm = ChatGroq(api_key=groq_key, model=os.getenv("MODEL_NAME", "qwen/qwen3.8-27b"), temperature=0.1, max_tokens=600)
            res = llm.invoke(prompt)
            content = res.content.strip()
            # Extract JSON from code block if present
            if "```json" in content:
                content = content.split("```json")[1].split("```")[0].strip()
            elif "```" in content:
                content = content.split("```")[1].split("```")[0].strip()
            
            data = json.loads(content)
            results = []
            if isinstance(data, list):
                for item in data:
                    if isinstance(item, dict) and "name" in item:
                        results.append(BotanicalInfo(
                            name=item.get("name", "Unknown Botanical"),
                            scientific_name=item.get("scientific_name", "N/A"),
                            traditional_uses=item.get("traditional_uses", "Traditional Ayurvedic preparation"),
                            classical_texts=item.get("classical_texts", "Classical Ayurvedic Samhitas"),
                            sec_3p_risk=item.get("sec_3p_risk", "Section 3(p) prior art objection likely unless novel synergy demonstrated.")
                        ))
            return results
        except Exception:
            return []


prior_art_service = PriorArtService()

