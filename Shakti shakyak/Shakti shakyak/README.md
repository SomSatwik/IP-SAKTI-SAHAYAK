# IP-SAKTI SAHAYAK (SIH 2026 — PS26045)
### AI-Powered Intellectual Property, Traditional Knowledge & Regulatory Assistant

[![SIH 2026](https://img.shields.io/badge/SIH-2026-blue.svg)](https://www.sih.gov.in/)
[![Python 3.10+](https://img.shields.io/badge/Python-3.10%2B-green.svg)](https://python.org)
[![Flask](https://img.shields.io/badge/Backend-Flask-lightgrey.svg)](https://flask.palletsprojects.com/)
[![FAISS](https://img.shields.io/badge/Vector%20Store-FAISS-red.svg)](https://github.com/facebookresearch/faiss)
[![BM25](https://img.shields.io/badge/Keyword%20Search-BM25Okapi-orange.svg)](https://github.com/dorianbrown/rank_bm25)
[![Groq Cloud](https://img.shields.io/badge/LLM-Groq%20Cloud-purple.svg)](https://groq.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 1. Problem Statement Overview
- **Problem Statement ID:** PS26045 (Smart India Hackathon 2026)
- **Domain:** Intellectual Property, Ayurveda, Traditional Knowledge, Biodiversity & Regulatory Compliance.
- **Challenge:** Traditional knowledge misappropriation (biopiracy), complex statutory bars under Indian patent law (e.g. **Section 3(d)** for efficacy, **Section 3(p)** for traditional knowledge, **Section 3(e)** for admixtures), and strict compliance requirements under **Section 6 of the Biological Diversity Act, 2002** (NBA Form III approval). Generic LLMs regularly hallucinate section numbers, fabricate case citations, and invent non-existent regulatory circulars.
- **Solution:** **IP-SAKTI SAHAYAK** is a grounded, traceable, citation-enforced AI assistant that queries official statutory databases, evaluates multi-signal confidence ratings, and safely abstains when authoritative evidence is insufficient.

---

## 2. Core Architecture & Pipeline

```
                     USER QUERY (English / हिन्दी / ଓଡ଼ିଆ)
                                       │
                                       ▼
                       [Flask Web Application Server]
                                       │
                                       ▼
                       [Domain & Jurisdiction Classifier]
                                       │
                                       ▼
                             [Retrieval Coordinator]
                ┌──────────────────────┴──────────────────────┐
                ▼                                             ▼
     [Local Hybrid Knowledge Cache]            [Real-Time Official Web Scraper]
       - FAISS Dense Vector Index               - IP India (CGPDTM)
       - BM25Okapi Exact Keyword Match          - National Biodiversity Authority
       - Exact Section 3(d)/3(p) Boost          - Ministry of Ayush / TKDL
                └──────────────────────┬──────────────────────┘
                                       ▼
                        [Precision Legal Reranker]
                         (Top 20 -> Top 5 Chunks)
                                       │
                                       ▼
                        [Safe Abstention Evaluator]
                                       │
               ├── (Confidence < 0.35 or Out of Scope) ──► [Refusal / Abstention Notice]
               └── (Sufficient Primary Evidence)
                                       │
                                       ▼
                     [Grounded LLM Prompting via Groq API]
                     (`openai/gpt-oss-120b` / `llama-3.3-70b`)
                                       │
                                       ▼
                        [Citation Verification Engine]
               (Validates [Source X] against retrieved corpus)
                                       │
                                       ▼
                        [Calibrated Confidence Scorer]
                           (0 - 100% Quality Metric)
                                       │
                                       ▼
                     [Interactive 3-Panel Legal-Tech UI]
```

---

## 3. Key Differentiators & Specialized Modules

### A. Authoritative Source Hierarchy (Levels 1–5)
- **Level 1:** Primary Legislation, Acts, Gazettes (The Patents Act 1970, Biological Diversity Act 2002, GI Act 1999).
- **Level 2:** Official Regulatory Guidelines, TKDL Protocols, AYUSH Pharmacopoeial Standards.
- **Level 3:** Official International Treaty Repositories (WIPO Lex, WTO TRIPS, PCT, Nagoya Protocol).
- **Level 4:** Institutional research repositories and certified university corpora.
- **Level 5:** Secondary sources (general web corroboration only).

### B. Specialized Feature Services
1. **Prior-Art Discovery Engine (`services/prior_art_search.py`):**
   - Scans formulations for active botanicals (Ashwagandha, Turmeric, Neem, Tulsi, Triphala).
   - Identifies Section 3(p) (traditional knowledge bar) and Section 3(e) (mere admixture) statutory risks.
   - Discloses classical treatise references (Charaka Samhita, Sushruta Samhita) and IPC sub-classes.
2. **Biodiversity & ABS Compliance Navigator (`services/compliance_engine.py`):**
   - Step-by-step statutory evaluator for commercializing biological resources from India.
   - Evaluates applicant status (Indian entity, NRI, foreign entity) and determines mandatory Form I, Form II, or Form III approvals from the National Biodiversity Authority.
3. **Interactive Knowledge Graph (`services/knowledge_graph.py`):**
   - Canvas-based visual semantic network mapping Ayurvedic plants, classical texts, patent bars, and government authorities.
4. **Regulatory Change Monitor (`services/regulation_monitor.py`):**
   - Live notification tracker for recent gazette amendments and AYUSH circulars.
5. **Printable Research Dossier Generator (`services/report_generator.py`):**
   - Generates audit-ready HTML/PDF dossiers with timestamped statutory citations and confidence telemetry.

---

## 4. Directory Structure

```text
d:/Shakti shakyak/
├── app.py                      # Flask Application Server & Blueprint Registry
├── config.py                   # Centralized Configuration & Source Registry
├── ingest_seed_data.py         # Primary Statute Ingestion & Index Builder
├── requirements.txt            # Python Dependencies
├── .env                        # Active Environment Configuration
├── .env.example                # Environment Template
├── README.md                   # Complete Documentation
│
├── ai/
│   ├── llm.py                  # Groq API Client & Fallback Engine
│   ├── prompts.py              # Grounded Legal Prompts & Anti-Hallucination Schemas
│   ├── domain_classifier.py    # IP & Traditional Knowledge Domain Classifier
│   ├── jurisdiction.py         # Territorial Jurisdiction Detector
│   ├── confidence.py           # Multi-Signal Confidence Calculator
│   ├── citation_verifier.py    # Grounding & Source Truthfulness Verifier
│   ├── abstention.py           # Safe Abstention Engine
│   └── multilingual.py         # English, Hindi & Odia Language Manager
│
├── rag/
│   ├── chunker.py              # Legal-Aware Hierarchical Chunker
│   ├── document_loader.py      # Multi-format parser (PDF, HTML, TXT, JSON)
│   ├── embeddings.py           # Normalized Multilingual Embeddings
│   ├── vector_store.py         # FAISS Vector Store with Persistence
│   ├── hybrid_search.py        # BM25 + FAISS Hybrid Fusion Engine
│   ├── reranker.py             # Precision Reranker
│   ├── web_sources.py          # Trusted Official Domain Fetcher
│   └── rag_pipeline.py         # End-to-End Hybrid RAG Orchestrator
│
├── database/
│   ├── db.py                   # SQLAlchemy Engine & Session Context
│   ├── models.py               # ORM Models (QueryLog, Source, Document, Citation)
│   └── cache.py                # Disk & Memory Cache with SHA-256 Hashing
│
├── services/
│   ├── prior_art_search.py     # Herbal Formulation Prior-Art Analyzer
│   ├── compliance_engine.py    # Biological Diversity Act / ABS Evaluator
│   ├── knowledge_graph.py      # Relational Graph Engine
│   ├── regulation_monitor.py   # Regulatory Gazette Monitor
│   └── report_generator.py     # Printable HTML Dossier Generator
│
├── routes/
│   ├── chat_routes.py          # /chat, /api/chat, /api/history, /api/reports
│   ├── search_routes.py        # /prior-art, /compliance, /api/search
│   ├── source_routes.py        # /sources, /knowledge-graph, /api/sources
│   └── admin_routes.py         # /admin, /api/health
│
├── templates/
│   ├── base.html               # Base Boilerplate & Theme Navigation
│   ├── index.html              # Landing Page with Hero & Feature Cards
│   ├── chat.html               # 3-Panel Legal Research Chat Workspace
│   ├── dashboard.html          # Metrics & Regulatory Updates Dashboard
│   ├── prior_art.html          # Prior-Art Formulation Scanner
│   ├── compliance.html         # ABS Section 6 Compliance Navigator
│   ├── knowledge_graph.html    # Interactive Canvas Network Visualizer
│   ├── sources.html            # Authoritative Source Registry
│   └── admin.html              # System Diagnostics & Health Monitor
│
├── static/
│   ├── css/                    # style.css, chat.css, dashboard.css, components.css
│   └── js/                     # app.js, chat.js, search.js, graph.js
│
├── data/
│   ├── raw/                    # Seed Legal Corpus (Indian Acts, Treaties, TKDL)
│   └── index/                  # FAISS & BM25 Binary Serializations
│
└── tests/
    ├── test_rag.py             # Domain, Jurisdiction & Statutory Search Tests
    ├── test_citations.py       # Citation Extraction & Confidence Tests
    └── test_api.py             # Integration Tests for Flask REST Endpoints
```

---

## 5. Getting Started & Installation

### Prerequisites
- Python 3.10+ (Tested up to Python 3.14 on Windows/Linux)
- Git

### 1. Clone & Install Dependencies
```bash
git clone https://github.com/your-username/IP-SAKTI-Sahayak.git
cd IP-SAKTI-Sahayak

pip install -r requirements.txt
```

### 2. Configure Environment Variables
Copy `.env.example` to `.env` and set your Groq API key:
```ini
GROQ_API_KEY=gsk_your_groq_api_key_here
GROQ_MODEL=openai/gpt-oss-120b
EMBEDDING_MODEL=BAAI/bge-m3
DATABASE_URL=sqlite:///ip_sakti.db
DEBUG=True
PORT=5000
```

### 3. Build & Ingest Knowledge Index
Run the ingestion script to initialize the SQLite database, chunk primary statutes, and build the FAISS & BM25 indexes:
```bash
python ingest_seed_data.py
```

### 4. Start the Application
```bash
python app.py
```
Open your browser and navigate to:
**`http://127.0.0.1:5000`**

---

## 6. Running the Automated Test Suite

Execute the complete test suite verifying retrieval precision, citation verifications, and API contracts:
```bash
python -m pytest -v
```
All 15 tests should pass with 100% green status.

---

## 7. API Reference Summary

| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/chat` | Main RAG inquiry endpoint. Accepts `query`, `jurisdiction`, `language`. Returns grounded answer, confidence score, and verified source cards. |
| `GET` | `/api/history` | Returns recent inquiry logs and confidence ratings. |
| `POST` | `/api/prior-art` | Analyzes botanical formulation for classical TKDL prior art and Section 3(p) hurdles. |
| `POST` | `/api/compliance` | Evaluates Biological Diversity Act obligations, Section 6 triggers, and NBA forms. |
| `GET` | `/api/sources` | Returns all registered official government sources and priority tiers. |
| `GET` | `/api/knowledge-graph`| Returns nodes and edges for the IP-Ayurveda semantic network. |
| `POST` | `/api/reports` | Generates a printable, audit-ready HTML research dossier. |
| `GET` | `/api/health` | Diagnostic health check (LLM connectivity, FAISS status, DB status). |

---

## 8. Responsible AI & Legal Disclaimer
> **IMPORTANT STATUTORY NOTICE:** IP-SAKTI SAHAYAK is designed for research, academic discovery, and preliminary regulatory guidance under Smart India Hackathon 2026. It does not provide binding legal opinions or substitute for formal patent prosecution before the Controller General of Patents, Designs and Trade Marks (CGPDTM) or the National Biodiversity Authority (NBA).
