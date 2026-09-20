"""
IP-SAKTI SAHAYAK
Admin & System Health Routes
============================
Health checks, vector store metrics, and administrative re-indexing operations.
"""

from flask import Blueprint, jsonify, render_template
from config import Config
from rag.vector_store import vector_store
from rag.hybrid_search import hybrid_searcher
from ai.llm import groq_client
from database.db import engine

admin_bp = Blueprint("admin_bp", __name__)


@admin_bp.route("/admin", methods=["GET"])
def admin_page():
    """Render the administrative control dashboard."""
    return render_template("admin.html")


@admin_bp.route("/api/health", methods=["GET"])
def api_health():
    """System health diagnostics endpoint."""
    # Check Database
    db_ok = True
    try:
        from sqlalchemy import text
        with engine.connect() as conn:
            conn.execute(text("SELECT 1"))
    except Exception:
        db_ok = False

    # Check Vector Store
    vector_ok = vector_store.index is not None
    chunk_count = vector_store.count()

    # Check LLM
    llm_ok = groq_client.is_available()

    all_healthy = db_ok and vector_ok and llm_ok

    return jsonify({
        "status": "healthy" if all_healthy else "degraded",
        "llm": llm_ok,
        "llm_model": Config.GROQ_MODEL,
        "vector_store": vector_ok,
        "chunks_indexed": chunk_count,
        "database": db_ok,
        "embedding_model": Config.EMBEDDING_MODEL
    })


@admin_bp.route("/api/admin/stats", methods=["GET"])
def api_admin_stats():
    """Return administrative corpus and search telemetry."""
    return jsonify({
        "success": True,
        "indexed_chunks": vector_store.count(),
        "registered_sources": len(Config.OFFICIAL_SOURCES),
        "embedding_dimension": Config.EMBEDDING_DIMENSION,
        "faiss_enabled": bool(vector_store.index is not None)
    })
