"""
IP-SAKTI SAHAYAK
Dynamic Compliance Roadmap Service
===================================
Constructs a sequential, milestone-driven roadmap combining:
1. Prior Art Clearance & TKDL verification
2. Biodiversity & NBA Section 6 / Form III approvals
3. State Ayush Licensing Authority (Rule 158B) application
4. Schedule T Good Manufacturing Practices & QA testing
5. Patent Application Filing (Form 1, 2, 18A expedited examination)
6. Commercial Launch & Post-Market Surveillance (Rule 161, DMR Act)
"""

from typing import List, Dict, Any, Optional
from ..models import ComplianceRoadmapResponse, RoadmapStep, ProductDetails, RegulatoryAnalysisResult

class RoadmapService:
    def build_unified_roadmap(
        self,
        product: ProductDetails,
        regulatory_result: Optional[RegulatoryAnalysisResult],
        ip_required: bool = True,
        regulatory_required: bool = True,
        international_required: bool = False
    ) -> ComplianceRoadmapResponse:
        steps: List[RoadmapStep] = []

        step_idx = 1

        # Phase 1: Prior Art Clearance & Botanical Assessment (Always step 1 if IP required)
        if ip_required:
            steps.append(RoadmapStep(
                id=f"step_{step_idx}",
                title="Phase 1: TKDL & Global Prior Art Clearance",
                description="Search Traditional Knowledge Digital Library (TKDL) and patent registries for prior art disclosures regarding the formulation's botanical components.",
                status="completed",
                duration="1-2 Weeks",
                authority="CSIR-TKDL / Indian Patent Office",
                form_required="Prior Art Search Report",
                evidence_source="Patents Act 1970 — Section 3(p) & TKDL Guidelines"
            ))
            step_idx += 1

        # Phase 2: Biodiversity Approval (Section 6 Form III)
        if regulatory_required or ip_required:
            steps.append(RoadmapStep(
                id=f"step_{step_idx}",
                title="Phase 2: National Biodiversity Authority (NBA) Clearance",
                description="Submit Form III online to the National Biodiversity Authority for permission to apply for an Intellectual Property Right on Indian biological resources.",
                status="in_progress",
                duration="4-8 Weeks",
                authority="National Biodiversity Authority (NBA)",
                form_required="Form III (Section 6, BD Act)",
                evidence_source="Biological Diversity Act, 2002 — Section 6(1)"
            ))
            step_idx += 1

        # Phase 3: State Ayush Manufacturing License & Formulation Scrutiny
        if regulatory_required:
            cat_name = regulatory_result.classification.potentialCategory if regulatory_result else "Ayurvedic Proprietary Medicine"
            steps.append(RoadmapStep(
                id=f"step_{step_idx}",
                title=f"Phase 3: State Ayush Licensing Application ({cat_name})",
                description="Submit Form 24-D to State Licensing Authority with batch manufacturing records, classical textual evidence, and stability protocol.",
                status="pending",
                duration="6-12 Weeks",
                authority="State Ayush Licensing Authority (SLA)",
                form_required="Form 24-D / Form 25-D",
                evidence_source="Drugs & Cosmetics Rules, 1945 — Rule 158B"
            ))
            step_idx += 1

            # Phase 4: Schedule T GMP Certification & Analytical Testing
            steps.append(RoadmapStep(
                id=f"step_{step_idx}",
                title="Phase 4: Schedule T GMP Audit & Heavy Metal Profiling",
                description="Validate factory premises and execute heavy metal (Pb, As, Cd, Hg) and microbial contamination testing through NABL-accredited laboratory.",
                status="pending",
                duration="2-4 Weeks",
                authority="State Ayush Directorate / NABL Labs",
                form_required="Schedule T Compliance Certificate & CoA",
                evidence_source="Ayurvedic Pharmacopoeia of India (API) Appendix 2"
            ))
            step_idx += 1

        # Phase 5: Patent Filing & Expedited Examination
        if ip_required:
            steps.append(RoadmapStep(
                id=f"step_{step_idx}",
                title="Phase 5: Patent Application Filing & Form 18A Expedited Examination",
                description="File complete specification at Patent Office branch; file Form 18A for fast-track examination under DPIIT Startup or Female Applicant provisions.",
                status="pending",
                duration="3-6 Months",
                authority="Indian Patent Office (CGPDTM)",
                form_required="Form 1, Form 2, Form 18A (Expedited)",
                evidence_source="Patents Rules, 2003 — Rule 24C"
            ))
            step_idx += 1

        # Phase 6: International Export Preparation (if applicable)
        if international_required:
            steps.append(RoadmapStep(
                id=f"step_{step_idx}",
                title="Phase 6: US FDA DSHEA Re-labeling & Prop 65 Verification",
                description="Transition packaging to Dietary Supplement format with statutory FDA disclaimer box and verify batch Lead levels against California Prop 65 safe harbor limits (<0.5 mcg/day).",
                status="pending",
                duration="3-5 Weeks",
                authority="US FDA / Customs & Border Protection",
                form_required="DSHEA 30-Day Structure/Function Notification",
                evidence_source="21 CFR 101.93 & California Proposition 65"
            ))
            step_idx += 1

        return ComplianceRoadmapResponse(steps=steps)

roadmap_service = RoadmapService()
