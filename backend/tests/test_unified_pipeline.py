"""
IP-SAKTI SAHAYAK - Unified Pipeline & Regulatory Integration Tests
==================================================================
Tests covering the 10 required end-to-end scenarios:
1. IP-only query routing and response
2. Regulatory-only query routing and response
3. Combined IP + Regulatory query with cross-domain synthesis
4. India-only query
5. India + USA international comparison query
6. Document upload / label / claim audit under DMR Act 1954
7. Insufficient evidence / safe partial abstention
8. Conflicting evidence handling (Classical vs Patent novelty)
9. Regulatory failure resilience (IP succeeds gracefully)
10. IP failure resilience (Regulatory succeeds gracefully)
"""

import pytest
from unittest.mock import patch, MagicMock

from backend.models import (
    QueryRequest, ProductDetails, RegulatoryEvaluateRequest,
    InternationalCompareRequest, IntentRoutingResult
)
from backend.services.router_service import router_service
from backend.services.regulatory_service import regulatory_service
from backend.services.cross_domain_reasoner import cross_domain_reasoner
from backend.services.graph_service import graph_service
from backend.services.roadmap_service import roadmap_service
from backend.services.query_service import query_service

@pytest.fixture(autouse=True)
def disable_remote_api(monkeypatch):
    """Ensure integration tests execute fast and deterministically against the local domain engine."""
    monkeypatch.setattr(query_service, "_run_ip_analysis", lambda req, *args, **kwargs: query_service._process_with_domain_knowledge(req))


# ----------------------------------------------------------------------
# TEST 1: IP-Only Query
# ----------------------------------------------------------------------
def test_01_ip_only_query():
    query = "Is my novel extraction method of Ashwagandha patentable under Section 3(e)?"
    routing = router_service.route_intent(query)
    
    assert routing.ip_required is True
    assert routing.regulatory_required is False
    assert routing.primary_intent == "ip_only"
    
    req = QueryRequest(question=query)
    res = query_service.process_query(req)
    
    assert res.answer is not None
    assert len(res.answer) > 0
    assert "Section 3" in res.answer or "patent" in res.answer.lower()
    # Regulatory analysis should be skipped / not required
    assert res.regulatory_analysis is None or res.regulatory_analysis.required is False


# ----------------------------------------------------------------------
# TEST 2: Regulatory-Only Query
# ----------------------------------------------------------------------
def test_02_regulatory_only_query():
    query = "What are the labelling requirements, Schedule T GMP and Rule 158B licensing for an Ayurvedic churnam?"
    routing = router_service.route_intent(query)
    
    assert routing.regulatory_required is True
    assert routing.ip_required is False
    assert routing.primary_intent == "regulatory_only"
    
    req = QueryRequest(question=query)
    res = query_service.process_query(req)
    
    assert res.regulatory_analysis is not None
    assert res.regulatory_analysis.required is True
    assert res.regulatory_analysis.classification.potential_category is not None
    assert len(res.regulatory_analysis.checklist) >= 5
    assert any((req.section and "158B" in req.section) or "158B" in req.title for req in res.regulatory_analysis.checklist)


# ----------------------------------------------------------------------
# TEST 3: Combined IP + Regulatory Query & Cross-Domain Synthesis
# ----------------------------------------------------------------------
def test_03_combined_ip_and_regulatory_query():
    query = "I developed a novel tablet combining Ashwagandha and Brahmi. Can I patent it and what Ayush license do I need to sell it?"
    routing = router_service.route_intent(query)
    
    assert routing.ip_required is True
    assert routing.regulatory_required is True
    assert routing.primary_intent == "combined_ip_regulatory"
    
    req = QueryRequest(question=query)
    res = query_service.process_query(req)
    
    assert res.regulatory_analysis is not None
    assert res.cross_domain_synthesis is not None
    # Must articulate the classical vs proprietary tradeoff
    assert "Section 3(p)" in res.cross_domain_synthesis or "3(p)" in res.cross_domain_synthesis
    assert "Rule 158B" in res.cross_domain_synthesis or "158B" in res.cross_domain_synthesis
    assert res.component_confidence is not None
    assert res.component_confidence.ip_confidence is not None
    assert res.component_confidence.regulatory_confidence is not None


# ----------------------------------------------------------------------
# TEST 4: India-Only Query
# ----------------------------------------------------------------------
def test_04_india_only_query():
    query = "Manufacturing and patenting Ashwagandha syrup strictly in India."
    routing = router_service.route_intent(query)
    
    assert "India" in routing.detected_jurisdictions
    assert "USA" not in routing.detected_jurisdictions
    assert routing.international_required is False
    
    req = QueryRequest(question=query)
    res = query_service.process_query(req)
    
    assert res.international_analysis is None or res.international_analysis.required is False


# ----------------------------------------------------------------------
# TEST 5: India + USA Query (International Comparison)
# ----------------------------------------------------------------------
def test_05_india_and_usa_query():
    query = "Can I sell my Ayurvedic polyherbal formulation in India as a medicine and export to USA as a dietary supplement?"
    routing = router_service.route_intent(query)
    
    assert "India" in routing.detected_jurisdictions
    assert "USA" in routing.detected_jurisdictions
    assert routing.international_required is True
    
    req = QueryRequest(question=query)
    res = query_service.process_query(req)
    
    assert res.international_analysis is not None
    assert res.international_analysis.required is True
    assert len(res.international_analysis.dimensions) >= 4
    
    # Must feature DSHEA 1994, 21 CFR 111 cGMP, Prop 65
    dim_text = " ".join(d.usa_details + " " + d.key_differences for d in res.international_analysis.dimensions)
    assert "DSHEA" in dim_text
    assert "111" in dim_text or "cGMP" in dim_text or "Prop 65" in dim_text


# ----------------------------------------------------------------------
# TEST 6: Document Upload / Label / Claim Audit under DMR Act 1954
# ----------------------------------------------------------------------
def test_06_claim_audit_dmr_act():
    query = "Evaluating label claims: 100% cure for diabetes mellitus and cancer prevention."
    routing = router_service.route_intent(query)
    
    assert routing.document_analysis is True
    
    product = router_service.extract_entities(query)
    reg_result = regulatory_service.evaluate_regulatory_guidance(product)
    
    # Must flag high risk under Drugs and Magic Remedies Act
    assert any("DMR Act" in r or "Section 3" in r or "cure" in r.lower() for r in reg_result.statutory_risks)
    
    claim_item = next((c for c in reg_result.checklist if c.id == "req_05"), None)
    assert claim_item is not None
    assert claim_item.status == "missing"
    assert "DMR" in claim_item.evidence_passage or "objectionable" in claim_item.evidence_passage.lower()


# ----------------------------------------------------------------------
# TEST 7: Insufficient Evidence / Safe Partial Abstention
# ----------------------------------------------------------------------
def test_07_insufficient_evidence_safe_abstention():
    # Synthetic chemical with no botanical ingredients
    product = ProductDetails(
        name="Fluorinated Synthetic Polymer XYZ-99",
        ingredients=[],  # Zero botanicals
        intended_use="Industrial coating",
        dosage_form="Liquid",
        claims=[]
    )
    
    reg_result = regulatory_service.evaluate_regulatory_guidance(product)
    
    assert reg_result.trust_report.safe_abstention_triggered is True
    assert reg_result.trust_report.retrieval_confidence < 0.60
    assert len(reg_result.missing_information) > 0


# ----------------------------------------------------------------------
# TEST 8: Conflicting Evidence Handling (Classical vs Patent Novelty)
# ----------------------------------------------------------------------
def test_08_conflicting_evidence_handling():
    # User claims formulation is verbatim classical Charaka Samhita but also claims patent novelty
    synthesis = cross_domain_reasoner.synthesize(
        query="Can I patent a verbatim classical formula from Charaka Samhita?",
        product=ProductDetails(name="Classical Triphala Churna", ingredients=["Haritaki", "Bibhitaki", "Amalaki"]),
        ip_findings=["Prior art found in Charaka Samhita Chikitsa Sthana. Section 3(p) rejection likely."],
        regulatory_findings=["Eligible for Classical Ayurvedic Drug license with no safety trials under Rule 158B."]
    )
    
    assert "Section 3(p)" in synthesis
    assert "Rule 158B" in synthesis
    assert "TRADE-OFF" in synthesis.upper() or "PARADOX" in synthesis.upper() or "TENSION" in synthesis.upper() or "CLASSICAL" in synthesis.upper()


# ----------------------------------------------------------------------
# TEST 9: Regulatory Failure Resilience (IP succeeds gracefully)
# ----------------------------------------------------------------------
def test_09_regulatory_module_failure_resilience():
    req = QueryRequest(question="Licensing and patenting Ashwagandha extract with synergetic bioavailability")
    
    # Simulate an internal crash inside the regulatory engine
    with patch.object(regulatory_service, "evaluate_regulatory_guidance", side_effect=RuntimeError("Regulatory DB Timeout")):
        res = query_service.process_query(req)
        
        # Pipeline must not crash!
        assert res.answer is not None
        assert res.regulatory_analysis is None
        assert res.component_confidence is not None
        assert res.component_confidence.abstention_flags.get("regulatory") is True
        assert res.component_confidence.ip_confidence is not None


# ----------------------------------------------------------------------
# TEST 10: IP Module Failure Resilience (Regulatory succeeds gracefully)
# ----------------------------------------------------------------------
def test_10_ip_module_failure_resilience():
    req = QueryRequest(question="Licensing and patenting novel Brahmi churnam")
    
    # Simulate an internal crash inside the IP engine
    with patch.object(query_service, "_run_ip_analysis", side_effect=RuntimeError("FAISS vectorstore corrupted")):
        res = query_service.process_query(req)
        
        # Pipeline must not crash!
        assert res.regulatory_analysis is not None
        assert res.component_confidence is not None
        assert res.component_confidence.abstention_flags.get("ip") is True
        assert res.component_confidence.regulatory_confidence is not None


# ----------------------------------------------------------------------
# BONUS: Graph and Roadmap Service Integrity
# ----------------------------------------------------------------------
def test_graph_and_roadmap_dynamic_construction():
    product = ProductDetails(
        name="Ashwagandha & Brahmi Synergy Drops",
        ingredients=["Ashwagandha", "Brahmi"],
        dosage_form="Syrup"
    )
    reg_result = regulatory_service.evaluate_regulatory_guidance(product)
    
    graph = graph_service.build_unified_graph(
        query="Novel syrup patent and licensing",
        product=product,
        ip_findings=["Sec 3(p) prior art risk"],
        regulatory_result=reg_result,
        jurisdictions=["India", "USA"]
    )
    assert len(graph.nodes) >= 8
    assert any(n.label == "Ashwagandha" for n in graph.nodes)
    assert any(n.label == "Brahmi" for n in graph.nodes)
    assert any("DSHEA" in n.label for n in graph.nodes)
    
    roadmap = roadmap_service.build_unified_roadmap(
        product=product,
        regulatory_result=reg_result,
        ip_required=True,
        regulatory_required=True,
        international_required=True
    )
    assert len(roadmap.steps) >= 5
    assert any("Form III" in s.form_required for s in roadmap.steps)
    assert any("US FDA" in s.title or "DSHEA" in s.title for s in roadmap.steps)
