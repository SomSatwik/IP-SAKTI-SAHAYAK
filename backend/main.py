from fastapi import FastAPI, HTTPException, Depends
from fastapi.middleware.cors import CORSMiddleware
from typing import List
import uuid
from datetime import datetime

from .models import (
    QueryRequest, QueryResponse, InvestigationSummary, 
    InvestigationDetail, EvidenceGraphResponse, 
    ComplianceRoadmapResponse, DocumentUploadResponse,
    HealthResponse, DashboardStats
)

from .demo_data import demo_dashboard_stats, demo_investigation, DEMO_INVESTIGATION_ID
from .services.query_service import query_service
from .services.investigation_service import investigation_service

app = FastAPI(title="IP-SAKTI SAHAYAK API")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/api/health", response_model=HealthResponse)
async def health_check():
    return HealthResponse(
        status="ok",
        mode="pipeline" if query_service.is_ready else "demo",
        version="1.0.0",
        pipeline_ready=query_service.is_ready
    )

@app.post("/api/query", response_model=QueryResponse)
async def process_query(request: QueryRequest):
    return query_service.process_query(request)

@app.post("/api/analyze", response_model=InvestigationDetail)
async def analyze_case(request: QueryRequest):
    response = query_service.process_query(request)
    
    # In demo mode, if it's the exact demo query, return the rich demo investigation
    if not query_service.is_ready or request.question == demo_investigation.query:
        return demo_investigation
        
    # Otherwise build a new investigation detail (mocked extra details for now)
    new_id = f"inv_{uuid.uuid4().hex[:8]}"
    detail = InvestigationDetail(
        id=new_id,
        query=request.question,
        timestamp=datetime.now(),
        domain="General IP",
        status="Completed",
        response=response,
        graph=demo_investigation.graph,  # Reuse demo graph for structural demo
        roadmap=demo_investigation.roadmap # Reuse demo roadmap
    )
    investigation_service.add_investigation(detail)
    return detail

@app.post("/api/deep-analysis", response_model=InvestigationDetail)
async def deep_analysis(request: QueryRequest):
    return await analyze_case(request)

@app.post("/api/documents/upload", response_model=DocumentUploadResponse)
async def upload_document():
    # Mock endpoint
    return DocumentUploadResponse(
        status="success",
        message="Document uploaded and processing started.",
        document_id=f"doc_{uuid.uuid4().hex[:8]}"
    )

@app.get("/api/documents")
async def list_documents():
    return []

@app.get("/api/sources")
async def list_sources():
    return []

@app.get("/api/investigations", response_model=List[InvestigationSummary])
async def list_investigations():
    return investigation_service.get_all_summaries()

@app.get("/api/investigations/{inv_id}", response_model=InvestigationDetail)
async def get_investigation(inv_id: str):
    inv = investigation_service.get_investigation(inv_id)
    if not inv:
        raise HTTPException(status_code=404, detail="Investigation not found")
    return inv

@app.get("/api/evidence/{inv_id}")
async def get_evidence(inv_id: str):
    inv = investigation_service.get_investigation(inv_id)
    if not inv:
        raise HTTPException(status_code=404, detail="Investigation not found")
    return inv.response.evidence

@app.get("/api/graph/{inv_id}", response_model=EvidenceGraphResponse)
async def get_graph(inv_id: str):
    inv = investigation_service.get_investigation(inv_id)
    if not inv:
        raise HTTPException(status_code=404, detail="Investigation not found")
    return inv.graph

@app.get("/api/roadmap/{inv_id}", response_model=ComplianceRoadmapResponse)
async def get_roadmap(inv_id: str):
    inv = investigation_service.get_investigation(inv_id)
    if not inv:
        raise HTTPException(status_code=404, detail="Investigation not found")
    return inv.roadmap

@app.get("/api/dashboard/stats", response_model=DashboardStats)
async def get_dashboard_stats():
    return demo_dashboard_stats

@app.get("/api/demo/investigation", response_model=InvestigationDetail)
async def get_demo_investigation():
    return demo_investigation

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("backend.main:app", host="0.0.0.0", port=8000, reload=True)
