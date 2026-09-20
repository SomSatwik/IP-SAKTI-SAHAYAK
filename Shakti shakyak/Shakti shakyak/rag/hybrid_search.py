"""
IP-SAKTI SAHAYAK
Hybrid Search Engine (BM25 + FAISS Fusion)
==========================================
Weighted fusion of keyword retrieval (BM25Okapi) and dense semantic search (FAISS).
Features exact legal citation boosting (e.g. 'Section 3(d)', 'Section 3(p)', 'Section 6')
and multi-field metadata filtering (jurisdiction, domain, authority).
"""

import re
import pickle
import logging
from pathlib import Path
from typing import List, Dict, Any, Optional
import numpy as np
from rank_bm25 import BM25Okapi
from config import Config
from rag.vector_store import vector_store

logger = logging.getLogger(__name__)


class HybridSearchEngine:
    """Combines BM25 keyword matching with FAISS vector search."""

    def __init__(self, bm25_path: Path = Config.BM25_INDEX_PATH):
        self.bm25_path = Path(bm25_path)
        self.bm25 = None
        self.corpus_chunks: List[Dict[str, Any]] = []
        self.tokenized_corpus = []

        # Load or initialize BM25
        self.load_bm25()

    def tokenize(self, text: str) -> List[str]:
        """Tokenize text preserving legal alphanumeric terms and section numbers."""
        # Convert "Section 3(d)" or "3(d)" into lowercase tokens preserving punctuation
        cleaned = re.sub(r"[^\w\s\(\)\.]", " ", text.lower())
        return [t for t in cleaned.split() if len(t) > 1]

    def build_bm25_index(self, chunks: List[Dict[str, Any]]):
        """Build BM25 index from a list of document chunks."""
        self.corpus_chunks = chunks
        self.tokenized_corpus = [self.tokenize(c["text"]) for c in chunks]

        if self.tokenized_corpus:
            self.bm25 = BM25Okapi(self.tokenized_corpus)
            self.save_bm25()
            logger.info("BM25 index built with %d documents.", len(chunks))

    def save_bm25(self):
        """Serialize BM25 index and tokenized corpus."""
        self.bm25_path.parent.mkdir(parents=True, exist_ok=True)
        with open(self.bm25_path, "wb") as f:
            pickle.dump({
                "corpus_chunks": self.corpus_chunks,
                "tokenized_corpus": self.tokenized_corpus
            }, f)

    def load_bm25(self) -> bool:
        """Load BM25 state if persisted."""
        if not self.bm25_path.exists():
            return False
        try:
            with open(self.bm25_path, "rb") as f:
                data = pickle.load(f)
                self.corpus_chunks = data["corpus_chunks"]
                self.tokenized_corpus = data["tokenized_corpus"]
                if self.tokenized_corpus:
                    self.bm25 = BM25Okapi(self.tokenized_corpus)
            logger.info("BM25 index restored with %d documents.", len(self.corpus_chunks))
            return True
        except Exception as e:
            logger.error("Error loading BM25 index: %s", str(e))
            return False

    def search(
        self,
        query: str,
        top_k: int = 15,
        jurisdiction_filter: Optional[str] = None,
        domain_filter: Optional[str] = None
    ) -> List[Dict[str, Any]]:
        """
        Execute hybrid search combining FAISS and BM25 with exact statutory boosting.
        """
        # 1. Semantic search from FAISS
        semantic_results = vector_store.search(query, top_k=top_k * 2)
        semantic_map: Dict[str, float] = {}
        chunk_lookup: Dict[str, Dict[str, Any]] = {}

        for chunk, score in semantic_results:
            cid = chunk["content_hash"]
            semantic_map[cid] = score
            chunk_lookup[cid] = chunk

        # 2. BM25 keyword search
        bm25_map: Dict[str, float] = {}
        query_tokens = self.tokenize(query)

        if self.bm25 and query_tokens and self.corpus_chunks:
            raw_bm25_scores = self.bm25.get_scores(query_tokens)
            max_bm25 = max(raw_bm25_scores) if len(raw_bm25_scores) > 0 and max(raw_bm25_scores) > 0 else 1.0

            for idx, raw_score in enumerate(raw_bm25_scores):
                if raw_score > 0.1:
                    chunk = self.corpus_chunks[idx]
                    cid = chunk["content_hash"]
                    norm_bm25 = min(1.0, float(raw_score / max_bm25))
                    bm25_map[cid] = norm_bm25
                    if cid not in chunk_lookup:
                        chunk_lookup[cid] = chunk

        # 3. Detect exact statutory references (e.g. "section 3(d)", "3(p)", "section 6", "pct")
        statutory_mentions = re.findall(
            r"(?:section|sec\.|article|rule)\s+[0-9a-zA-Z\(\)]+|3\(d\)|3\(p\)|nagoya|tkdl|form iii",
            query,
            re.IGNORECASE
        )

        # 4. Score Fusion & Metadata Filtering
        fused_results = []
        all_candidate_ids = set(semantic_map.keys()).union(set(bm25_map.keys()))

        for cid in all_candidate_ids:
            chunk = chunk_lookup.get(cid)
            if not chunk:
                continue

            # Apply Jurisdiction Filter
            if jurisdiction_filter and jurisdiction_filter.lower() != "all":
                c_juris = chunk.get("jurisdiction", "").lower()
                req_juris = jurisdiction_filter.lower()
                if req_juris not in c_juris and c_juris != "all":
                    continue

            # Apply Domain Filter
            if domain_filter and domain_filter.lower() not in ("all", "general ip"):
                c_domain = chunk.get("domain", "").lower()
                req_domain = domain_filter.lower()
                if req_domain not in c_domain:
                    # Give slight penalty rather than hard discard if relevant
                    domain_penalty = 0.85
                else:
                    domain_penalty = 1.0
            else:
                domain_penalty = 1.0

            s_score = semantic_map.get(cid, 0.0)
            k_score = bm25_map.get(cid, 0.0)

            # Base weighted score
            score = (Config.SEMANTIC_WEIGHT * s_score) + (Config.KEYWORD_WEIGHT * k_score)

            # Exact legal citation boost
            chunk_text_lower = chunk["text"].lower()
            exact_match_found = False
            for mention in statutory_mentions:
                if mention.lower() in chunk_text_lower:
                    exact_match_found = True
                    break

            if exact_match_found:
                score *= Config.LEGAL_KEYWORD_BOOST

            # Authority Level boost
            auth_level = chunk.get("authority_level", "LEVEL_3")
            if auth_level == "LEVEL_1":
                score *= 1.15
            elif auth_level == "LEVEL_2":
                score *= 1.08

            score *= domain_penalty

            candidate = dict(chunk)
            candidate["retrieval_score"] = round(min(1.0, score), 4)
            candidate["semantic_score"] = round(s_score, 4)
            candidate["bm25_score"] = round(k_score, 4)
            fused_results.append(candidate)

        # Sort by fused score descending
        fused_results.sort(key=lambda x: x["retrieval_score"], reverse=True)
        return fused_results[:top_k]


# Global singleton instance
hybrid_searcher = HybridSearchEngine()
