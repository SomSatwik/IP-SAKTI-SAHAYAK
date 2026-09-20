"""
IP-SAKTI SAHAYAK
Biodiversity & ABS Compliance Engine
====================================
Rule-based evaluation for commercialization and IP filing on biological resources
under the Biological Diversity Act, 2002 and Access & Benefit Sharing (ABS) regulations.
"""

from typing import Dict, Any, List
from database.db import get_db_session
from database.models import ComplianceCheck


class ComplianceEngine:
    """Evaluates regulatory obligations under the Biological Diversity Act & AYUSH rules."""

    def evaluate_compliance(
        self,
        applicant_type: str,  # 'indian_citizen', 'nri', 'foreign_entity', 'indian_company_foreign_share'
        uses_biological_resource: bool,
        commercial_intent: bool,
        ip_filing_intended: bool,
        resource_details: str = ""
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
            mandatory_forms.append("Form I")

        # 2. Commercial Utilization by Indian Entities (Section 7)
        if uses_biological_resource and not is_foreign_element and commercial_intent:
            triggers.append({
                "statute": "Section 7, Biological Diversity Act, 2002",
                "authority": "State Biodiversity Board (SBB)",
                "obligation": "Prior intimation to the concerned State Biodiversity Board before obtaining biological resources for commercial utilization.",
                "form_required": "Form I (State Biodiversity Board rules)"
            })
            mandatory_forms.append("SBB Intimation")

        # 3. Intellectual Property Rights Filing (Section 6)
        if uses_biological_resource and ip_filing_intended:
            triggers.append({
                "statute": "Section 6, Biological Diversity Act, 2002",
                "authority": "National Biodiversity Authority (NBA)",
                "obligation": "Mandatory prior approval from the NBA before filing an application for any intellectual property right inside or outside India based on biological resources from India.",
                "timing": "Must be obtained before patent grant/sealing by the Patent Office.",
                "form_required": "Form III (Application for seeking prior approval for applying for Intellectual Property Right)"
            })
            mandatory_forms.append("Form III")

        # 4. Traditional Knowledge & Drug Licensing (AYUSH)
        if commercial_intent:
            triggers.append({
                "statute": "Drugs & Cosmetics Act, 1940 (Rule 158-B)",
                "authority": "State Licensing Authority (AYUSH)",
                "obligation": "Manufacturing license required under Ayurvedic, Siddha, or Unani categories with proof of classical textual reference or safety documentation.",
                "form_required": "Form 24-D / Form 25-D"
            })
            mandatory_forms.append("AYUSH License")

        # Generate summary guidance text
        guidance = [
            f"Evaluation for Applicant Type: {applicant_type.replace('_', ' ').title()}",
            f"Biological Resource Involved: {'Yes' if uses_biological_resource else 'No'}",
            f"Commercial Intent: {'Yes' if commercial_intent else 'No'}",
            f"IP Filing Intended: {'Yes' if ip_filing_intended else 'No'}",
            f"Mandatory Filings Identified: {', '.join(mandatory_forms) if mandatory_forms else 'None'}"
        ]

        # Log check to DB
        try:
            with get_db_session() as session:
                rec = ComplianceCheck(
                    project_title=resource_details[:200] or "General Formulation Assessment",
                    applicant_type=applicant_type,
                    uses_biological_resource=uses_biological_resource,
                    commercial_intent=commercial_intent,
                    ip_protection_intended=ip_filing_intended,
                    required_form=", ".join(mandatory_forms),
                    recommendation="; ".join(guidance)
                )
                session.add(rec)
        except Exception:
            pass

        return {
            "success": True,
            "status": "Preliminary compliance guidance",
            "applicant_type": applicant_type,
            "mandatory_forms": list(set(mandatory_forms)),
            "statutory_triggers": triggers,
            "compliance_summary": guidance,
            "disclaimer": "This guidance is preliminary and informational only. Form III submissions must be evaluated by the National Biodiversity Authority."
        }


# Global singleton instance
compliance_engine = ComplianceEngine()
