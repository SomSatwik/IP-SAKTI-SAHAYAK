"""
Unit tests for Citation Verification and Multi-Signal Confidence Scoring.
"""

import pytest
from ai.citation_verifier import citation_verifier
from ai.confidence import confidence_scorer


def test_citation_extraction_and_verification():
    mock_evidence = [
        {
            "document_title": "The Patents Act, 1970",
            "authority": "CGPDTM",
            "jurisdiction": "India",
            "section": "Section 3(d)",
            "source_url": "https://ipindia.gov.in",
            "text": "Section 3(d) requires enhancement of known efficacy."
        },
        {
            "document_title": "Biological Diversity Act, 2002",
            "authority": "National Biodiversity Authority",
            "jurisdiction": "India",
            "section": "Section 6",
            "source_url": "https://nbaindia.org",
            "text": "Section 6 mandates NBA clearance before applying for IPR."
        }
    ]

    answer = "Under Indian law, Section 3(d) requires enhanced efficacy [Source 1], and NBA approval is mandated by Section 6 [Source 2]."
    result = citation_verifier.verify(answer, mock_evidence)

    assert result["is_valid"] is True
    assert result["validity_score"] == 1.0
    assert len(result["verified_sources"]) == 2


def test_invalid_citation_handling():
    mock_evidence = [
        {"document_title": "Single Act", "authority": "Auth", "section": "Sec 1", "text": "Text"}
    ]
    # Source 5 does not exist
    answer = "According to Section 99 [Source 5]."
    result = citation_verifier.verify(answer, mock_evidence)

    assert 5 in result["invalid_indices"]
    assert result["validity_score"] == 0.0
    # Sanitized answer removes the invalid citation
    assert "[Source 5]" not in result["sanitized_answer"]


def test_confidence_calculation():
    mock_evidence = [
        {
            "retrieval_score": 0.88,
            "authority_level": "LEVEL_1"
        },
        {
            "retrieval_score": 0.75,
            "authority_level": "LEVEL_1"
        }
    ]
    citation_info = {"validity_score": 1.0}
    conf = confidence_scorer.calculate(mock_evidence, citation_info, "Section 3(d)")

    assert conf["score"] >= 0.70
    assert conf["level"] in ("HIGH", "MEDIUM")
