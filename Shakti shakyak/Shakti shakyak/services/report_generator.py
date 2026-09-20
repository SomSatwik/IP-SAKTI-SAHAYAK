"""
IP-SAKTI SAHAYAK
Legal Research Report Generator
===============================
Generates structured, print-ready HTML/PDF audit summaries of RAG inquiries,
including confidence telemetry, verified citations, and statutory disclaimers.
"""

from datetime import datetime
from typing import Dict, Any


class ReportGenerator:
    """Creates formatted audit reports for patent and regulatory compliance research."""

    def generate_html_report(self, query_data: Dict[str, Any]) -> str:
        """Render a clean, printable HTML document for research verification."""
        timestamp = datetime.utcnow().strftime("%Y-%m-%d %H:%M:%S UTC")
        query = query_data.get("query", "N/A")
        answer = query_data.get("answer", "N/A")
        confidence = query_data.get("confidence", 0.0)
        confidence_pct = int(confidence * 100)
        level = query_data.get("confidence_level", "MEDIUM")
        domain = query_data.get("domain", "General IP")
        jurisdiction = query_data.get("jurisdiction", "India")
        sources = query_data.get("sources", [])

        # Format sources HTML
        sources_html = ""
        for s in sources:
            sources_html += f"""
            <div style="border: 1px solid #e2e8f0; border-radius: 8px; padding: 12px; margin-bottom: 12px; background: #f8fafc;">
                <div style="font-weight: bold; color: #1e293b;">[Source {s.get('citation_index', '')}] {s.get('title', '')}</div>
                <div style="font-size: 13px; color: #64748b; margin: 4px 0;">Authority: {s.get('authority', '')} | Jurisdiction: {s.get('jurisdiction', '')} | Section: {s.get('section', '')}</div>
                <div style="font-size: 13px; color: #334155; font-style: italic; margin-top: 6px;">"{s.get('text_snippet', '')}"</div>
                <div style="font-size: 12px; margin-top: 6px;"><a href="{s.get('source_url', '#')}" target="_blank" style="color: #2563eb;">View Official Source</a></div>
            </div>
            """

        html = f"""<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>IP-SAKTI SAHAYAK — Official Research Report</title>
    <style>
        body {{ font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; line-height: 1.6; color: #0f172a; max-width: 800px; margin: 0 auto; padding: 40px 20px; }}
        .header {{ border-bottom: 2px solid #0f172a; padding-bottom: 16px; margin-bottom: 24px; }}
        .badge {{ display: inline-block; padding: 4px 10px; border-radius: 9999px; font-size: 12px; font-weight: 600; background: #e0f2fe; color: #0369a1; margin-right: 8px; }}
        .confidence-box {{ display: flex; align-items: center; gap: 12px; background: #f0fdf4; border: 1px solid #bbf7d0; padding: 12px 16px; border-radius: 8px; margin: 20px 0; }}
        .disclaimer {{ background: #fffbeb; border-left: 4px solid #f59e0b; padding: 12px; font-size: 12px; color: #92400e; margin-top: 32px; }}
        h1, h2, h3 {{ color: #0f172a; }}
        @media print {{ body {{ padding: 0; }} .no-print {{ display: none; }} }}
    </style>
</head>
<body>
    <div class="header">
        <div style="display: flex; justify-content: space-between; align-items: center;">
            <h1 style="margin: 0; font-size: 24px;">IP-SAKTI SAHAYAK</h1>
            <button class="no-print" onclick="window.print()" style="padding: 8px 16px; background: #0f172a; color: white; border: none; border-radius: 6px; cursor: pointer;">Print / Save as PDF</button>
        </div>
        <p style="color: #64748b; margin: 6px 0 0 0; font-size: 14px;">SIH 2026 (PS26045) — AI-Powered Intellectual Property & Regulatory Research Dossier</p>
        <p style="font-size: 12px; color: #94a3b8; margin: 4px 0 0 0;">Generated: {timestamp}</p>
    </div>

    <div>
        <span class="badge">Domain: {domain}</span>
        <span class="badge">Jurisdiction: {jurisdiction}</span>
    </div>

    <div class="confidence-box">
        <div><strong>Confidence Rating:</strong> {confidence_pct}% ({level})</div>
        <div style="font-size: 13px; color: #166534;">Supported by authoritative statutory evidence.</div>
    </div>

    <section>
        <h3>Research Question</h3>
        <p style="font-size: 16px; font-weight: 500; background: #f1f5f9; padding: 12px; border-radius: 6px;">{query}</p>
    </section>

    <section>
        <h3>Grounded Legal Analysis</h3>
        <div style="white-space: pre-line; background: #ffffff; padding: 4px 0;">{answer}</div>
    </section>

    <section style="margin-top: 32px;">
        <h3>Verified Statutory Citations</h3>
        {sources_html if sources_html else "<p>No statutory citations referenced.</p>"}
    </section>

    <div class="disclaimer">
        <strong>LEGAL DISCLAIMER:</strong> This dossier was compiled by IP-SAKTI Sahayak for research and informational purposes only. It does not constitute formal legal counsel or create an attorney-client privilege.
    </div>
</body>
</html>"""
        return html


# Global singleton instance
report_generator = ReportGenerator()
