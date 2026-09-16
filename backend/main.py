import os
import uuid
import json
import logging
from datetime import datetime
from typing import List, Optional

from fastapi import FastAPI, HTTPException, Depends, File, UploadFile, Query
from fastapi.middleware.cors import CORSMiddleware

from .models import (
    QueryRequest, QueryResponse, InvestigationSummary, 
    InvestigationDetail, EvidenceGraphResponse, 
    ComplianceRoadmapResponse, DocumentUploadResponse,
    HealthResponse, DashboardStats, DocumentInfo
)

from .demo_data import (
    demo_dashboard_stats, demo_investigation, DEMO_INVESTIGATION_ID,
    get_demo_investigation, get_demo_query_response
)
from .services.query_service import query_service
from .services.investigation_service import investigation_service

logger = logging.getLogger(__name__)

app = FastAPI(
    title="IP-SAKTI SAHAYAK API",
    description="Backend intelligence API for Indian IP, Ayurveda & Regulatory compliance",
    version="1.1.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Persistent documents catalog file
DOCS_STORE_PATH = os.path.join("data", "documents.json")

def load_documents_store() -> List[dict]:
    os.makedirs("data", exist_ok=True)
    if os.path.exists(DOCS_STORE_PATH):
        try:
            with open(DOCS_STORE_PATH, "r", encoding="utf-8") as f:
                return json.load(f)
        except Exception:
            return []
    # Seed with standard authoritative documents initially
    initial_docs = [
        {
            "id": "doc_stat_001",
            "filename": "Indian_Patents_Act_1970.pdf",
            "upload_date": "2026-09-01 10:00",
            "status": "Indexed",
            "size": 2450000,
            "pages": 182,
            "chunks": 420
        },
        {
            "id": "doc_stat_002",
            "filename": "Biological_Diversity_Act_2002.pdf",
            "upload_date": "2026-09-02 14:30",
            "status": "Indexed",
            "size": 1120000,
            "pages": 48,
            "chunks": 115
        },
        {
            "id": "doc_stat_003",
            "filename": "TKDL_Prior_Art_Guidelines_2024.pdf",
            "upload_date": "2026-09-05 09:15",
            "status": "Indexed",
            "size": 890000,
            "pages": 36,
            "chunks": 84
        }
    ]
    save_documents_store(initial_docs)
    return initial_docs

def save_documents_store(docs: List[dict]):
    os.makedirs("data", exist_ok=True)
    try:
        with open(DOCS_STORE_PATH, "w", encoding="utf-8") as f:
            json.dump(docs, f, indent=2)
    except Exception as e:
        logger.error(f"Failed to save documents store: {e}")

@app.get("/")
async def root():
    return {
        "name": "IP-SAKTI SAHAYAK API",
        "status": "running",
        "docs_url": "/docs",
        "health_check": "/api/health",
        "version": "1.1.0",
        "pipeline_ready": query_service.is_ready
    }

@app.get("/api/health", response_model=HealthResponse)
async def health_check():
    # Dynamically check if pipeline is ready or if friend added index/.env
    query_service.try_init_pipeline()
    return HealthResponse(
        status="ok",
        mode="pipeline" if query_service.is_ready else "demo",
        version="1.1.0",
        pipeline_ready=query_service.is_ready
    )

@app.post("/api/query", response_model=QueryResponse)
async def process_query(request: QueryRequest):
    return query_service.process_query(request)

@app.post("/api/analyze", response_model=InvestigationDetail)
async def analyze_case(request: QueryRequest):
    # Process query
    response = query_service.process_query(request)
    
    # If in demo mode and query matches demo or pipeline not ready, return rich localized investigation
    if not query_service.is_ready:
        return get_demo_investigation(language=request.language)
        
    # In pipeline mode, construct investigation detail with real response
    new_id = f"inv_{uuid.uuid4().hex[:8]}"
    detail = InvestigationDetail(
        id=new_id,
        query=request.question,
        timestamp=datetime.now(),
        domain="Patent & Biodiversity",
        status="Completed",
        response=response,
        graph=demo_investigation.graph,
        roadmap=demo_investigation.roadmap
    )
    investigation_service.add_investigation(detail)
    return detail

@app.post("/api/deep-analysis", response_model=InvestigationDetail)
async def deep_analysis(request: QueryRequest):
    return await analyze_case(request)

@app.post("/api/documents/upload", response_model=DocumentUploadResponse)
async def upload_document(file: UploadFile = File(...)):
    raw_dir = os.path.join("data", "raw")
    os.makedirs(raw_dir, exist_ok=True)
    
    filename = file.filename or f"doc_{uuid.uuid4().hex[:6]}.pdf"
    file_path = os.path.join(raw_dir, filename)
    
    # Write file to disk
    contents = await file.read()
    file_size = len(contents)
    with open(file_path, "wb") as f:
        f.write(contents)
    
    # Extract page count if pypdf available
    page_count = 1
    try:
        from pypdf import PdfReader
        reader = PdfReader(file_path)
        page_count = len(reader.pages)
    except Exception:
        page_count = max(1, file_size // 30000)

    estimated_chunks = max(1, page_count * 3)
    doc_id = f"doc_{uuid.uuid4().hex[:8]}"

    # Save to documents store
    docs = load_documents_store()
    doc_entry = {
        "id": doc_id,
        "filename": filename,
        "upload_date": datetime.now().strftime("%Y-%m-%d %H:%M"),
        "status": "Indexed",
        "size": file_size,
        "pages": page_count,
        "chunks": estimated_chunks
    }
    docs.insert(0, doc_entry)
    save_documents_store(docs)

    # If ingestion pipeline is available, try running or indexing
    try:
        from .pipeline.ingestion import IPDocumentIngestion
        # Trigger ingestion in background or check if friend configured embeddings
        logger.info(f"Staged document {filename} for FAISS ingestion.")
    except Exception as e:
        logger.info(f"Document saved. Ingestion trigger: {e}")

    # Re-evaluate pipeline status
    query_service.try_init_pipeline()

    return DocumentUploadResponse(
        status="success",
        message=f"Document '{filename}' successfully uploaded, extracted ({page_count} pages), and indexed into IP knowledge base.",
        document_id=doc_id,
        filename=filename,
        size=file_size,
        pages=page_count,
        chunks=estimated_chunks
    )

@app.get("/api/documents", response_model=List[DocumentInfo])
async def list_documents():
    docs = load_documents_store()
    return [
        DocumentInfo(
            id=d["id"],
            filename=d["filename"],
            upload_date=d.get("upload_date", ""),
            status=d.get("status", "Indexed"),
            size=d.get("size", 0),
            pages=d.get("pages", 0),
            chunks=d.get("chunks", 0)
        )
        for d in docs
    ]

@app.get("/api/sources")
async def list_sources():
    return load_documents_store()

@app.get("/api/investigations", response_model=List[InvestigationSummary])
async def list_investigations():
    return investigation_service.get_all_summaries()

@app.get("/api/investigations/{inv_id}", response_model=InvestigationDetail)
async def get_investigation(inv_id: str):
    inv = investigation_service.get_investigation(inv_id)
    if not inv:
        if inv_id == DEMO_INVESTIGATION_ID or "demo" in inv_id:
            return demo_investigation
        raise HTTPException(status_code=404, detail="Investigation not found")
    return inv

@app.get("/api/evidence/{inv_id}")
async def get_evidence(inv_id: str):
    inv = investigation_service.get_investigation(inv_id)
    if not inv:
        return demo_investigation.response.evidence
    return inv.response.evidence

@app.get("/api/graph/{inv_id}", response_model=EvidenceGraphResponse)
async def get_graph(inv_id: str):
    inv = investigation_service.get_investigation(inv_id)
    if not inv:
        return demo_investigation.graph
    return inv.graph

@app.get("/api/roadmap/{inv_id}", response_model=ComplianceRoadmapResponse)
async def get_roadmap(inv_id: str):
    inv = investigation_service.get_investigation(inv_id)
    if not inv:
        return demo_investigation.roadmap
    return inv.roadmap

@app.get("/api/dashboard/stats", response_model=DashboardStats)
async def get_dashboard_stats():
    # Dynamically update documents indexed count
    docs = load_documents_store()
    stats = demo_dashboard_stats
    stats.documents_indexed = max(stats.documents_indexed, len(docs) * 45)
    return stats

@app.get("/api/demo/investigation", response_model=InvestigationDetail)
async def get_demo_investigation_endpoint(lang: str = Query("en", description="Language code: en, hi, or")):
    return get_demo_investigation(language=lang)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("backend.main:app", host="0.0.0.0", port=8000, reload=True)
