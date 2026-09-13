
"""
=====================================================
IP-SAKTI SAHAYAK
FAISS Vector Store
=====================================================

Purpose:
    Create, save, load and manage the FAISS vector
    database used by the IP-SAKTI RAG system.

Pipeline:

    Documents
        ↓
    Cleaned Documents
        ↓
    Legal Chunks
        ↓
    BGE-M3 Embeddings
        ↓
    FAISS Vector Store
        ↓
    Semantic Retrieval

FAISS:
    Facebook AI Similarity Search

The vector store preserves the original document
metadata so that retrieved information can later
be converted into reliable source citations.

Important metadata:

    document_name
    document_type
    authority
    jurisdiction
    section
    page
    source_url
    version
    effective_date
    chunk_id
=====================================================
"""

import os
from typing import List, Optional

from langchain_core.documents import Document
from langchain_community.vectorstores import FAISS

from .embeddings import IPEmbeddingModel
from .config import VECTOR_DB_DIR


# =====================================================
# VECTOR STORE CLASS
# =====================================================

class IPVectorStore:
    """
    Manage the FAISS vector database used by
    the IP-SAKTI RAG pipeline.
    """

    def __init__(
        self,
        embedding_model: Optional[IPEmbeddingModel] = None,
        vector_db_path: str = VECTOR_DB_DIR
    ):
        self.vector_db_path = vector_db_path

        # -------------------------------------------------
        # Create embedding model if not supplied
        # -------------------------------------------------

        if embedding_model is None:
            self.embedding_model = IPEmbeddingModel()
        else:
            self.embedding_model = embedding_model

        self.vector_store = None

        # -------------------------------------------------
        # Create directory
        # -------------------------------------------------

        os.makedirs(
            self.vector_db_path,
            exist_ok=True
        )


    # =================================================
    # CREATE VECTOR STORE
    # =================================================

    def create(
        self,
        documents: List[Document]
    ):
        """
        Create a FAISS vector store from document
        chunks.
        """

        if not documents:
            raise ValueError(
                "Cannot create vector store from "
                "an empty document list."
            )

        print("\n" + "=" * 60)
        print("CREATING FAISS VECTOR STORE")
        print("=" * 60)

        print(
            f"Documents / chunks: {len(documents)}"
        )

        self.vector_store = FAISS.from_documents(
            documents,
            self.embedding_model.embeddings
        )

        print(
            "FAISS vector store created successfully!"
        )

        return self.vector_store


    # =================================================
    # ADD DOCUMENTS
    # =================================================

    def add_documents(
        self,
        documents: List[Document]
    ):
        """
        Add additional document chunks to an
        existing FAISS vector store.
        """

        if not documents:
            return

        if self.vector_store is None:
            self.create(documents)
            return

        print(
            f"Adding {len(documents)} chunks "
            "to vector store..."
        )

        self.vector_store.add_documents(
            documents
        )

        print(
            "Documents added successfully!"
        )


    # =================================================
    # SAVE VECTOR STORE
    # =================================================

    def save(
        self,
        path: Optional[str] = None
    ):
        """
        Save the FAISS vector store to disk.

        The index can later be loaded without
        rebuilding all embeddings.
        """

        if self.vector_store is None:
            raise ValueError(
                "Vector store does not exist. "
                "Create it before saving."
            )

        save_path = (
            path
            if path
            else self.vector_db_path
        )

        os.makedirs(
            save_path,
            exist_ok=True
        )

        print("\n" + "=" * 60)
        print("SAVING FAISS VECTOR STORE")
        print("=" * 60)

        self.vector_store.save_local(
            save_path
        )

        print(
            f"Vector store saved to:\n"
            f"{save_path}"
        )


    # =================================================
    # LOAD VECTOR STORE
    # =================================================

    def load(
        self,
        path: Optional[str] = None
    ):
        """
        Load an existing FAISS vector store
        from disk.
        """

        load_path = (
            path
            if path
            else self.vector_db_path
        )

        if not os.path.exists(load_path):
            raise FileNotFoundError(
                f"Vector store directory not found: "
                f"{load_path}"
            )

        print("\n" + "=" * 60)
        print("LOADING FAISS VECTOR STORE")
        print("=" * 60)

        self.vector_store = FAISS.load_local(
            load_path,
            self.embedding_model.embeddings,
            allow_dangerous_deserialization=True
        )

        print(
            "FAISS vector store loaded successfully!"
        )

        return self.vector_store


    # =================================================
    # SIMILARITY SEARCH
    # =================================================

    def similarity_search(
        self,
        query: str,
        k: int = 5
    ) -> List[Document]:
        """
        Search the vector database for documents
        semantically similar to the user's query.
        """

        if self.vector_store is None:
            raise ValueError(
                "Vector store is not loaded or created."
            )

        if not query or not query.strip():
            raise ValueError(
                "Query cannot be empty."
            )

        print(
            f"\nSearching for: {query}"
        )

        results = self.vector_store.similarity_search(
            query,
            k=k
        )

        print(
            f"Retrieved {len(results)} documents."
        )

        return results


    # =================================================
    # SIMILARITY SEARCH WITH SCORE
    # =================================================

    def similarity_search_with_score(
        self,
        query: str,
        k: int = 5
    ):
        """
        Perform similarity search while also
        returning the FAISS distance score.

        Scores can later be used by the RAG
        confidence and safe-abstention system.
        """

        if self.vector_store is None:
            raise ValueError(
                "Vector store is not loaded or created."
            )

        if not query or not query.strip():
            raise ValueError(
                "Query cannot be empty."
            )

        return (
            self.vector_store
            .similarity_search_with_score(
                query,
                k=k
            )
        )


    # =================================================
    # GET VECTOR STORE STATUS
    # =================================================

    def get_status(self) -> dict:
        """
        Return basic information about the
        current vector store.
        """

        return {
            "loaded": self.vector_store is not None,
            "path": self.vector_db_path
        }


# =====================================================
# HELPER FUNCTION
# =====================================================

def create_vector_store(
    documents: List[Document]
) -> IPVectorStore:
    """
    Create a vector store from document chunks
    and return the vector store manager.
    """

    store = IPVectorStore()

    store.create(documents)

    return store


# =====================================================
# PREVIEW SEARCH RESULTS
# =====================================================

def preview_search_results(
    results: List[Document]
):
    """
    Display retrieved documents and their
    important citation metadata.
    """

    print("\n" + "=" * 70)
    print("VECTOR SEARCH RESULTS")
    print("=" * 70)

    for index, document in enumerate(
        results,
        start=1
    ):

        print(
            f"\nResult {index}"
        )

        print(
            f"Document: "
            f"{document.metadata.get('document_name')}"
        )

        print(
            f"Authority: "
            f"{document.metadata.get('authority')}"
        )

        print(
            f"Jurisdiction: "
            f"{document.metadata.get('jurisdiction')}"
        )

        print(
            f"Section: "
            f"{document.metadata.get('section')}"
        )

        print(
            f"Page: "
            f"{document.metadata.get('page')}"
        )

        print(
            f"Source: "
            f"{document.metadata.get('source_url')}"
        )

        print("\nContent:")
        print(
            document.page_content[:1000]
        )

        print(
            "-" * 70
        )


# =====================================================
# TEST FUNCTION
# =====================================================

def test_vector_store():
    """
    Test FAISS vector store using sample
    legal documents.
    """

    print("\n" + "=" * 70)
    print("VECTOR STORE TEST")
    print("=" * 70)

    # -------------------------------------------------
    # Create sample documents
    # -------------------------------------------------

    documents = [
        Document(
            page_content=(
                "An invention must be novel and "
                "involve an inventive step to qualify "
                "for patent protection."
            ),
            metadata={
                "document_name": "Indian Patent Test Document",
                "document_type": "Statute",
                "authority": "Indian Patent Authority",
                "jurisdiction": "India",
                "section": "Section 3",
                "page": 1,
                "source_url": "https://example.gov.in"
            }
        ),

        Document(
            page_content=(
                "A trademark is a mark capable of "
                "distinguishing the goods or services "
                "of one person from those of others."
            ),
            metadata={
                "document_name": "Indian Trademark Test Document",
                "document_type": "Statute",
                "authority": "Indian Trademark Authority",
                "jurisdiction": "India",
                "section": "Section 2",
                "page": 1,
                "source_url": "https://example.gov.in"
            }
        ),

        Document(
            page_content=(
                "Traditional knowledge associated "
                "with biological resources may be "
                "subject to access and benefit-sharing "
                "requirements."
            ),
            metadata={
                "document_name": "Biodiversity Test Document",
                "document_type": "Regulation",
                "authority": "Biodiversity Authority",
                "jurisdiction": "India",
                "section": "Section 7",
                "page": 2,
                "source_url": "https://example.gov.in"
            }
        )
    ]

    # -------------------------------------------------
    # Create vector store
    # -------------------------------------------------

    store = IPVectorStore()

    store.create(documents)

    # -------------------------------------------------
    # Search
    # -------------------------------------------------

    query = (
        "What are the requirements for "
        "getting a patent?"
    )

    results = store.similarity_search(
        query,
        k=2
    )

    # -------------------------------------------------
    # Display results
    # -------------------------------------------------

    preview_search_results(
        results
    )

    print("\n" + "=" * 70)
    print("VECTOR STORE TEST COMPLETED!")
    print("=" * 70)


# =====================================================
# MAIN
# =====================================================

if __name__ == "__main__":
    test_vector_store()
