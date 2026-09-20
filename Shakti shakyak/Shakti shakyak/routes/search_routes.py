"""
IP-SAKTI SAHAYAK
Search, Prior-Art & Compliance Routes
=====================================
Dedicated endpoints for prior-art discovery, botanical patentability checks,
and Biological Diversity Act / ABS compliance guidance.
"""

from flask import Blueprint, request, jsonify, render_template
from rag.hybrid_search import hybrid_searcher
from services.prior_art_search import prior_art_searcher
from services.compliance_engine import compliance_engine

search_bp = Blueprint("search_bp", __name__)


@search_bp.route("/prior-art", methods=["GET"])
def prior_art_page():
    """Render the prior-art discovery workspace."""
    return render_template("prior_art.html")


@search_bp.route("/compliance", methods=["GET"])
def compliance_page():
    """Render the biodiversity and ABS compliance navigator."""
    return render_template("compliance.html")


@search_bp.route("/api/search", methods=["POST"])
def api_search():
    """Execute direct hybrid search across indexed legal corpus."""
    data = request.get_json(silent=True) or {}
    query = data.get("query", "").strip()
    top_k = int(data.get("top_k", 10))
    jurisdiction = data.get("jurisdiction")
    domain = data.get("domain")

    if not query:
        return jsonify({"success": False, "error": "Query parameter is required."}), 400

    results = hybrid_searcher.search(
        query=query,
        top_k=top_k,
        jurisdiction_filter=jurisdiction,
        domain_filter=domain
    )
    return jsonify({"success": True, "count": len(results), "results": results})


@search_bp.route("/api/prior-art", methods=["POST"])
def api_prior_art():
    """Analyze botanical/Ayurvedic formulation for prior-art overlaps."""
    data = request.get_json(silent=True) or {}
    description = data.get("description", "").strip()

    if not description:
        return jsonify({"success": False, "error": "Formulation description is required."}), 400

    analysis = prior_art_searcher.analyze_formulation(description)
    return jsonify(analysis)


@search_bp.route("/api/compliance", methods=["POST"])
def api_compliance():
    """Run rule-based compliance check for biological resource usage and patenting."""
    data = request.get_json(silent=True) or {}

    applicant_type = data.get("applicant_type", "indian_citizen")
    uses_bio = bool(data.get("uses_biological_resource", True))
    commercial = bool(data.get("commercial_intent", True))
    ip_filing = bool(data.get("ip_filing_intended", True))
    details = data.get("resource_details", "")

    result = compliance_engine.evaluate_compliance(
        applicant_type=applicant_type,
        uses_biological_resource=uses_bio,
        commercial_intent=commercial,
        ip_filing_intended=ip_filing,
        resource_details=details
    )
    return jsonify(result)
