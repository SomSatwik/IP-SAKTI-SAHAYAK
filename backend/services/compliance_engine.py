"""
IP-SAKTI SAHAYAK
Biodiversity & ABS Compliance Engine
====================================
Rule-based evaluation for commercialization and IP filing on biological resources
under the Biological Diversity Act, 2002 and Access & Benefit Sharing (ABS) regulations.
"""

from typing import Dict, Any, List


class ComplianceEngine:
    """Evaluates regulatory obligations under the Biological Diversity Act & AYUSH rules."""

    def evaluate_compliance(
        self,
        applicant_type: str = "indian_citizen",  # 'indian_citizen', 'nri', 'foreign_entity', 'indian_company_foreign_share'
        uses_biological_resource: bool = True,
        commercial_intent: bool = True,
        ip_filing_intended: bool = True,
        resource_details: str = "Ayurvedic Herbal Formulation"
    ) -> Dict[str, Any]:
        """
        Determine mandatory legal approvals (Section 3, 4, 6) and National Biodiversity Authority forms.
        """
        triggers: List[Dict[str, str]] = []
        mandatory_forms: List[str] = []
        is_foreign_element = applicant_type in ("nri", "foreign_entity", "indian_company_foreign_share")

        # 1. Access to Biological Resources (Section 3)
        if uses_biological_resource and is_foreign_element:
            triggers.append({
                "statute": "Section 3, Biological Diversity Act, 2002",
                "authority": "National Biodiversity Authority (NBA)",
                "obligation": "Mandatory prior approval before obtaining any biological resource occurring in India for research or commercial utilization.",
                "form_required": "Form I (Application for Access to Biological Resources)"
            })
            mandatory_forms.append("Form I (NBA)")

        # 2. Commercial Utilization by Indian Entities (Section 7)
        if uses_biological_resource and not is_foreign_element and commercial_intent:
            triggers.append({
                "statute": "Section 7, Biological Diversity Act, 2002",
                "authority": "State Biodiversity Board (SBB)",
                "obligation": "Prior intimation to the concerned State Biodiversity Board before obtaining biological resources for commercial utilization.",
                "form_required": "SBB Form I (State Biodiversity Board rules)"
            })
            mandatory_forms.append("SBB Intimation")

        # 3. Intellectual Property Rights Filing (Section 6)
        if uses_biological_resource and ip_filing_intended:
            triggers.append({
                "statute": "Section 6, Biological Diversity Act, 2002",
                "authority": "National Biodiversity Authority (NBA)",
                "obligation": "Mandatory prior approval from NBA before applying for any intellectual property right inside or outside India based on biological resources from India.",
                "timing": "Must be obtained before patent grant/sealing by the Patent Office.",
                "form_required": "Form III (Seeking prior approval for applying for Intellectual Property Right)"
            })
            mandatory_forms.append("Form III (NBA)")

        # 4. Traditional Knowledge & Drug Licensing (AYUSH)
        if commercial_intent:
            triggers.append({
                "statute": "Drugs & Cosmetics Act, 1940 (Rule 158-B)",
                "authority": "State Licensing Authority (AYUSH)",
                "obligation": "Manufacturing license required under Ayurvedic, Siddha, or Unani categories with proof of classical textual reference or safety documentation.",
                "form_required": "Form 24-D / Form 25-D"
            })
            mandatory_forms.append("AYUSH License (Form 24-D / 25-D)")

        guidance = [
            f"Applicant Classification: {applicant_type.replace('_', ' ').title()}",
            f"Biological Resource: {'Involved' if uses_biological_resource else 'None'}",
            f"Commercial Intent: {'Yes' if commercial_intent else 'Research Only'}",
            f"IP Filing Intended: {'Yes' if ip_filing_intended else 'No'}",
            f"Mandatory Statutory Forms: {', '.join(mandatory_forms) if mandatory_forms else 'None'}"
        ]

        return {
            "success": True,
            "status": "Official Compliance Clearance Assessment",
            "applicant_type": applicant_type,
            "resource_details": resource_details,
            "mandatory_forms": list(set(mandatory_forms)),
            "statutory_triggers": triggers,
            "compliance_summary": guidance,
            "disclaimer": "This compliance assessment is generated for regulatory readiness. Formal filings must be submitted to the National Biodiversity Authority and State AYUSH authorities."
        }


compliance_engine = ComplianceEngine()
