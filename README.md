# IP-SAKTI SAHAYAK

### AI-Powered Indian IP Intelligence & Compliance Workspace

> **Smart India Hackathon 2026**

IP-SAKTI SAHAYAK transforms complex Indian intellectual property, traditional knowledge, biodiversity, and Ayurveda-related regulatory knowledge into actionable intelligence through evidence-based analysis.

## Core Philosophy

```
SEARCH → UNDERSTAND → VERIFY → EXPLAIN → ACT
```

This is NOT a generic chatbot. It is a **grounded IP intelligence workspace** that provides:

- **Evidence Graph** — Interactive visualization of legal/regulatory relationships
- **Verified Citations** — Every claim traceable to authoritative sources
- **Safe Abstention** — System refuses to answer when evidence is insufficient
- **Compliance Roadmap** — Actionable investigation steps
- **Transparent Confidence** — Explainable confidence scoring

## Architecture

```
Frontend (React + Tailwind)
    ↓
FastAPI Backend
    ↓
Python AI/RAG Pipeline
    ↓
FAISS Vector Store + Groq LLM
    ↓
Grounded Response with Citations
```

## Quick Start

### Prerequisites

- Python 3.9+
- Node.js 18+
- Groq API Key ([console.groq.com](https://console.groq.com))

### 1. Environment Setup

```bash
# Copy environment template
cp .env.example .env

# Add your Groq API key to .env
# GROQ_API_KEY=your_key_here
```

### 2. Backend

```bash
cd backend
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```

### 3. Frontend

```bash
cd frontend
npm install
npm run dev
```

Open [http://localhost:5173](http://localhost:5173)

## Features

| Feature | Status |
|---------|--------|
| Grounded IP Query | ✅ |
| Evidence & Citations | ✅ |
| Safe Abstention | ✅ |
| Confidence Scoring | ✅ |
| Evidence Graph | ✅ |
| Compliance Roadmap | ✅ |
| Investigation Workspace | ✅ |
| Document Upload | ✅ |
| Knowledge Base | ✅ |
| Demo Mode | ✅ |
| Multilingual (English) | ✅ |
| Hindi / Odia | 🔜 |
| Voice Input | 🔜 |

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/health` | Health check |
| POST | `/api/query` | Quick answer |
| POST | `/api/analyze` | Standard analysis |
| POST | `/api/deep-analysis` | Deep investigation |
| POST | `/api/documents/upload` | Upload document |
| GET | `/api/documents` | List documents |
| GET | `/api/sources` | List sources |
| GET | `/api/investigations` | List investigations |
| GET | `/api/investigations/{id}` | Investigation detail |
| GET | `/api/evidence/{id}` | Evidence detail |
| GET | `/api/graph/{id}` | Evidence graph data |
| GET | `/api/roadmap/{id}` | Compliance roadmap |
| GET | `/api/dashboard/stats` | Dashboard statistics |
| GET | `/api/demo/investigation` | Demo investigation |

## Environment Variables

| Variable | Required | Description |
|----------|----------|-------------|
| `GROQ_API_KEY` | Yes | Groq API key for LLM |

## Technology Stack

- **Frontend**: React, Vite, Tailwind CSS, React Router, react-force-graph-2d
- **Backend**: FastAPI, Pydantic, Uvicorn
- **AI/RAG**: LangChain, FAISS, BGE-M3 (BAAI), Groq LLM
- **Documents**: PyPDF, Legal-aware chunking

## Team

Smart India Hackathon 2026

## Disclaimer

This system provides informational and research assistance only. It does not constitute legal advice. Users should consult qualified legal professionals for specific legal matters.
