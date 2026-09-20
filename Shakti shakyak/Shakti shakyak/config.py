"""
IP-SAKTI SAHAYAK
Configuration Module
====================
Centralized application configuration, environment variable loader,
authoritative source registry, and system-wide threshold settings.
"""

import os
from pathlib import Path
from dotenv import load_dotenv

# Base Directory
BASE_DIR = Path(__file__).resolve().parent

# Load environment variables from .env if present
load_dotenv(BASE_DIR / ".env")


class Config:
    """Application configuration and authoritative legal source registry."""

    # Core Application
    BASE_DIR = BASE_DIR
    SECRET_KEY = os.getenv("SECRET_KEY", "ip_sakti_sahayak_secret_key_2026_sih")
    DEBUG = os.getenv("DEBUG", "True").lower() in ("true", "1", "yes")
    PORT = int(os.getenv("PORT", 5000))

    # Groq LLM Configuration
    GROQ_API_KEY = os.getenv("GROQ_API_KEY", "")
    GROQ_MODEL = os.getenv("GROQ_MODEL", "openai/gpt-oss-120b")
    GROQ_TEMPERATURE = float(os.getenv("GROQ_TEMPERATURE", 0.1))  # Low temperature for deterministic legal grounding
    GROQ_MAX_TOKENS = int(os.getenv("GROQ_MAX_TOKENS", 2048))

    # Embedding & Reranker Configuration
    EMBEDDING_MODEL = os.getenv("EMBEDDING_MODEL", "BAAI/bge-m3")
    RERANKER_MODEL = os.getenv("RERANKER_MODEL", "cross-encoder/ms-marco-MiniLM-L-6-v2")
    EMBEDDING_DIMENSION = 1024

    # Database
    DATABASE_URL = os.getenv("DATABASE_URL", f"sqlite:///{BASE_DIR / 'ip_sakti.db'}")

    # Paths
    DATA_DIR = BASE_DIR / "data"
    RAW_DATA_PATH = DATA_DIR / "raw"
    PROCESSED_DATA_PATH = DATA_DIR / "processed"
    INDEX_DIR = DATA_DIR / "index"
    CACHE_DIR = DATA_DIR / "cache"
    REPORTS_DIR = DATA_DIR / "reports"
    LOGS_DIR = BASE_DIR / "logs"

    VECTOR_DB_PATH = INDEX_DIR / "faiss_index.bin"
    BM25_INDEX_PATH = INDEX_DIR / "bm25_index.pkl"
    CHUNKS_PATH = INDEX_DIR / "chunks.pkl"

    # Hybrid Retrieval Weights
    SEMANTIC_WEIGHT = 0.55
    KEYWORD_WEIGHT = 0.45
    LEGAL_KEYWORD_BOOST = 1.35  # Boost factor for exact section numbers like "Section 3(d)", "Section 3(p)"

    # Confidence Thresholds
    CONFIDENCE_HIGH = 0.80
    CONFIDENCE_MEDIUM = 0.60
    CONFIDENCE_LOW = 0.40
    ABSTENTION_THRESHOLD = 0.35  # Abstain when confidence is below this score

    # Source Authority Levels (1 = Highest Authority)
    AUTHORITY_LEVELS = {
        "LEVEL_1": "Primary Official Legislation, Gazettes, Patent Offices, Treaties",
        "LEVEL_2": "Official Regulatory Guidelines, AYUSH/NBA Guidelines, TKDL Protocols",
        "LEVEL_3": "Official International Organizations (WIPO, WTO, WHO)",
        "LEVEL_4": "Verified Institutional Publications & Repositories",
        "LEVEL_5": "General Web Sources (Secondary)"
    }

    # Authoritative Source Registry
    OFFICIAL_SOURCES = [
        {
            "id": "ip_india",
            "name": "Intellectual Property India (CGPDTM)",
            "base_url": "https://ipindia.gov.in",
            "jurisdiction": "India",
            "domain": "Patents, Trademarks, Designs, GI",
            "authority": "Controller General of Patents, Designs and Trade Marks",
            "authority_level": "LEVEL_1",
            "enabled": True,
            "priority": 1
        },
        {
            "id": "india_code",
            "name": "India Code (Legislative Department)",
            "base_url": "https://www.indiacode.nic.in",
            "jurisdiction": "India",
            "domain": "All Central Legislation & Acts",
            "authority": "Ministry of Law and Justice, Govt of India",
            "authority_level": "LEVEL_1",
            "enabled": True,
            "priority": 1
        },
        {
            "id": "ayush_ministry",
            "name": "Ministry of AYUSH",
            "base_url": "https://ayush.gov.in",
            "jurisdiction": "India",
            "domain": "Ayurveda, Yoga, Unani, Siddha, Homoeopathy, TKDL",
            "authority": "Ministry of Ayush, Govt of India",
            "authority_level": "LEVEL_2",
            "enabled": True,
            "priority": 2
        },
        {
            "id": "nba_india",
            "name": "National Biodiversity Authority (NBA)",
            "base_url": "https://nbaindia.org",
            "jurisdiction": "India",
            "domain": "Biological Diversity Act, Access & Benefit Sharing (ABS), Form III",
            "authority": "National Biodiversity Authority, Govt of India",
            "authority_level": "LEVEL_1",
            "enabled": True,
            "priority": 1
        },
        {
            "id": "tkdl_csir",
            "name": "Traditional Knowledge Digital Library (TKDL)",
            "base_url": "https://www.tkdl.res.in",
            "jurisdiction": "India",
            "domain": "Traditional Knowledge Prior-Art & Ayurveda",
            "authority": "CSIR & Ministry of Ayush",
            "authority_level": "LEVEL_2",
            "enabled": True,
            "priority": 2
        },
        {
            "id": "wipo",
            "name": "World Intellectual Property Organization (WIPO)",
            "base_url": "https://www.wipo.int",
            "jurisdiction": "International",
            "domain": "PCT, Madrid, Hague, TRIPS, Traditional Knowledge IGC",
            "authority": "United Nations Agency",
            "authority_level": "LEVEL_3",
            "enabled": True,
            "priority": 2
        },
        {
            "id": "wipo_lex",
            "name": "WIPO Lex Database",
            "base_url": "https://wipolex.wipo.int",
            "jurisdiction": "International",
            "domain": "Global IP Treaties & National IP Laws",
            "authority": "WIPO",
            "authority_level": "LEVEL_3",
            "enabled": True,
            "priority": 2
        }
    ]

    @classmethod
    def init_directories(cls):
        """Ensure all storage and cache directories exist."""
        for path in [
            cls.DATA_DIR,
            cls.RAW_DATA_PATH,
            cls.PROCESSED_DATA_PATH,
            cls.INDEX_DIR,
            cls.CACHE_DIR,
            cls.REPORTS_DIR,
            cls.LOGS_DIR
        ]:
            path.mkdir(parents=True, exist_ok=True)


# Initialize folders upon configuration import
Config.init_directories()
