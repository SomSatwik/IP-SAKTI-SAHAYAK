"""
Integration tests for Flask API endpoints.
"""

import pytest
from app import create_app


@pytest.fixture
def client():
    app = create_app()
    app.config["TESTING"] = True
    with app.test_client() as client:
        yield client


def test_index_route(client):
    res = client.get("/")
    assert res.status_code == 200
    assert b"IP-SAKTI SAHAYAK" in res.data


def test_health_endpoint(client):
    res = client.get("/api/health")
    assert res.status_code == 200
    json_data = res.get_json()
    assert "status" in json_data
    assert json_data["vector_store"] is True
    assert json_data["database"] is True


def test_sources_endpoint(client):
    res = client.get("/api/sources")
    assert res.status_code == 200
    json_data = res.get_json()
    assert json_data["success"] is True
    assert json_data["count"] > 0


def test_search_endpoint(client):
    res = client.post("/api/search", json={"query": "Section 3(d)", "top_k": 3})
    assert res.status_code == 200
    json_data = res.get_json()
    assert json_data["success"] is True
    assert len(json_data["results"]) > 0


def test_compliance_endpoint(client):
    res = client.post("/api/compliance", json={
        "applicant_type": "foreign_entity",
        "uses_biological_resource": True,
        "commercial_intent": True,
        "ip_filing_intended": True
    })
    assert res.status_code == 200
    json_data = res.get_json()
    assert json_data["success"] is True
    assert "Form III" in json_data["mandatory_forms"]


def test_prior_art_endpoint(client):
    res = client.post("/api/prior-art", json={
        "description": "Ashwagandha and Turmeric extract for inflammation"
    })
    assert res.status_code == 200
    json_data = res.get_json()
    assert json_data["success"] is True
    assert len(json_data["detected_botanicals"]) >= 2
