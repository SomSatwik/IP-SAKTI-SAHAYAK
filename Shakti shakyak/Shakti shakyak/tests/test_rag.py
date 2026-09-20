"""
Unit tests for Hybrid RAG Retrieval, Section 3(d) / Section 3(p) precision, and Safe Abstention.
"""

import pytest
from rag.hybrid_search import hybrid_searcher
from ai.domain_classifier import domain_classifier
from ai.jurisdiction import jurisdiction_detector
from ai.abstention import abstention_engine


def test_domain_classifier_patent():
    result = domain_classifier.classify("What is Section 3(d) of the Indian Patents Act?")
    assert result["primary_domain"] == "Patent"


def test_domain_classifier_ayurveda():
    result = domain_classifier.classify("Can I protect an Ayurvedic herbal formulation using Ashwagandha?")
    assert "Ayurveda" in result["all_detected"] or "Traditional Knowledge" in result["all_detected"]


def test_jurisdiction_detector():
    res_india = jurisdiction_detector.detect("Under Indian patent law, what is required for patent grant?")
    assert res_india["jurisdiction"] == "India"

    res_pct = jurisdiction_detector.detect("What is the PCT international filing timeline under WIPO?")
    assert res_pct["jurisdiction"] == "International"


def test_hybrid_search_section_3d():
    results = hybrid_searcher.search("Section 3(d) enhanced efficacy of known substance", top_k=5)
    assert len(results) > 0
    top_text = " ".join([r["text"] for r in results])
    assert "3(d)" in top_text or "Section 3" in top_text


def test_hybrid_search_traditional_knowledge():
    results = hybrid_searcher.search("Traditional Knowledge Section 3(p) TKDL bar", top_k=5)
    assert len(results) > 0
    top_text = " ".join([r["text"] for r in results])
    assert "3(p)" in top_text or "traditional knowledge" in top_text.lower()


def test_safe_abstention_out_of_scope():
    check = abstention_engine.evaluate_abstention(
        query="What will be the price of gold in 2035?",
        evidence_chunks=[],
        confidence_score=0.10
    )
    assert check["should_abstain"] is True
