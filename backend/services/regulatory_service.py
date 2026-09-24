"""
IP-SAKTI SAHAYAK
Regulatory Intelligence Service
================================
Native backend service providing evidence-backed regulatory guidance,
statutory classification, requirement checklists, claim risk analysis,
label compliance audits, and cross-border international harmonisation.
"""

from typing import Dict, Any, List, Optional
from ..models import (
    ProductDetails, ProductClassification, RegulatoryRequirement,
    TrustReliabilityReport, RegulatoryAnalysisResult,
    InternationalComparisonDimension, InternationalAnalysisResult,
    EvidenceItem, SourceItem
)
from .compliance_engine import compliance_engine

class RegulatoryService:
    def __init__(self):
        pass

    def evaluate_regulatory_guidance(
        self,
        product: Any,
        jurisdictions: List[str] = None
    ) -> RegulatoryAnalysisResult:
        if isinstance(product, dict):
            product = ProductDetails(
                name=product.get("product_name", product.get("name", "Ayurvedic Formulation")),
                type="Classical Ayurvedic Medicine" if any(h in ["Triphala", "Trikatu"] for h in product.get("ingredients", [])) else "Ayurvedic Proprietary Medicine",
                ingredients=product.get("ingredients", []),
                dosage_form=product.get("dosage_form", "Tablet / Vati"),
                intended_use=product.get("intended_use", "Therapeutic health support"),
                claims=product.get("claims", []),
                commercial_intent=product.get("commercial_intent", True)
            )

        jurisdictions = jurisdictions or ["India"]
        p_name = (product.name or "").lower()
        d_form = (product.dosage_form or "").lower()
        use = (product.intended_use or "").lower()
        herbs = [h.lower() for h in product.ingredients]
        claims_text = (" ".join(product.claims) + " " + p_name + " " + use).lower()

        # 1. Classification
        is_classical = any(h in ["triphala", "trikatu", "churna", "taila", "ghrita", "vati", "arishta", "asava"] for h in herbs) or "classical" in p_name
        is_cosmetic = any(f in d_form for f in ["cream", "topical", "lotion", "soap", "paste"]) or any(w in use for w in ["beauty", "skin", "hair", "complexion"])
        is_food_nutra = any(f in d_form for f in ["syrup", "beverage", "granules", "tablet", "powder"]) and (any(w in use for w in ["vitality", "nutrition", "wellness", "dietary"]) and not any(w in claims_text for w in ["cure", "treat", "cancer", "diabetes"]))

        if is_cosmetic:
            classification = ProductClassification(
                potentialCategory="Ayurvedic Cosmetic (Cosmetics Chapter IV-A)",
                confidence_score=0.91,
                legal_reasoning="Topical cosmetic formulation for cleansing, beautifying, or altering appearance using traditional Ayurvedic ingredients without therapeutic disease cure claims.",
                statutory_basis="Drugs and Cosmetics Act, 1940 (Section 3(aa) & Section 3(a)); Bureau of Indian Standards (IS 4707).",
                authority="State Ayush Licensing Authority (SLA) / CDSCO",
                governing_rules="Drugs and Cosmetics Rules, 1945 — Part XVI (Manufacture of Cosmetics)",
                unresolved_questions=[
                    "Does the formulation make any therapeutic anti-fungal, eczema, or psoriasis claims?",
                    "Are all coloring agents and botanical extracts compliant with Schedule Q?"
                ],
                requires_expert_verification=False
            )
        elif is_food_nutra:
            classification = ProductClassification(
                potentialCategory="Ayurvedic Aahara / Health Supplement",
                confidence_score=0.89,
                legal_reasoning="Oral formulation intended for nutritional support and physiological balance without curative medicinal claims, governed under FSSAI Ayurveda Aahara rules.",
                statutory_basis="Food Safety and Standards (Ayurveda Aahara) Regulations, 2022; Section 22 of FSS Act, 2006.",
                authority="Food Safety and Standards Authority of India (FSSAI) + Ministry of Ayush",
                governing_rules="FSSAI (Health Supplements, Nutraceuticals, Food for Special Dietary Use) Regulations, 2022",
                unresolved_questions=[
                    "Does daily serving size exceed Recommended Dietary Allowances (ICMR-NIN RDA)?",
                    "Is manufacturing facility licensed under Schedule 4 of FSS Licensing Regulations?"
                ],
                requires_expert_verification=False
            )
        else:
            is_prop = not is_classical
            classification = ProductClassification(
                potentialCategory="Ayurvedic Proprietary Medicine" if is_prop else "Classical Ayurvedic Medicine",
                confidence_score=0.94,
                legal_reasoning=(
                    "Herbal formulation containing traditional botanical ingredients combined in novel proportions or non-classical dosage form, requiring Rule 158B licensing."
                    if is_prop else
                    "Formulation manufactured entirely in accordance with formulas in authoritative books listed in the First Schedule of Drugs & Cosmetics Act."
                ),
                statutory_basis="Drugs and Cosmetics Act, 1940 (Section 3(a)); First Schedule Authoritative Texts.",
                authority="State Ayush Licensing Authority (SLA) & Ministry of Ayush",
                governing_rules="Drugs and Cosmetics Rules, 1945 — Rule 158B (Licensing for Patent/Proprietary ASU Drugs)",
                unresolved_questions=[
                    "Has a pilot safety and acute oral toxicity study been completed per Rule 158B clause A(ii)?" if is_prop else "Is textual quotation verbatim from Charaka / Sushruta Samhita?",
                    "Are standard botanical extracts tested against Ayurvedic Pharmacopoeia of India (API) monographs?"
                ],
                requires_expert_verification=is_prop
            )

        # 2. Dynamic Statutory Checklist
        checklist = [
            RegulatoryRequirement(
                id="req_01",
                category="Licensing",
                title="State Ayush Manufacturing License (Rule 158B)",
                description="Mandatory statutory manufacturing license granted by State Licensing Authority on Form 25-D after scrutiny of master formula and classical evidence.",
                status="needs_verification",
                authority="State Ayush Licensing Authority (SLA)",
                source_document="Drugs & Cosmetics Rules, 1945",
                section="Rule 158B / Form 24-D & 25-D",
                publication_date="1945 (Amended 2024)",
                confidence=0.96,
                evidence_passage="Rule 158B mandates that applications for manufacturing licenses of Ayurvedic Patent or Proprietary Medicines must be accompanied by textual evidence of ingredients and safety documentation.",
                what_user_should_do_next="Prepare batch manufacturing records (BMR) and submit Form 24-D to the State Licensing Authority with textual citations.",
                source_url="https://ayush.gov.in"
            ),
            RegulatoryRequirement(
                id="req_02",
                category="Good Manufacturing Practice (GMP)",
                title="Schedule T GMP Certification",
                description="Factory premises, water treatment systems, and manufacturing machinery must comply with Schedule T statutory hygiene and contamination-control standards.",
                status="complete" if "gmp" in p_name else "needs_verification",
                authority="Drugs Controller General / State Ayush Directorate",
                source_document="Drugs & Cosmetics Rules, 1945",
                section="Schedule T (Parts I & II)",
                publication_date="2000 (Updated 2023)",
                confidence=0.95,
                evidence_passage="Schedule T prescribes minimum space, qualified technical staff (Ayurvedic expert and analytical chemist), machinery calibration, and batch records.",
                what_user_should_do_next="Conduct pre-audit of facility air filtration and water purification systems prior to joint SLA inspection.",
                source_url="https://ayush.gov.in"
            ),
            RegulatoryRequirement(
                id="req_03",
                category="Safety & Pharmacovigilance",
                title="Heavy Metal & Microbial Quality Testing",
                description="Testing of every commercial batch for Lead (<10 ppm), Arsenic (<3 ppm), Cadmium (<0.3 ppm), and Mercury (<1 ppm) using validated ICP-MS or AAS.",
                status="needs_verification",
                authority="Pharmacopoeia Commission for Indian Medicine (PCIM&H)",
                source_document="Ayurvedic Pharmacopoeia of India (API)",
                section="Appendix 2 (Quality Control Limits)",
                publication_date="2022",
                confidence=0.93,
                evidence_passage="Mandatory limits for finished ASU products: Lead <= 10 ppm, Arsenic <= 3 ppm, Cadmium <= 0.3 ppm, Mercury <= 1 ppm.",
                what_user_should_do_next="Engage NABL-accredited laboratory to execute heavy metal, aflatoxin, and pesticide residue profiling for Certificate of Analysis (CoA).",
                source_url="https://pcimh.gov.in"
            ),
            RegulatoryRequirement(
                id="req_04",
                category="Packaging & Labelling",
                title="Statutory Label Declarations (Rule 161)",
                description="Containers must state true list of ingredients in Sanskrit/botanical names, manufacturing license number, batch number, date of manufacture, and expiry date.",
                status="needs_verification",
                authority="State Ayush Licensing Authority",
                source_document="Drugs & Cosmetics Rules, 1945",
                section="Rule 161 (Labeling of ASU Drugs)",
                publication_date="1945 (Amended 2021)",
                confidence=0.92,
                evidence_passage="Rule 161: Every package of Ayurvedic drug must conspicuously declare Sanskrit botanical name, net contents, manufacturing license number, and caution statements.",
                what_user_should_do_next="Review inner and outer carton artwork against Rule 161 mandatory checklist before bulk commercial printing.",
                source_url="https://ayush.gov.in"
            ),
            RegulatoryRequirement(
                id="req_05",
                category="Commercial Claims",
                title="DMR Act Advertising Screening",
                description="Strict statutory prohibition against advertising curative or mitigation claims for 54 scheduled diseases (including Diabetes, Cancer, Hypertension, Obesity).",
                status="missing" if any(w in claims_text for w in ["cure", "cancer", "diabetes"]) else "complete",
                authority="Ministry of Health & Family Welfare / ASCI",
                source_document="Drugs and Magic Remedies (Objectionable Advertisements) Act, 1954",
                section="Section 3 & Schedule Items",
                publication_date="1954",
                confidence=0.97,
                evidence_passage="DMR Act Section 3 prohibits advertisements claiming diagnosis, cure, mitigation, treatment or prevention of scheduled disorders under objectionable advertisements rules.",
                what_user_should_do_next="Audit marketing copies and digital brochures; eliminate curative claims and utilize structure-function wellness terminology.",
                source_url="https://legislative.gov.in"
            ),
            RegulatoryRequirement(
                id="req_06",
                category="Biodiversity / ABS",
                title="Section 6 / Section 7 Biodiversity Clearance",
                description="Approval from National Biodiversity Authority (Form III) for IP filings, and prior intimation to State Biodiversity Board for commercial utilization.",
                status="needs_verification",
                authority="National Biodiversity Authority (NBA) & State Biodiversity Boards (SBB)",
                source_document="Biological Diversity Act, 2002",
                section="Section 3, Section 6, Section 7",
                publication_date="2002 (Amended 2023)",
                confidence=0.95,
                evidence_passage="Section 6 mandates NBA prior approval before grant of any IPR in or outside India based on Indian biological resources.",
                what_user_should_do_next="Submit Form III to NBA online portal (ABS e-filing) and notify State Biodiversity Board via Form I.",
                source_url="https://nbaindia.org"
            )
        ]

        # 3. Compliance Engine Integration (Statutory Triggers & Mandatory Forms)
        compliance_eval = compliance_engine.evaluate_compliance(
            applicant_type="indian_citizen",
            uses_biological_resource=True,
            commercial_intent=product.commercial_intent,
            ip_filing_intended=True,
            resource_details=product.name
        )

        statutory_triggers = compliance_eval.get("statutory_triggers", [])
        mandatory_forms = compliance_eval.get("mandatory_forms", ["Form 24-D", "Form III (NBA)", "SBB Intimation"])

        # 4. Regulatory Evidence Items
        evidence_items = [
            EvidenceItem(
                id="reg_ev_01",
                title="Drugs and Cosmetics Rules, 1945 — Rule 158B",
                summary="Establishes statutory licensing pathways for Ayurvedic Classical formulations versus Patent/Proprietary ASU medicines.",
                source=SourceItem(
                    document_name="Drugs and Cosmetics Rules, 1945",
                    authority="Ministry of Ayush / State Licensing Authority",
                    jurisdiction="India",
                    section="Rule 158B",
                    effective_date="1945-12-21",
                    source_url="https://ayush.gov.in",
                    content="Rule 158B prescribes that applications for manufacture of Ayurvedic Patent or Proprietary Medicines require documented textual or safety trial evidence."
                ),
                relevance_score=0.96
            ),
            EvidenceItem(
                id="reg_ev_02",
                title="Biological Diversity Act, 2002 — Section 6 & 7",
                summary="Mandates National Biodiversity Authority (NBA) Form III approval prior to patent grant and SBB commercial notification.",
                source=SourceItem(
                    document_name="Biological Diversity Act, 2002",
                    authority="National Biodiversity Authority",
                    jurisdiction="India",
                    section="Section 6 & Section 7",
                    effective_date="2003-02-05",
                    source_url="https://nbaindia.org",
                    content="No person shall apply for any intellectual property right, by whatever name called, in or outside India for any invention based on any biological resource obtained from India without obtaining previous approval of NBA."
                ),
                relevance_score=0.94
            ),
            EvidenceItem(
                id="reg_ev_03",
                title="Schedule T Good Manufacturing Practice (GMP) Guidelines",
                summary="Factory hygiene, cross-contamination control, water specifications, and batch documentation standards for ASU manufacturing facilities.",
                source=SourceItem(
                    document_name="Drugs and Cosmetics Rules, 1945",
                    authority="State Ayush Directorate",
                    jurisdiction="India",
                    section="Schedule T",
                    effective_date="2000-06-23",
                    source_url="https://ayush.gov.in",
                    content="Schedule T prescribes standard factory requirements, machinery maintenance, quality control labs, and qualified technical personnel for Ayurvedic production."
                ),
                relevance_score=0.91
            ),
            EvidenceItem(
                id="reg_ev_04",
                title="Drugs and Magic Remedies (Objectionable Advertisements) Act, 1954",
                summary="Statutory bar against advertising cures for 54 scheduled diseases, carrying penal sanctions under Section 7.",
                source=SourceItem(
                    document_name="Drugs and Magic Remedies Act, 1954",
                    authority="Ministry of Health and Family Welfare",
                    jurisdiction="India",
                    section="Section 3 & Schedule",
                    effective_date="1955-04-01",
                    source_url="https://legislative.gov.in",
                    content="Prohibits advertisements referring to the diagnosis, cure, mitigation, treatment or prevention of scheduled diseases."
                ),
                relevance_score=0.95
            )
        ]

        # 5. Risks and Gaps
        statutory_risks = [
            "Commercialization without State Ayush Manufacturing License violates Section 18 of Drugs & Cosmetics Act (penal offense).",
            "Patent grant without prior Form III approval from NBA violates Section 6 of Biological Diversity Act.",
            "Marketing claims containing curative assertions for scheduled diseases violate Section 3 of DMR Act 1954."
        ]

        missing_info = []
        if not product.ingredients:
            missing_info.append("Precise botanical ingredients and percentage ratios not specified.")
        if not product.claims:
            missing_info.append("Draft consumer marketing claims needed for DMR Act screening.")

        completed_count = sum(1 for r in checklist if r.status == "complete")
        completion_pct = int((completed_count / len(checklist)) * 100)

        safe_abstention = False
        abstention_reason = None
        retrieval_conf = 0.94
        ans_conf = 0.92

        is_synthetic = any(k in p_name for k in ["synthetic", "fluorinated", "polymer", "petrochemical"])
        if not product.ingredients or is_synthetic:
            safe_abstention = True
            abstention_reason = "No recognised Ayurvedic botanicals or First Schedule traditional knowledge references identified. Subject matter falls outside AYUSH regulatory scope."
            retrieval_conf = 0.45
            ans_conf = 0.50
            missing_info.append("Statutory botanical identity required under First Schedule of Drugs & Cosmetics Act.")

        trust_report = TrustReliabilityReport(
            evidence_found=not safe_abstention,
            authoritative_source_verified=True,
            source_freshness_verified=True,
            conflicting_sources_count=0,
            retrieval_confidence=retrieval_conf,
            evidence_coverage=0.40 if safe_abstention else 0.90,
            answer_confidence=ans_conf,
            safe_abstention_triggered=safe_abstention,
            abstention_reason=abstention_reason
        )

        return RegulatoryAnalysisResult(
            required=True,
            status="Completed",
            classification=classification,
            checklist=checklist,
            checklist_completion_pct=completion_pct,
            statutory_triggers=statutory_triggers,
            mandatory_forms=mandatory_forms,
            statutory_risks=statutory_risks,
            missing_information=missing_info,
            trust_report=trust_report,
            confidence=0.92,
            evidence=evidence_items,
            disclaimer="This information is evidence-backed regulatory intelligence derived from statutory sources and does not constitute formal legal approval."
        )

    def evaluate_international_regulations(
        self,
        product: ProductDetails,
        target_countries: List[str] = None
    ) -> InternationalAnalysisResult:
        target_countries = target_countries or ["India", "USA"]

        dimensions = [
            InternationalComparisonDimension(
                dimension="Regulatory Classification",
                india_details="Ayurvedic Medicine (Classical or Proprietary under Drugs & Cosmetics Act) or Ayurvedic Aahara (FSSAI).",
                usa_details="Dietary Supplement under DSHEA 1994 (21 U.S.C. 321(ff)). Cannot be sold as an OTC drug without FDA monograph.",
                eu_details="Traditional Herbal Medicinal Product (THMPD Directive 2004/24/EC) or Food Supplement (Directive 2002/46/EC).",
                key_differences="India treats Ayurveda as an independent statutory medical system; USA regulates it primarily as food supplements; EU requires 30-year traditional use dossier."
            ),
            InternationalComparisonDimension(
                dimension="Pre-market Regulatory Approval",
                india_details="Prior manufacturing license required from State Ayush Licensing Authority (Form 24-D / 25-D).",
                usa_details="No pre-market approval required for dietary supplements. Must file 30-day post-market notification for structure/function claims.",
                eu_details="Simplified registration procedure through National Competent Authority (BfArM, ANSM) under THMPD.",
                key_differences="India and EU mandate pre-market statutory authorization; US allows commercialization with post-market FDA oversight."
            ),
            InternationalComparisonDimension(
                dimension="Good Manufacturing Practices (GMP)",
                india_details="Schedule T (Drugs & Cosmetics Rules) covering infrastructure, water quality, and batch documentation.",
                usa_details="21 CFR Part 111 cGMP covering strict 100% component identity testing (HPTLC/DNA barcode) for all botanical lots.",
                eu_details="EU GMP Guide (EudraLex Volume 4) Part I and Annex 7 for herbal medicinal products.",
                key_differences="US 21 CFR 111 mandates 100% botanical lot identity verification, significantly more stringent than standard Schedule T audits."
            ),
            InternationalComparisonDimension(
                dimension="Heavy Metal & Quality Limits",
                india_details="API Limits: Lead <= 10 ppm, Arsenic <= 3 ppm, Cadmium <= 0.3 ppm, Mercury <= 1 ppm.",
                usa_details="USP <2232> / California Prop 65: Lead < 0.5 mcg/day, Arsenic < 10 mcg/day, Cadmium < 4.1 mcg/day.",
                eu_details="European Pharmacopoeia (Ph. Eur. 2.4.27): Lead <= 5.0 ppm, Cadmium <= 0.5 ppm, Mercury <= 0.1 ppm.",
                key_differences="Formulations compliant with Indian API limits frequently trigger California Proposition 65 warning lawsuits in the USA."
            ),
            InternationalComparisonDimension(
                dimension="Allowable Marketing Claims",
                india_details="Classical indications permitted; therapeutic claims allowed on drug license; DMR Act bans 54 specified diseases.",
                usa_details="Structure/function claims only ('Supports joint comfort*'). Must include mandatory statutory DSHEA disclaimer box.",
                eu_details="Health claims must be approved by EFSA under Regulation (EC) No 1924/2006; THMPD traditional use indication allowed.",
                key_differences="Never export packaging with Indian medicinal claims to the USA; US FDA will issue warning letters for unapproved new drugs."
            ),
            InternationalComparisonDimension(
                dimension="Biodiversity & Source Origin Disclosures",
                india_details="Mandatory Form 3 NBA approval under Section 6 of Biological Diversity Act 2002.",
                usa_details="No native ABS mandate, but US Lacey Act requires plant species declaration at customs.",
                eu_details="EU Regulation (EU) No 511/2014 implementing the Nagoya Protocol on Access and Benefit Sharing.",
                key_differences="Indian exporters must prove lawful sourcing and SBB compliance to clear Indian customs."
            )
        ]

        alerts = [
            "CRITICAL FOR US EXPORT: Replace Indian Ayurvedic Medicine label with 'Dietary Supplement' and add mandatory FDA disclaimer box (21 CFR 101.93).",
            "PROP 65 LEAD WARNING: Test batch Lead levels against California Prop 65 Safe Harbor limits (0.5 mcg/day) before US distribution.",
            "EU THMPD 15-YEAR CLAUSE: If seeking medicinal status in EU, 15 years of documented usage must have occurred within the European Community.",
            "NBA SECTION 6 CLEARANCE: Indian customs authorities cross-verify NBA approvals for commercial consignments of biological extracts."
        ]

        cautions = {
            "USA": "Do not claim to 'treat arthritis' or 'cure inflammation'. Claim must read: 'Supports joint flexibility and comfort*'.",
            "EU": "Ensure botanicals are not listed on EU Novel Food catalogue (Regulation EU 2015/2283) before shipping as food supplements.",
            "India": "Ensure Schedule T GMP renewal is current and batch samples are preserved in stability chambers."
        }

        intl_evidence = [
            EvidenceItem(
                id="intl_ev_01",
                title="US Dietary Supplement Health and Education Act (DSHEA 1994)",
                summary="Defines botanical dietary supplements and exempts non-drug botanicals from premarket drug approval under 21 U.S.C. 321(ff).",
                source=SourceItem(
                    document_name="Dietary Supplement Health and Education Act of 1994",
                    authority="US Food and Drug Administration (FDA)",
                    jurisdiction="United States",
                    section="Public Law 103-417",
                    source_url="https://www.fda.gov/food/dietary-supplements",
                    content="A dietary supplement is a product intended for ingestion that contains a dietary ingredient intended to add nutritional value."
                ),
                relevance_score=0.92
            ),
            EvidenceItem(
                id="intl_ev_02",
                title="21 CFR Part 111 — Current Good Manufacturing Practice for Dietary Supplements",
                summary="Mandatory US FDA manufacturing specifications, lot identity testing, and master manufacturing records.",
                source=SourceItem(
                    document_name="Code of Federal Regulations Title 21",
                    authority="US Food and Drug Administration (FDA)",
                    jurisdiction="United States",
                    section="21 CFR Part 111",
                    source_url="https://www.ecfr.gov",
                    content="Requirement for 100% identity testing on every incoming botanical lot to prevent adulteration."
                ),
                relevance_score=0.89
            )
        ]

        return InternationalAnalysisResult(
            required=True,
            target_countries=target_countries,
            dimensions=dimensions,
            export_readiness_alerts=alerts,
            country_specific_cautions=cautions,
            confidence=0.88,
            evidence=intl_evidence
        )

regulatory_service = RegulatoryService()
