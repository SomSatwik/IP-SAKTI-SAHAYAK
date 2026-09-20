"""
IP-SAKTI SAHAYAK
Source Registry & Knowledge Graph Routes
========================================
Endpoints for exploring official source registries, regulatory updates,
and the interactive IP-Ayurveda knowledge graph.
"""

from flask import Blueprint, jsonify, render_template, abort
from config import Config
from services.regulation_monitor import regulation_monitor
from services.knowledge_graph import knowledge_graph

source_bp = Blueprint("source_bp", __name__)


@source_bp.route("/sources", methods=["GET"])
def sources_page():
    """Render the official authoritative source registry view."""
    return render_template("sources.html", sources=Config.OFFICIAL_SOURCES)


@source_bp.route("/knowledge-graph", methods=["GET"])
def knowledge_graph_page():
    """Render the interactive knowledge graph visualizer."""
    return render_template("knowledge_graph.html")


@source_bp.route("/api/sources", methods=["GET"])
def api_sources():
    """Return all registered official sources with authority tiers."""
    return jsonify({
        "success": True,
        "count": len(Config.OFFICIAL_SOURCES),
        "sources": Config.OFFICIAL_SOURCES
    })


@source_bp.route("/api/sources/<source_id>", methods=["GET"])
def api_source_detail(source_id: str):
    """Return metadata for a specific registered source."""
    source = next((s for s in Config.OFFICIAL_SOURCES if s["id"] == source_id), None)
    if not source:
        abort(404, description="Source ID not registered.")
    return jsonify({"success": True, "source": source})


@source_bp.route("/api/regulations", methods=["GET"])
def api_regulations():
    """Return recent regulatory notices and gazette updates."""
    updates = regulation_monitor.get_recent_updates(limit=10)
    return jsonify({"success": True, "updates": updates})


@source_bp.route("/api/knowledge-graph", methods=["GET"])
def api_knowledge_graph():
    """Return node and edge data for knowledge graph visualization."""
    data = knowledge_graph.get_graph_data()
    return jsonify({"success": True, "graph": data})
