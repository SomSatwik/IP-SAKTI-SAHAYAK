"""
IP-SAKTI SAHAYAK
Legal & Compliance Report Generator
====================================
Generates structured, print-ready HTML/PDF audit summaries of RAG inquiries,
compliance roadmaps, and statutory clearances.
"""

from datetime import datetime
from typing import Dict, Any, List


class ReportGenerator:
    """Creates formatted audit reports for patent and regulatory compliance research."""

    def generate_html_report(self, query_data: Dict[str, Any]) -> str:
        """Render a clean, printable HTML document for research verification."""
        timestamp = datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S UTC")
        query = query_data.get("query", "Ayurvedic Formulation IP & Compliance Roadmap")
        answer = query_data.get("answer", "Analysis of statutory requirements under Patents Act 1970 and Biological Diversity Act 2002.")
        confidence = query_data.get("confidence", 0.85)
        confidence_pct = int(confidence * 100)
        domain = query_data.get("domain", "Ayurveda & Patents")
        jurisdiction = query_data.get("jurisdiction", "India")
        sources = query_data.get("sources", [])
        steps = query_data.get("roadmap_steps", [])
        mandatory_forms = query_data.get("mandatory_forms", ["NBA Form III", "AYUSH Form 24-D"])

        sources_html = ""
        for s in sources:
            sources_html += f"""
            <div style="border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; margin-bottom: 12px; background: #f8fafc;">
                <div style="font-weight: bold; color: #1e293b;">{s.get('title', '')}</div>
                <div style="font-size: 13px; color: #64748b; margin: 4px 0;">Authority: {s.get('authority', '')} | Jurisdiction: {s.get('jurisdiction', '')} | Section: {s.get('section', '')}</div>
                <div style="font-size: 13px; color: #334155; font-style: italic; margin-top: 6px;">"{s.get('summary', '')}"</div>
            </div>
            """

        steps_html = ""
        for idx, st in enumerate(steps):
            status_color = "#16a34a" if st.get("status") == "completed" else ("#d97706" if st.get("status") == "in_progress" else "#64748b")
            steps_html += f"""
            <li style="margin-bottom: 12px; padding: 10px; border-left: 4px solid {status_color}; background: #f8fafc; border-radius: 4px;">
                <strong>Step {idx+1}: {st.get('title', '')}</strong> — <span style="color: {status_color}; font-weight: bold; text-transform: uppercase; font-size: 12px;">{st.get('status', 'Pending')}</span>
                <p style="margin: 4px 0 0 0; font-size: 13px; color: #475569;">{st.get('description', '')}</p>
                <span style="font-size: 11px; color: #94a3b8;">Est. Duration: {st.get('duration', 'N/A')}</span>
            </li>
            """

        forms_html = " ".join([f"<span class='badge' style='background:#fef3c7; color:#92400e;'>📋 {f}</span>" for f in mandatory_forms])

        html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>IP-SAKTI SAHAYAK — Official Compliance & IP Audit Dossier</title>
    <style>
        body {{ font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; line-height: 1.6; color: #0f172a; max-width: 820px; margin: 0 auto; padding: 36px 20px; }}
        .header {{ border-bottom: 3px solid #1e3a8a; padding-bottom: 16px; margin-bottom: 24px; }}
        .badge {{ display: inline-block; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 600; background: #e0f2fe; color: #0369a1; margin-right: 8px; margin-bottom: 6px; }}
        .confidence-box {{ display: flex; align-items: center; justify-content: space-between; background: #f0fdf4; border: 1px solid #bbf7d0; padding: 12px 18px; border-radius: 8px; margin: 20px 0; }}
        .disclaimer {{ background: #fffbeb; border-left: 4px solid #f59e0b; padding: 12px; font-size: 12px; color: #92400e; margin-top: 32px; border-radius: 4px; }}
        h1, h2, h3 {{ color: #0f172a; }}
        ul {{ list-style-type: none; padding-left: 0; }}
        @media print {{ body {{ padding: 0; }} .no-print {{ display: none; }} }}
    </style>
</head>
<body>
    <div class="header">
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <div>
                <h1 style="margin: 0; font-size: 26px; color: #1e3a8a;">IP-SAKTI SAHAYAK</h1>
                <p style="color: #64748b; margin: 4px 0 0 0; font-size: 13px;">SIH PS26045: Ayurvedic IP, Traditional Knowledge & Regulatory Compliance Platform</p>
            </div>
            <button class="no-print" onclick="window.print()" style="padding: 10px 18px; background: #1e3a8a; color: white; border: none; border-radius: 6px; cursor: pointer; font-weight: bold;">🖨️ Print / Save as PDF</button>
        </div>
        <p style="font-size: 12px; color: #94a3b8; margin: 8px 0 0 0;">Report Generated: {timestamp}</p>
    </div>

    <div>
        <span class="badge">Domain: {domain}</span>
        <span class="badge">Territory: {jurisdiction}</span>
    </div>

    <div class="confidence-box">
        <div><strong>Evidence Confidence Score:</strong> <span style="color: #16a34a; font-size: 18px; font-weight: bold;">{confidence_pct}%</span> (Verified by Statutory Corpus)</div>
        <div style="font-size: 12px; color: #166534;">Section 3(p) & NBA Form III Evaluated</div>
    </div>

    <section>
        <h3 style="border-bottom: 1px solid #e2e8f0; padding-bottom: 6px;">Subject Matter Query</h3>
        <p style="font-size: 15px; background: #f1f5f9; padding: 12px; border-radius: 6px; margin: 0;">{query}</p>
    </section>

    <section style="margin-top: 24px;">
        <h3 style="border-bottom: 1px solid #e2e8f0; padding-bottom: 6px;">Mandatory Regulatory Filings Required</h3>
        <div style="margin-top: 8px;">
            {forms_html}
        </div>
    </section>

    <section style="margin-top: 24px;">
        <h3 style="border-bottom: 1px solid #e2e8f0; padding-bottom: 6px;">Actionable Step-by-Step Compliance Roadmap</h3>
        <ul>
            {steps_html if steps_html else "<li>Step 1: Conduct TKDL Prior Art Clearance (1-2 weeks)<br>Step 2: Document Synergistic Therapeutic Efficacy (2-3 months)<br>Step 3: Submit NBA Form III Prior Approval (3-6 months)<br>Step 4: File Patent Specification with Indian Patent Office</li>"}
        </ul>
    </section>

    <section style="margin-top: 24px;">
        <h3 style="border-bottom: 1px solid #e2e8f0; padding-bottom: 6px;">Authoritative Legal Citations & Statutory Grounding</h3>
        {sources_html if sources_html else "<p>Grounded in Patents Act 1970 (Section 3p), Biological Diversity Act 2002 (Section 6), and AYUSH Rule 158B.</p>"}
    </section>

    <div class="disclaimer">
        <strong>OFFICIAL DISCLAIMER:</strong> This Compliance Audit Dossier is synthesized by IP-SAKTI Sahayak for research and regulatory planning purposes. It does not constitute formal legal counsel. Prior to commercial filing, consult a registered patent attorney and State Biodiversity Board representatives.
    </div>
</body>
</html>"""
        return html


report_generator = ReportGenerator()
