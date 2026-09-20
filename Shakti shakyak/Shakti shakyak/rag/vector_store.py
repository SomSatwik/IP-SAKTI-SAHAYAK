"""
IP-SAKTI SAHAYAK
FAISS Vector Store Manager
==========================
Persistent FAISS vector store with normalized inner product (cosine similarity) index.
Supports index creation, document addition, search, serialization, and index rebuilds.
"""

import pickle
import logging
from pathlib import Path
from typing import List, Dict, Any, Tuple
import numpy as np
from config import Config
from rag.embeddings import embedding_service

logger = logging.getLogger(__name__)

# Attempt to import faiss
try:
    import faiss
    FAISS_AVAILABLE = True
except ImportError:
    FAISS_AVAILABLE = False
    logger.warning("faiss-cpu not detected. Using high-performance NumPy cosine similarity index.")


class FaissVectorStore:
    """Manages vector indexing and nearest-neighbor search for legal chunks."""

    def __init__(
        self,
        index_path: Path = Config.VECTOR_DB_PATH,
        chunks_path: Path = Config.CHUNKS_PATH,
        dimension: int = Config.EMBEDDING_DIMENSION
    ):
        self.index_path = Path(index_path)
        self.chunks_path = Path(chunks_path)
        self.dimension = dimension
        self.index = None
        self.chunks: List[Dict[str, Any]] = []
        self._vectors: List[np.ndarray] = []  # In-memory vectors for numpy fallback

        # Load existing index if present
        self.load_index()

    def create_index(self):
        """Create a new empty Inner Product (Cosine) index."""
        if FAISS_AVAILABLE:
            self.index = faiss.IndexFlatIP(self.dimension)
        else:
            self.index = "NUMPY_INDEX"
        self.chunks = []
        self._vectors = []
        logger.info("Initialized fresh vector index with dimension %d", self.dimension)

    def add_chunks(self, chunks: List[Dict[str, Any]]) -> int:
        """Embed text chunks and add them to the FAISS index with metadata."""
        if not chunks:
            return 0

        if self.index is None:
            self.create_index()

        texts = [c["text"] for c in chunks]
        embeddings = embedding_service.embed_batch(texts)

        if FAISS_AVAILABLE and isinstance(self.index, faiss.Index):
            self.index.add(embeddings)
        else:
            for vec in embeddings:
                self._vectors.append(vec)

        self.chunks.extend(chunks)
        self.save_index()
        logger.info("Successfully added %d chunks to vector store (Total: %d)", len(chunks), len(self.chunks))
        return len(chunks)

    def search(self, query: str, top_k: int = 15) -> List[Tuple[Dict[str, Any], float]]:
        """Search the index for the most semantically relevant chunks."""
        if not self.chunks or self.index is None:
            logger.warning("Vector search requested on empty index.")
            return []

        query_vec = embedding_service.embed_text(query).reshape(1, -1)
        results = []

        if FAISS_AVAILABLE and isinstance(self.index, faiss.Index) and self.index.ntotal > 0:
            k = min(top_k, self.index.ntotal)
            scores, indices = self.index.search(query_vec, k)

            for score, idx in zip(scores[0], indices[0]):
                if idx != -1 and idx < len(self.chunks):
                    # Ensure cosine score is clamped [0, 1]
                    norm_score = max(0.0, min(1.0, float(score)))
                    results.append((self.chunks[idx], norm_score))
        elif self._vectors:
            # NumPy cosine similarity fallback
            vectors_matrix = np.array(self._vectors, dtype=np.float32)
            # Dot product since vectors are unit normalized
            scores = np.dot(vectors_matrix, query_vec.T).flatten()
            top_indices = np.argsort(scores)[::-1][:top_k]

            for idx in top_indices:
                norm_score = max(0.0, min(1.0, float(scores[idx])))
                results.append((self.chunks[idx], norm_score))

        return results

    def save_index(self):
        """Serialize FAISS index and chunk metadata to disk."""
        self.index_path.parent.mkdir(parents=True, exist_ok=True)

        # Save metadata
        with open(self.chunks_path, "wb") as f:
            pickle.dump(self.chunks, f)

        # Save FAISS index
        if FAISS_AVAILABLE and isinstance(self.index, faiss.Index):
            faiss.write_index(self.index, str(self.index_path))
        else:
            # Save vectors for fallback
            np_path = self.index_path.with_suffix(".npy")
            if self._vectors:
                np.save(str(np_path), np.array(self._vectors))

        logger.info("Saved vector index and %d chunks to %s", len(self.chunks), self.index_path)

    def load_index(self) -> bool:
        """Load FAISS index and chunk metadata if available."""
        if not self.index_path.exists() or not self.chunks_path.exists():
            logger.info("No pre-existing vector store found at %s. Awaiting document ingestion.", self.index_path)
            self.create_index()
            return False

        try:
            with open(self.chunks_path, "rb") as f:
                self.chunks = pickle.load(f)

            if FAISS_AVAILABLE:
                self.index = faiss.read_index(str(self.index_path))
            else:
                np_path = self.index_path.with_suffix(".npy")
                if np_path.exists():
                    self._vectors = list(np.load(str(np_path)))
                self.index = "NUMPY_INDEX"

            logger.info("Successfully loaded vector store with %d chunks.", len(self.chunks))
            return True
        except Exception as e:
            logger.error("Error loading FAISS index: %s. Re-creating fresh index.", str(e))
            self.create_index()
            return False

    def count(self) -> int:
        """Total chunks currently indexed."""
        return len(self.chunks)


# Global singleton instance
vector_store = FaissVectorStore()
