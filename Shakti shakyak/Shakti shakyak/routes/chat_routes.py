"""
IP-SAKTI SAHAYAK
Chat & Inquiry API Routes
=========================
Handles conversational legal queries, research history, and report generation.
"""

from flask import Blueprint, request, jsonify, render_template, Response
from rag.rag_pipeline import rag_pipeline
from services.report_generator import report_generator
from database.db import get_db_session
from database.models import QueryLog

chat_bp = Blueprint("chat_bp", __name__)


@chat_bp.route("/chat", methods=["GET"])
def chat_page():
    """Render the 3-panel chat and research workspace."""
    return render_template("chat.html")


@chat_bp.route("/api/chat", methods=["POST"])
def api_chat():
    """Execute end-to-end RAG inquiry and return grounded answer with sources."""
    data = request.get_json(silent=True) or {}
    query = data.get("query", "").strip()

    if not query:
        return jsonify({
            "success": False,
            "error": "Query parameter cannot be empty."
        }), 400

    conversation_id = data.get("conversation_id")
    jurisdiction = data.get("jurisdiction")
    language = data.get("language")

    result = rag_pipeline.process_query(
        query=query,
        conversation_id=conversation_id,
        preferred_jurisdiction=jurisdiction,
        preferred_language=language
    )

    return jsonify(result)


@chat_bp.route("/api/history", methods=["GET"])
def api_history():
    """Fetch recent queries for session history."""
    try:
        with get_db_session() as session:
            logs = session.query(QueryLog).order_by(QueryLog.id.desc()).limit(15).all()
            history = [
                {
                    "id": l.id,
                    "query": l.query_text,
                    "domain": l.detected_domain,
                    "jurisdiction": l.detected_jurisdiction,
                    "confidence": l.confidence_score,
                    "level": l.confidence_level,
                    "timestamp": l.created_at.strftime("%Y-%m-%d %H:%M")
                }
                for l in logs
            ]
        return jsonify({"success": True, "history": history})
    except Exception as e:
        return jsonify({"success": False, "error": str(e)}), 500


@chat_bp.route("/api/reports", methods=["POST"])
def api_reports():
    """Generate printable HTML research report."""
    data = request.get_json(silent=True) or {}
    html = report_generator.generate_html_report(data)
    return Response(html, mimetype="text/html")
