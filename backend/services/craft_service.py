"""
Craft Service: Formulation Novelty Scanner / Infinite Craft Engine
Evaluates combinations of Ayurvedic botanicals for classical TKDL status,
Section 3(p) bars, Section 3(e) mere admixture criteria, and unexpected synergy potential.
"""

from typing import List, Dict, Any, Optional
import re

BOTANICAL_APOTHECARY: Dict[str, Dict[str, Any]] = {
    "turmeric": {
        "canonical_name": "Turmeric",
        "sanskrit_name": "Haridra",
        "botanical_name": "Curcuma longa",
        "active_actives": "Curcuminoids (Curcumin, Desmethoxycurcumin)",
        "classical_category": "Lekhaniya / Kushtaghna / Varnya",
        "emoji": "🫚",
        "synonyms": ["turmeric", "haridra", "haldi", "curcuma", "curcuma longa", "curcumin"]
    },
    "black_pepper": {
        "canonical_name": "Black Pepper",
        "sanskrit_name": "Maricha",
        "botanical_name": "Piper nigrum",
        "active_actives": "Piperine, Chavicine",
        "classical_category": "Deepana / Shirovirechana / Pramathi",
        "emoji": "🌶️",
        "synonyms": ["black pepper", "maricha", "kali mirch", "piper nigrum", "piperine"]
    },
    "pipali": {
        "canonical_name": "Long Pepper",
        "sanskrit_name": "Pipali",
        "botanical_name": "Piper longum",
        "active_actives": "Piperlongumine, Piperine",
        "classical_category": "Kaphahara / Rasayana / Deepana",
        "emoji": "🌿",
        "synonyms": ["pipali", "pippali", "long pepper", "piper longum", "piperlongumine"]
    },
    "dry_ginger": {
        "canonical_name": "Dry Ginger",
        "sanskrit_name": "Shunthi",
        "botanical_name": "Zingiber officinale",
        "active_actives": "Gingerols, Shogaols, Zingiberene",
        "classical_category": "Triptighna / Arshoghna / Deepana",
        "emoji": "🫚",
        "synonyms": ["dry ginger", "shunthi", "sonth", "zingiber officinale", "ginger"]
    },
    "amla": {
        "canonical_name": "Amla",
        "sanskrit_name": "Amalaki",
        "botanical_name": "Emblica officinalis",
        "active_actives": "Ascorbic acid, Emblicanin A & B, Gallic acid",
        "classical_category": "Vayasthapana / Chakshushya / Rasayana",
        "emoji": "🍏",
        "synonyms": ["amla", "amalaki", "emblica officinalis", "phyllanthus emblica", "indian gooseberry"]
    },
    "haritaki": {
        "canonical_name": "Haritaki",
        "sanskrit_name": "Haritaki",
        "botanical_name": "Terminalia chebula",
        "active_actives": "Chebulic acid, Tannins, Corilagin",
        "classical_category": "Rasayana / Anulomana / Prajasthapana",
        "emoji": "🌰",
        "synonyms": ["haritaki", "harad", "terminalia chebula", "chebulic myrobalan"]
    },
    "bibhitaki": {
        "canonical_name": "Bibhitaki",
        "sanskrit_name": "Bibhitaki",
        "botanical_name": "Terminalia bellirica",
        "active_actives": "Ellagic acid, Gallic acid, Belleric acid",
        "classical_category": "Chakshushya / Bhedana / Kaphapittahara",
        "emoji": "🥜",
        "synonyms": ["bibhitaki", "baheda", "terminalia bellirica", "belleric myrobalan"]
    },
    "ashwagandha": {
        "canonical_name": "Ashwagandha",
        "sanskrit_name": "Ashwagandha",
        "botanical_name": "Withania somnifera",
        "active_actives": "Withaferin A, Withanolides, Somniferine",
        "classical_category": "Balya / Rasayana / Brimhaniya",
        "emoji": "🌾",
        "synonyms": ["ashwagandha", "asgandh", "withania somnifera", "indian ginseng"]
    },
    "brahmi": {
        "canonical_name": "Brahmi",
        "sanskrit_name": "Brahmi",
        "botanical_name": "Bacopa monnieri",
        "active_actives": "Bacosides A & B, Bacosaponins",
        "classical_category": "Medhya / Ayushya / Prajasthapana",
        "emoji": "🍃",
        "synonyms": ["brahmi", "bacopa monnieri", "water hyssop", "jalanimba"]
    },
    "tulsi": {
        "canonical_name": "Holy Basil",
        "sanskrit_name": "Tulsi",
        "botanical_name": "Ocimum sanctum",
        "active_actives": "Eugenol, Ursolic acid, Rosmarinic acid",
        "classical_category": "Shvasahara / Kasahara / Krimighna",
        "emoji": "🌱",
        "synonyms": ["tulsi", "tulasi", "holy basil", "ocimum sanctum", "ocimum microphyllum"]
    },
    "neem": {
        "canonical_name": "Neem",
        "sanskrit_name": "Nimba",
        "botanical_name": "Azadirachta indica",
        "active_actives": "Azadirachtin, Nimbin, Nimbidol",
        "classical_category": "Kandughna / Tiktaskandha / Vranaropana",
        "emoji": "🌿",
        "synonyms": ["neem", "nimba", "azadirachta indica", "margosa"]
    },
    "guduchi": {
        "canonical_name": "Giloy",
        "sanskrit_name": "Guduchi",
        "botanical_name": "Tinospora cordifolia",
        "active_actives": "Tinosporide, Cordifolioside, Berberine",
        "classical_category": "Vayasthapana / Jvarahara / Dahaprashamana",
        "emoji": "🎋",
        "synonyms": ["guduchi", "giloy", "amrita", "tinospora cordifolia"]
    },
    "shatavari": {
        "canonical_name": "Shatavari",
        "sanskrit_name": "Shatavari",
        "botanical_name": "Asparagus racemosus",
        "active_actives": "Shatavarins I-IV, Sarsasapogenin",
        "classical_category": "Stanyajanana / Balya / Rasayana",
        "emoji": "🌾",
        "synonyms": ["shatavari", "satavar", "asparagus racemosus"]
    },
    "guggulu": {
        "canonical_name": "Indian Bdellium",
        "sanskrit_name": "Guggulu",
        "botanical_name": "Commiphora mukul",
        "active_actives": "E- and Z-Guggulsterones",
        "classical_category": "Medoroga / Sandhivata / Lekhaniya",
        "emoji": "🪵",
        "synonyms": ["guggulu", "guggul", "commiphora mukul", "commiphora wightii"]
    },
    "mulethi": {
        "canonical_name": "Licorice",
        "sanskrit_name": "Yashtimadhu",
        "botanical_name": "Glycyrrhiza glabra",
        "active_actives": "Glycyrrhizin, Glabridin, Liquiritin",
        "classical_category": "Kanthya / Chardinigrahana / Sandhaniya",
        "emoji": "🪵",
        "synonyms": ["mulethi", "yashtimadhu", "licorice", "glycyrrhiza glabra", "liquorice"]
    }
}

COMBINATIONS_RECIPES = [
    {
        "keys": {"amla", "haritaki", "bibhitaki"},
        "title": "Triphala Churna",
        "sanskrit_name": "Triphala (त्रिफला चूर्ण)",
        "status": "Known Classical Formulation",
        "classical_source": "Charaka Samhita Chikitsa Sthana 1:3; Sushruta Samhita Sutrasthana 38:56",
        "tkdl_status": "Section 3(p) Barred — Direct Classical Prescription",
        "patentability_risk_pct": 96,
        "synergy_score_pct": 12,
        "therapeutic_category": "Rasayana / Tridoshic Harmonizer / Digestive Tonic",
        "statutory_rationale": "Directly disclosed in ancient classical Ayurvedic compendia. Section 3(p) of the Indian Patents Act, 1970 strictly excludes traditional knowledge from patentability. Furthermore, under Section 3(e), simple admixtures of classical herbs are non-patentable unless non-obvious synergistic physiological effect is proven beyond prior art.",
        "statutory_requirements": [
            "Patents Act Section 3(p) Traditional Knowledge Bar",
            "Drugs & Cosmetics Rules, 1945 Rule 158B Classical ASU License",
            "Biological Diversity Act Section 6 / Form III clearance if commercializing biological resource"
        ],
        "suggested_queries": [
            "Triphala churna patentability under Section 3(p) and TKDL citations",
            "Rule 158B manufacturing licensing requirements for Triphala extract",
            "Overcoming Section 3(e) mere admixture bar for modified Triphala nano-formulations"
        ]
    },
    {
        "keys": {"dry_ginger", "black_pepper", "pipali"},
        "title": "Trikatu Churna",
        "sanskrit_name": "Trikatu (त्रिकटु चूर्ण)",
        "status": "Known Classical Formulation",
        "classical_source": "Bhavaprakasha Nighantu; Sharangadhara Samhita Madhyama Khanda 6:12",
        "tkdl_status": "Section 3(p) Barred — Direct Classical Formulation",
        "patentability_risk_pct": 94,
        "synergy_score_pct": 15,
        "therapeutic_category": "Deepana / Pachana / Bioavailability Enhancer",
        "statutory_rationale": "Tri-katu ('three acrids') is a classical Ayurvedic combination established since antiquity for digestive fire (agni) stimulation. Fully documented in TKDL. Barred under Section 3(p). Commercial exploitation requires classical ASU product licensing rather than proprietary patenting.",
        "statutory_requirements": [
            "Patents Act Section 3(p) Bar",
            "Classical Ayurvedic Pharmacopoeia of India (API) Monograph Compliance",
            "State Licensing Authority (SLA) ASU Manufacturing License"
        ],
        "suggested_queries": [
            "Trikatu classical formulation Section 3(p) prior art challenges",
            "Piperine-Gingerol synergy evidence standard under Indian Patent Law"
        ]
    },
    {
        "keys": {"turmeric", "black_pepper"},
        "title": "Curcumin-Piperine Synergistic Bioavailability Enhancer",
        "sanskrit_name": "Haridra-Maricha Synergistic Yoga",
        "status": "Novel Synergistic Formulation Candidate",
        "classical_source": "Modern Synergistic Validation of Traditional Bioavailability Concept",
        "tkdl_status": "Novel Candidate — Section 3(e) Synergistic Evidence Required",
        "patentability_risk_pct": 34,
        "synergy_score_pct": 92,
        "therapeutic_category": "Synergistic Bio-enhancer & Anti-Inflammatory",
        "statutory_rationale": "Piperine inhibits hepatic and intestinal glucuronidation of curcumin, yielding an unexpected >2000% human bioavailability increase. To survive Section 3(e) examination, the applicant must file comparative pharmacokinetic AUC curve data substantiating that the combination produces unexpected synergistic potency rather than a mere additive effect.",
        "statutory_requirements": [
            "Patents Act Section 3(e) Synergistic Potency Documentation (Pharmacokinetic AUC)",
            "NBA Form III Approval under Section 6 of Biological Diversity Act, 2002",
            "Rule 158B Proprietary ASU Safety & Efficacy Dossier"
        ],
        "suggested_queries": [
            "How to overcome Section 3(e) for Curcumin Piperine synergy",
            "National Biodiversity Authority Form III requirements for Curcuma longa and Piper nigrum"
        ]
    },
    {
        "keys": {"turmeric", "pipali"},
        "title": "Curcumin-Piperine Synergistic Bioavailability Enhancer",
        "sanskrit_name": "Haridra-Pipali Synergistic Yoga",
        "status": "Novel Synergistic Formulation Candidate",
        "classical_source": "Modern Synergistic Validation of Traditional Bioavailability Concept",
        "tkdl_status": "Novel Candidate — Section 3(e) Synergistic Evidence Required",
        "patentability_risk_pct": 36,
        "synergy_score_pct": 89,
        "therapeutic_category": "Synergistic Bio-enhancer & Anti-Inflammatory",
        "statutory_rationale": "Pipali contains active piperine and piperlongumine, synergistically boosting curcumin absorption across gut enterocytes. Qualifies as patentable subject matter if unexpected synergistic bioavailability is demonstrated with quantitative bio-assays.",
        "statutory_requirements": [
            "Section 3(e) Synergistic Potency Documentation",
            "NBA Form III Approval",
            "Rule 158B Clinical Trial Documentation"
        ],
        "suggested_queries": [
            "Curcumin Piper longum bioenhancement patent claims in India"
        ]
    },
    {
        "keys": {"ashwagandha", "brahmi"},
        "title": "Neuro-Adaptogenic Nootropic Complex",
        "sanskrit_name": "Medhya-Balya Samyoga (मेध्य-बल्य संयोग)",
        "status": "Novel Synergistic Formulation Candidate",
        "classical_source": "Medhya Rasayana Novel Combinatorial Protocol",
        "tkdl_status": "Novel Candidate — Requires Non-Obvious Synergy Substantiation",
        "patentability_risk_pct": 42,
        "synergy_score_pct": 85,
        "therapeutic_category": "Nootropic / Cognitive Enhancement & Neuroprotection",
        "statutory_rationale": "Synergistic withanolide-bacoside modulation across GABAergic receptors, acetylcholinesterase inhibition, and BDNF signaling. Patentable under Section 3(e) if cellular or clinical trials show synergistic neuroprotection over monotherapy.",
        "statutory_requirements": [
            "Section 3(e) In-vitro / In-vivo Neuroprotection Synergy Data",
            "Form III NBA Approval for Withania somnifera and Bacopa monnieri",
            "Rule 158B Safety Dossier for Proprietary ASU Drug"
        ],
        "suggested_queries": [
            "Patentability of Ashwagandha and Brahmi synergistic nootropic combinations",
            "Overcoming Section 3(p) objections with quantified receptor assay data"
        ]
    },
    {
        "keys": {"ashwagandha", "tulsi"},
        "title": "Adaptogenic Immunomodulatory & Stress Relief Complex",
        "sanskrit_name": "Rasayana-Ojasvardhaka Yoga (ओजोवर्धक योग)",
        "status": "Novel Synergistic Formulation Candidate",
        "classical_source": "Ayurvedic Rasayana & Immunomodulatory Adaptation",
        "tkdl_status": "Novel Candidate — Requires Section 3(e) Synergy Data",
        "patentability_risk_pct": 40,
        "synergy_score_pct": 82,
        "therapeutic_category": "Adaptogen / Cortisol Modulation & Cellular Immunity",
        "statutory_rationale": "Combined adaptogenic action downregulates chronic salivary cortisol and stimulates NK-cell activity. Not a classical single-shloka recipe; acceptable under Section 3(e) upon submission of comparative biomarker data.",
        "statutory_requirements": [
            "Section 3(e) Synergy Proof (Comparative In-Vivo Biomarkers)",
            "NBA Section 6 Biological Diversity Clearance",
            "ASU State Licensing Authority Proprietary Drug License"
        ],
        "suggested_queries": [
            "Biological Diversity Act compliance for Ashwagandha and Tulsi formulations",
            "Patent claims for stress and immune polyherbal extract"
        ]
    },
    {
        "keys": {"neem", "turmeric"},
        "title": "Antimicrobial Dermal Cleansing Complex",
        "sanskrit_name": "Haridra-Nimba Lepa (हरिद्रा-निम्ब लेप)",
        "status": "Classical Admixture with Novel Synergy Potential",
        "classical_source": "Kushtaghna Formulations (Chakradatta Kushtaroga Chikitsa)",
        "tkdl_status": "Classical Admixture — Section 3(p) & 3(e) Bar unless Novel Delivery",
        "patentability_risk_pct": 62,
        "synergy_score_pct": 68,
        "therapeutic_category": "Dermatological / Antimicrobial & Anti-Acne",
        "statutory_rationale": "Widely known in classical Ayurvedic dermatological remedies. Direct simple mixing faces Section 3(p) and Section 3(e) bars. To qualify for patent protection, the formulation must employ an inventive delivery mechanism (e.g. nano-emulsion, lipid carriers) exhibiting non-obvious dermal permeation.",
        "statutory_requirements": [
            "Overcome Section 3(p) via Inventive Delivery Carrier",
            "Section 3(e) Minimum Inhibitory Concentration (MIC) Synergy Proof",
            "AYUSH / CDSCO Topical Cosmetic or ASU Licensing"
        ],
        "suggested_queries": [
            "Patentability of Neem and Turmeric nano-formulations in India",
            "TKDL objections against Haridra Nimba topical applications"
        ]
    },
    {
        "keys": {"guduchi", "amla", "turmeric"},
        "title": "Prameha Metabolic Support Complex",
        "sanskrit_name": "Nishamalaki-Guduchi Pramehahara Yoga",
        "status": "Ayurvedic Rasayana Synergy",
        "classical_source": "Nishamalaki Shloka Extension (Ashtanga Hridaya Chikitsa 12)",
        "tkdl_status": "Novel Synergistic Extension of Classical Pair",
        "patentability_risk_pct": 48,
        "synergy_score_pct": 78,
        "therapeutic_category": "Glycemic Balance & Antioxidant Protection",
        "statutory_rationale": "Combines classical Nishamalaki (Turmeric + Amla) with Guduchi for AMPK pathway activation. Patentable under Section 3(e) if insulin-sensitizing synergy is substantiated over the base Nishamalaki control.",
        "statutory_requirements": [
            "Section 3(e) Synergy Evidence over Standard Nishamalaki Control",
            "NBA Form III Biological Diversity Approval",
            "Rule 158B Proof of Safety and Efficacy"
        ],
        "suggested_queries": [
            "Patentability of modified Nishamalaki formulations",
            "Section 3(e) requirement for diabetes polyherbal mixtures"
        ]
    },
    {
        "keys": {"shatavari", "ashwagandha"},
        "title": "Balya Rasayana Vitality Synergy",
        "sanskrit_name": "Ashwagandha-Shatavari Rasayana (बल्य रसायन)",
        "status": "Ayurvedic Vitality Admixture",
        "classical_source": "Classical Balya-Vrishya Herb Pairings",
        "tkdl_status": "Classical Admixture — High Section 3(e) Burden",
        "patentability_risk_pct": 55,
        "synergy_score_pct": 70,
        "therapeutic_category": "Balya / Endocrine Support & Physical Stamina",
        "statutory_rationale": "Well-known traditional pairing for vitality and tissue replenishment. Patenting requires showing specific synergistic hormone-modulating receptor affinity rather than generalized vitality claims.",
        "statutory_requirements": [
            "Section 3(e) Comparative Assay Data",
            "Biological Diversity Act Section 6 Clearance",
            "Rule 158B Manufacturing License"
        ],
        "suggested_queries": [
            "Patent guidelines for Ashwagandha Shatavari combination"
        ]
    },
    {
        "keys": {"guggulu", "turmeric"},
        "title": "Anti-Inflammatory Lipid-Modulating Complex",
        "sanskrit_name": "Haridra-Guggulu Sandhivata Yoga",
        "status": "Synergistic Anti-Arthritic Candidate",
        "classical_source": "Sandhivata & Medoroga Management",
        "tkdl_status": "Novel Synergistic Formulation Candidate",
        "patentability_risk_pct": 38,
        "synergy_score_pct": 88,
        "therapeutic_category": "Joint Mobility, COX-2 Modulation & Lipid Health",
        "statutory_rationale": "Guggulsterones combined with curcuminoids exhibit synergistic inhibition of NF-kB and COX-2 pathways. Strong candidate to overcome Section 3(e) with comparative anti-inflammatory biomarker data.",
        "statutory_requirements": [
            "Section 3(e) Synergy Assay Proof",
            "NBA Form III Clearance",
            "Ayush Rule 158B Safety & Standardization Dossier"
        ],
        "suggested_queries": [
            "Patent claims for Guggulu and Turmeric synergistic anti-arthritic agents"
        ]
    },
    {
        "keys": {"mulethi", "tulsi", "dry_ginger"},
        "title": "Kasa-Shwasa Bronchial Soothing Complex",
        "sanskrit_name": "Kasa-Shwasahara Polyherbal Yoga",
        "status": "Novel Respiratory Synergistic Formulation",
        "classical_source": "Pranavaha Srotas Soothing Principles",
        "tkdl_status": "Novel Candidate — Fulfills Section 3(e) if Synergistic",
        "patentability_risk_pct": 44,
        "synergy_score_pct": 80,
        "therapeutic_category": "Respiratory Health & Bronchial Soothing",
        "statutory_rationale": "Glycyrrhizin, eugenol, and gingerols provide triple-action bronchodilator, anti-tussive, and mucosal soothing synergy. Can be patented if non-additive anti-tussive kinetics are established.",
        "statutory_requirements": [
            "Section 3(e) Synergy Demonstration",
            "NBA Section 6 Clearance",
            "Rule 158B Proof of Effectiveness"
        ],
        "suggested_queries": [
            "Polyherbal cough syrup patent requirements under Indian Patent Act"
        ]
    }
]

class CraftService:
    def __init__(self):
        self.apothecary = BOTANICAL_APOTHECARY
        self.recipes = COMBINATIONS_RECIPES

    def get_apothecary_list(self) -> List[Dict[str, Any]]:
        """Returns all discoverable botanicals in the laboratory shelf."""
        result = []
        for key, herb in self.apothecary.items():
            result.append({
                "id": key,
                "canonical_name": herb["canonical_name"],
                "sanskrit_name": herb["sanskrit_name"],
                "botanical_name": herb["botanical_name"],
                "active_actives": herb["active_actives"],
                "classical_category": herb["classical_category"],
                "emoji": herb["emoji"]
            })
        return result

    def resolve_key(self, ingredient_name: str) -> Optional[str]:
        """Resolves raw ingredient string to canonical dictionary key."""
        clean = ingredient_name.lower().strip()
        for key, herb in self.apothecary.items():
            if clean == key:
                return key
            for synonym in herb["synonyms"]:
                if synonym in clean or clean in synonym:
                    return key
        return None

    def combine_ingredients(self, raw_ingredients: List[str]) -> Dict[str, Any]:
        """
        Combines 1 or more ingredients, scanning against classical TKDL recipes
        and evaluating statutory novelty and Section 3(e)/3(p) patentability.
        """
        if not raw_ingredients:
            return {
                "discovered": False,
                "title": "Empty Crucible",
                "ingredients": [],
                "status": "No Ingredients Selected",
                "tkdl_status": "Please add 2 or more ingredients to the crucible",
                "patentability_risk_pct": 0,
                "synergy_score_pct": 0,
                "therapeutic_category": "N/A",
                "statutory_rationale": "Add herbs from the apothecary tray into the cauldron to analyze their statutory combination profile.",
                "statutory_requirements": [],
                "suggested_queries": []
            }

        resolved_keys = set()
        canonical_names = []

        for item in raw_ingredients:
            k = self.resolve_key(item)
            if k:
                resolved_keys.add(k)
                canonical_names.append(self.apothecary[k]["canonical_name"])
            else:
                canonical_names.append(item.strip().title())

        if len(resolved_keys) == 1 and len(raw_ingredients) == 1:
            key = list(resolved_keys)[0]
            herb = self.apothecary[key]
            return {
                "discovered": True,
                "title": f"Single Botanical: {herb['canonical_name']}",
                "sanskrit_name": herb["sanskrit_name"],
                "ingredients": canonical_names,
                "status": "Single Natural Herb",
                "classical_source": f"Ayurvedic Pharmacopoeia of India (API) Monograph; Classical Category: {herb['classical_category']}",
                "tkdl_status": "Non-Patentable under Section 3(c) & Section 3(p)",
                "patentability_risk_pct": 98,
                "synergy_score_pct": 0,
                "therapeutic_category": herb["classical_category"],
                "statutory_rationale": (
                    f"{herb['canonical_name']} ({herb['botanical_name']}) in isolated form is naturally occurring, "
                    f"barred from patenting under Section 3(c) (discovery of naturally occurring substance) "
                    f"and Section 3(p) (traditional knowledge). Combine with complementary herbs to test "
                    f"formulation novelty and Section 3(e) unexpected synergy!"
                ),
                "statutory_requirements": [
                    "Patents Act Section 3(c) Natural Substance Bar",
                    "Patents Act Section 3(p) Traditional Knowledge Bar",
                    "Biological Diversity Act Section 6 Access Approval"
                ],
                "suggested_queries": [
                    f"Can {herb['canonical_name']} be patented in India?",
                    f"How to combine {herb['canonical_name']} into a patentable synergistic formulation"
                ]
            }

        for recipe in self.recipes:
            if recipe["keys"] == resolved_keys:
                return {
                    "discovered": True,
                    "title": recipe["title"],
                    "sanskrit_name": recipe["sanskrit_name"],
                    "ingredients": canonical_names,
                    "status": recipe["status"],
                    "classical_source": recipe["classical_source"],
                    "tkdl_status": recipe["tkdl_status"],
                    "patentability_risk_pct": recipe["patentability_risk_pct"],
                    "synergy_score_pct": recipe["synergy_score_pct"],
                    "therapeutic_category": recipe["therapeutic_category"],
                    "statutory_rationale": recipe["statutory_rationale"],
                    "statutory_requirements": recipe["statutory_requirements"],
                    "suggested_queries": recipe["suggested_queries"]
                }

        names_str = " + ".join(canonical_names)
        return {
            "discovered": True,
            "title": f"Novel Polyherbal Prototype: {names_str}",
            "sanskrit_name": "Anukta Yoga (Novel Herbal Formulation)",
            "ingredients": canonical_names,
            "status": "Experimental Formulation Candidate",
            "classical_source": "Novel Polyherbal Combination (Not directly indexed as classical single recipe in Charaka/Sushruta)",
            "tkdl_status": "Novel Candidate — Must Fulfill Section 3(e) Synergistic Burden",
            "patentability_risk_pct": 42,
            "synergy_score_pct": 74,
            "therapeutic_category": "Multi-Target Polyherbal Complex",
            "statutory_rationale": (
                f"The combination of {names_str} does not directly match single classical shlokas in the TKDL database. "
                "However, under Section 3(e) of the Indian Patents Act, the Indian Patent Office presumes all herbal "
                "combinations to be mere aggregations. To secure grant, the applicant must establish quantitative, non-obvious "
                "synergistic therapeutic enhancement through comparative pharmacological assays."
            ),
            "statutory_requirements": [
                "Patents Act Section 3(e) Proof of Synergistic Efficacy over Individual Components",
                "National Biodiversity Authority (NBA) Form III Prior Approval under Section 6",
                "Drugs & Cosmetics Rules, 1945 Rule 158B Proprietary ASU Licensing Dossier",
                "Standardization & Heavy Metal / Aflatoxin Testing Protocol"
            ],
            "suggested_queries": [
                f"Patentability of polyherbal mixture ({names_str}) under Section 3(e)",
                "NBA Form III approval checklist for polyherbal extraction",
                "Rule 158B safety testing requirements for new ASU drug"
            ]
        }

craft_service = CraftService()
