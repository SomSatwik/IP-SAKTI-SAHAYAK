
"""
=====================================================
IP-SAKTI SAHAYAK
Retrieval Testing Module
=====================================================

Purpose:
    Test the FAISS-based retrieval system independently
    from the LLM.

This module verifies that the retrieval layer can find
relevant legal and regulatory evidence.

Pipeline:

    User Query
         ↓
    BGE-M3 Embedding
         ↓
    FAISS Search
         ↓
    Relevant Chunks
         ↓
    Metadata + Evidence

=====================================================
"""

from .retriever import create_retriever
from .vector_store import IPVectorStore
from .embeddings import create_embedding_model

from .config import VECTOR_DB_DIR


# =====================================================
# RETRIEVAL TESTER CLASS
# =====================================================

class RetrievalTester:
    """
    Test and inspect the IP-SAKTI retrieval system.
    """

    def __init__(
        self,
        vector_db_dir: str = VECTOR_DB_DIR
    ):
        """
        Initialize the retrieval tester.
        """

        print("\n" + "=" * 70)
        print("INITIALIZING RETRIEVAL TESTER")
        print("=" * 70)

        # =================================================
        # EMBEDDING MODEL
        # =================================================

        self.embedding_model = create_embedding_model()

        # =================================================
        # LOAD FAISS VECTOR STORE
        # =================================================

        self.vector_store = IPVectorStore(
            embedding_model=self.embedding_model
        )

        self.vector_store.load(
            vector_db_dir
        )

        # =================================================
        # CREATE RETRIEVER
        # =================================================

        self.retriever = create_retriever(
            vector_store=self.vector_store
        )

        print(
            "\nRetrieval tester initialized successfully."
        )


    # =====================================================
    # SEARCH KNOWLEDGE BASE
    # =====================================================

    def search(
        self,
        query: str
    ):
        """
        Search the knowledge base.

        Parameters:
            query:
                User search query.

        Returns:
            Retrieved document chunks.
        """

        print("\n" + "=" * 70)
        print("RETRIEVAL TEST")
        print("=" * 70)

        print(
            f"\nQuery:\n{query}"
        )

        documents = self.retriever.retrieve(
            query
        )

        return documents


    # =====================================================
    # DISPLAY RESULTS
    # =====================================================

    def display_results(
        self,
        documents
    ):
        """
        Display retrieved chunks and their metadata.
        """

        print("\n" + "-" * 70)
        print("RETRIEVED RESULTS")
        print("-" * 70)

        if not documents:

            print(
                "\nNo documents retrieved."
            )

            return


        print(
            f"\nResults retrieved: "
            f"{len(documents)}"
        )


        for index, document in enumerate(
            documents,
            start=1
        ):

            metadata = document.metadata

            print("\n" + "=" * 70)

            print(
                f"[RESULT {index}]"
            )

            print(
                f"\nDocument: "
                f"{metadata.get('document_name', 'Unknown')}"
            )

            print(
                f"Authority: "
                f"{metadata.get('authority', 'Unknown')}"
            )

            print(
                f"Jurisdiction: "
                f"{metadata.get('jurisdiction', 'Unknown')}"
            )

            print(
                f"Document Type: "
                f"{metadata.get('document_type', 'Unknown')}"
            )

            print(
                f"Section: "
                f"{metadata.get('section', 'Unknown')}"
            )

            print(
                f"Page: "
                f"{metadata.get('page', 'Unknown')}"
            )

            print(
                f"Chunk ID: "
                f"{metadata.get('global_chunk_id', 'Unknown')}"
            )

            print("\nContent:\n")

            print(
                document.page_content[:2000]
            )


        print("\n" + "=" * 70)


    # =====================================================
    # RUN RETRIEVAL TEST
    # =====================================================

    def run_test(
        self,
        query: str
    ):
        """
        Run a complete retrieval test.
        """

        documents = self.search(
            query
        )

        self.display_results(
            documents
        )

        return documents


# =====================================================
# TEST FUNCTION
# =====================================================

def test_retrieval():
    """
    Run a basic retrieval test.
    """

    tester = RetrievalTester()

    # =================================================
    # SAMPLE LEGAL QUESTION
    # =================================================

    query = (
        "What is a patent and what rights does a patent "
        "provide to the patent owner in India?"
    )

    # =================================================
    # RUN RETRIEVAL
    # =================================================

    documents = tester.run_test(
        query
    )

    # =================================================
    # FINAL STATUS
    # =================================================

    print("\n" + "=" * 70)
    print("RETRIEVAL TEST COMPLETED")
    print("=" * 70)

    if documents:

        print(
            f"\nSuccessfully retrieved "
            f"{len(documents)} document chunks."
        )

    else:

        print(
            "\nNo relevant document chunks were retrieved."
        )


# =====================================================
# MAIN EXECUTION
# =====================================================

if __name__ == "__main__":

    test_retrieval()
