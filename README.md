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
Native Android App (Kotlin + Jetpack Compose)
         ↓  REST API
FastAPI Backend (Python)
         ↓
IP-SAKTI RAG Pipeline
         ↓
FAISS Vector Store + Groq LLM
         ↓
Grounded Response with Citations
```

## Repository Structure

```
IP-SAKTI-SAHAYAK/
├── android/                    # Native Android Studio project
│   ├── app/
│   │   ├── build.gradle.kts
│   │   └── src/main/
│   │       ├── AndroidManifest.xml
│   │       └── java/com/ipsakti/sahayak/
│   │           ├── MainActivity.kt
│   │           ├── IpSaktiApplication.kt
│   │           ├── data/
│   │           │   ├── api/        # Retrofit API service
│   │           │   ├── model/      # Data classes
│   │           │   └── repository/ # Repository layer
│   │           └── ui/
│   │               ├── theme/      # Material 3 theme
│   │               ├── navigation/ # Compose navigation
│   │               ├── components/ # Reusable components
│   │               └── screens/    # All app screens
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradle/
│
├── backend/                    # FastAPI + Python Pipeline
│   ├── main.py
│   ├── models.py
│   ├── demo_data.py
│   ├── requirements.txt
│   ├── pipeline/               # Extracted RAG modules
│   │   ├── config.py
│   │   ├── document_loader.py
│   │   ├── text_cleaner.py
│   │   ├── chunker.py
│   │   ├── embeddings.py
│   │   ├── vector_store.py
│   │   ├── retriever.py
│   │   ├── prompts.py
│   │   ├── llm.py
│   │   ├── rag_pipeline.py
│   │   └── ingestion.py
│   └── services/
│
├── ps45.py                     # Original Colab notebook (reference)
├── .env.example
├── .gitignore
└── README.md
```

## Quick Start

### 1. Clone

```bash
git clone https://github.com/SomSatwik/IP-SAKTI-SAHAYAK.git
cd IP-SAKTI-SAHAYAK
```

### 2. Backend Setup

```bash
cd backend
pip install -r requirements.txt

# Create .env in the project root
cp ../.env.example ../.env
# Edit .env and add: GROQ_API_KEY=your_key_here

# Start the FastAPI server
cd ..
python -m uvicorn backend.main:app --host 0.0.0.0 --port 8000 --reload
```

The backend starts in **demo mode** if no API key or vector index is present. All demo endpoints work immediately.

### 3. Android Setup

1. Open `android/` directory in **Android Studio**
2. Wait for Gradle sync to complete
3. Start the backend server (step 2)
4. Run the app on emulator or physical device

**For Android Emulator:** The app automatically connects to `http://10.0.2.2:8000/` (maps to host localhost).

**For Physical Device:** Go to Settings in the app → change backend URL to `http://<your-computer-ip>:8000/`

### 4. Demo Flow

1. Open app → Home screen with dashboard
2. Tap **"Load Demo"** → see a complete pre-loaded investigation
3. Or tap **"Investigate"** → enter your own IP case
4. View the structured investigation report
5. Navigate to **Evidence Graph** → interactive visualization
6. Navigate to **Compliance Roadmap** → actionable steps
7. Tap any evidence card → see exact authoritative passage

## App Screens

| Screen | Description |
|--------|-------------|
| **Home** | Executive dashboard with metrics, quick actions |
| **Investigate** | Multi-domain IP analysis input with mode selection |
| **Report** | Structured investigation with confidence, risks, evidence |
| **Evidence Detail** | Full source verification and supporting passage |
| **Evidence Graph** | Interactive canvas with zoom, pan, tap-to-inspect |
| **Compliance Roadmap** | Timeline with priority badges and status |
| **Saved** | Investigation history archive |
| **Upload** | Document ingestion pipeline visualization |
| **Settings** | Backend URL config, connection test, system info |

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

## Technology Stack

| Layer | Technology |
|-------|-----------|
| **Android** | Kotlin, Jetpack Compose, Material 3, Navigation Compose |
| **Networking** | Retrofit 2, OkHttp, Gson |
| **Architecture** | ViewModel, Coroutines, StateFlow |
| **Backend** | FastAPI, Pydantic, Uvicorn |
| **AI/RAG** | LangChain, FAISS, BGE-M3 (BAAI), Groq LLM |
| **Documents** | PyPDF, Legal-aware chunking |

## Security

- ⚠️ **Never commit `.env`** — it is in `.gitignore`
- ⚠️ **Never put API keys in the Android app** — the Groq key belongs ONLY on the backend
- ⚠️ **Rotate any exposed keys** immediately at [console.groq.com](https://console.groq.com)

## Environment Variables

| Variable | Location | Required | Description |
|----------|----------|----------|-------------|
| `GROQ_API_KEY` | Backend `.env` | Yes | Groq API key for LLM |

The Android application contains **zero secrets**. It communicates only with the backend API.

## Team

Smart India Hackathon 2026

## Disclaimer

This system provides informational and research assistance only. It does not constitute legal advice. Users should consult qualified legal professionals for specific legal matters.
