"""
IP-SAKTI SAHAYAK
Authoritative Legal Knowledge Ingestion Pipeline
================================================
Parses raw primary statutes, executes legal-aware chunking, builds the FAISS
vector index, creates BM25 indices, and populates the SQLite database.
"""

import json
import logging
from pathlib import Path
from config import Config
from database.db import init_db, get_db_session
from database.models import Source, Document, DocumentChunk
from rag.chunker import LegalChunker
from rag.vector_store import vector_store
from rag.hybrid_search import hybrid_searcher

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)


def ingest_seed_corpus():
    """Ingest primary seed legal documents into database, FAISS, and BM25."""
    logger.info("Initializing Database...")
    init_db()

    # 1. Register Official Sources
    with get_db_session() as session:
        for s in Config.OFFICIAL_SOURCES:
            existing = session.query(Source).filter_by(id=s["id"]).first()
            if not existing:
                src_rec = Source(
                    id=s["id"],
                    name=s["name"],
                    base_url=s["base_url"],
                    jurisdiction=s.get("jurisdiction", "India"),
                    domain=s.get("domain", "General IP"),
                    authority=s["authority"],
                    authority_level=s.get("authority_level", "LEVEL_1"),
                    enabled=s.get("enabled", True),
                    priority=s.get("priority", 1)
                )
                session.add(src_rec)
        session.flush()

    # 2. Load Raw Legal JSON Corpus
    seed_file = Config.RAW_DATA_PATH / "seed_legal_corpus.json"
    if not seed_file.exists():
        logger.error("Seed file not found at %s", seed_file)
        return

    with open(seed_file, "r", encoding="utf-8") as f:
        docs_data = json.load(f)

    chunker = LegalChunker(target_chunk_size=700, overlap_size=100)
    all_chunks = []

    logger.info("Chunking and indexing %d primary legal acts...", len(docs_data))

    with get_db_session() as session:
        for doc_entry in docs_data:
            meta = doc_entry.get("metadata", {})
            text = doc_entry.get("text", "")

            # Create Document record
            doc_rec = Document(
                source_id=meta.get("source_id"),
                title=meta.get("title", "Official Statute"),
                document_type=meta.get("document_type", "Act"),
                jurisdiction=meta.get("jurisdiction", "India"),
                domain=meta.get("domain", "General IP"),
                official_url=meta.get("source_url"),
                version=meta.get("version", "1.0"),
                effective_date=meta.get("effective_date")
            )
            session.add(doc_rec)
            session.flush()

            # Execute legal-aware chunking
            chunks = chunker.chunk_legal_text(text=text, metadata=meta)

            for c in chunks:
                chunk_rec = DocumentChunk(
                    document_id=doc_rec.id,
                    chunk_index=c["chunk_index"],
                    section=c["section"],
                    text=c["text"],
                    content_hash=c["content_hash"]
                )
                session.add(chunk_rec)
                all_chunks.append(c)

    logger.info("Generated %d legal chunks.", len(all_chunks))

    # 3. Add to FAISS Vector Store
    logger.info("Generating embeddings and building FAISS index...")
    vector_store.create_index()
    vector_store.add_chunks(all_chunks)

    # 4. Build and persist BM25 Index
    logger.info("Building BM25 keyword search index...")
    hybrid_searcher.build_bm25_index(all_chunks)

    logger.info("Successfully completed ingestion of %d legal chunks into FAISS and BM25!", len(all_chunks))


if __name__ == "__main__":
    ingest_seed_corpus()
