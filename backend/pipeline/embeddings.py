
"""
=====================================================
IP-SAKTI SAHAYAK
Multilingual Embedding Module
=====================================================

Purpose:
    Convert legal, regulatory and Ayurveda-related
    document chunks into multilingual vector
    embeddings for semantic search.

Model:
    BAAI/bge-m3

Why BGE-M3?
    - Supports multilingual text
    - Suitable for semantic retrieval
    - Works well with legal and technical content
    - Supports cross-language retrieval
    - Useful for future Indian-language queries

Pipeline:

    Document Chunks
          ↓
    Text Extraction
          ↓
    BGE-M3 Embedding Model
          ↓
    Numerical Vectors
          ↓
    FAISS Vector Database

Example:

    "Patent protection for an invention"

              ↓

    [0.021, -0.113, 0.482, ...]
=====================================================
"""

from typing import List

from langchain_core.documents import Document
from langchain_huggingface import HuggingFaceEmbeddings

from .config import EMBEDDING_MODEL


# =====================================================
# EMBEDDING CLASS
# =====================================================

class IPEmbeddingModel:
    """
    Wrapper around the multilingual BGE-M3
    embedding model.

    This class provides a clean interface for
    generating embeddings from document chunks
    and user queries.
    """

    def __init__(
        self,
        model_name: str = EMBEDDING_MODEL
    ):
        self.model_name = model_name

        print("=" * 60)
        print("INITIALIZING EMBEDDING MODEL")
        print("=" * 60)

        print(f"Model: {self.model_name}")

        self.embeddings = HuggingFaceEmbeddings(
            model_name=self.model_name,
            model_kwargs={
                "device": "cpu"
            },
            encode_kwargs={
                "normalize_embeddings": True
            }
        )

        print("Embedding model loaded successfully!")


    # =================================================
    # EMBED SINGLE TEXT
    # =================================================

    def embed_text(self, text: str) -> List[float]:
        """
        Convert a single text string into
        a numerical embedding vector.
        """

        if not text or not text.strip():
            raise ValueError(
                "Text cannot be empty."
            )

        return self.embeddings.embed_query(text)


    # =================================================
    # EMBED DOCUMENTS
    # =================================================

    def embed_documents(
        self,
        documents: List[Document]
    ) -> List[List[float]]:
        """
        Generate embeddings for a collection
        of LangChain documents.

        Returns:
            List of numerical vectors.
        """

        if not documents:
            return []

        texts = [
            document.page_content
            for document in documents
        ]

        print(
            f"Generating embeddings for "
            f"{len(texts)} documents..."
        )

        vectors = self.embeddings.embed_documents(
            texts
        )

        print(
            f"Generated {len(vectors)} embeddings."
        )

        return vectors


    # =================================================
    # EMBED QUERY
    # =================================================

    def embed_query(self, query: str) -> List[float]:
        """
        Generate an embedding for a user query.

        The query embedding will later be compared
        against document embeddings during retrieval.
        """

        if not query or not query.strip():
            raise ValueError(
                "Query cannot be empty."
            )

        return self.embeddings.embed_query(
            query
        )


    # =================================================
    # GET EMBEDDING DIMENSION
    # =================================================

    def get_embedding_dimension(self) -> int:
        """
        Determine the dimensionality of the
        embedding vectors produced by the model.
        """

        test_vector = self.embed_text(
            "test"
        )

        return len(test_vector)


# =====================================================
# HELPER FUNCTION
# =====================================================

def create_embedding_model() -> IPEmbeddingModel:
    """
    Create and return the default IP-SAKTI
    multilingual embedding model.
    """

    return IPEmbeddingModel()


# =====================================================
# TEST FUNCTION
# =====================================================

def test_embedding_model():
    """
    Test the embedding model using sample
    legal and multilingual queries.
    """

    print("\n" + "=" * 60)
    print("EMBEDDING MODEL TEST")
    print("=" * 60)

    model = IPEmbeddingModel()

    # -------------------------------------------------
    # English Test
    # -------------------------------------------------

    english_text = (
        "An invention must satisfy the requirements "
        "of novelty, inventive step and industrial "
        "applicability."
    )

    english_vector = model.embed_text(
        english_text
    )

    print("\nEnglish Test:")
    print(
        f"Text: {english_text}"
    )
    print(
        f"Vector dimension: "
        f"{len(english_vector)}"
    )

    # -------------------------------------------------
    # Hindi Test
    # -------------------------------------------------

    hindi_text = (
        "पेटेंट किसी नए आविष्कार के लिए "
        "कानूनी सुरक्षा प्रदान करता है।"
    )

    hindi_vector = model.embed_text(
        hindi_text
    )

    print("\nHindi Test:")
    print(
        f"Text: {hindi_text}"
    )
    print(
        f"Vector dimension: "
        f"{len(hindi_vector)}"
    )

    # -------------------------------------------------
    # Query Test
    # -------------------------------------------------

    query = (
        "What are the requirements for patent protection?"
    )

    query_vector = model.embed_query(
        query
    )

    print("\nQuery Test:")
    print(
        f"Query: {query}"
    )
    print(
        f"Vector dimension: "
        f"{len(query_vector)}"
    )

    # -------------------------------------------------
    # Final Test
    # -------------------------------------------------

    print("\n" + "=" * 60)
    print("EMBEDDING TEST COMPLETED SUCCESSFULLY!")
    print("=" * 60)


# =====================================================
# MAIN
# =====================================================

if __name__ == "__main__":
    test_embedding_model()
