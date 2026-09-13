
"""
=====================================================
IP-SAKTI SAHAYAK
Document Ingestion Pipeline
=====================================================

Purpose:
    Process official IP, Ayurveda, legal and regulatory
    documents and convert them into a searchable FAISS
    knowledge base.

Pipeline:

    Raw Documents
          ↓
    Document Loading
          ↓
    Text Cleaning
          ↓
    Legal-Aware Chunking
          ↓
    Embedding Generation
          ↓
    FAISS Vector Store
          ↓
    Persistent Knowledge Base

=====================================================
"""

import os
from typing import List

from langchain_core.documents import Document

from .document_loader import IPDocumentLoader
from .text_cleaner import clean_documents
from .chunker import IPLegalChunker
from .embeddings import create_embedding_model
from .vector_store import IPVectorStore

from .config import (
    RAW_DATA_DIR,
    VECTOR_DB_DIR,
    SUPPORTED_DOCUMENT_FORMATS
)


# =====================================================
# DOCUMENT INGESTION CLASS
# =====================================================

class IPDocumentIngestion:
    """
    Complete document ingestion pipeline for
    IP-SAKTI SAHAYAK.
    """

    def __init__(
        self,
        raw_data_dir: str = RAW_DATA_DIR,
        vector_db_dir: str = VECTOR_DB_DIR
    ):
        """
        Initialize the ingestion pipeline.
        """

        print("\n" + "=" * 70)
        print("INITIALIZING DOCUMENT INGESTION PIPELINE")
        print("=" * 70)

        self.raw_data_dir = raw_data_dir
        self.vector_db_dir = vector_db_dir

        # -------------------------------------------------
        # Initialize document processing components
        # -------------------------------------------------

        self.chunker = IPLegalChunker()

        self.embedding_model = create_embedding_model()

        self.vector_store = None

        print("\nIngestion pipeline initialized successfully.")


    # =====================================================
    # FIND DOCUMENTS
    # =====================================================

    def find_documents(self) -> List[str]:
        """
        Find supported documents inside the raw data
        directory.

        Returns:
            List of document file paths.
        """

        if not os.path.exists(self.raw_data_dir):

            print(
                f"\nRaw data directory does not exist: "
                f"{self.raw_data_dir}"
            )

            return []

        document_paths = []

        for filename in sorted(
            os.listdir(self.raw_data_dir)
        ):

            file_path = os.path.join(
                self.raw_data_dir,
                filename
            )

            # Ignore directories
            if not os.path.isfile(file_path):
                continue

            # Check file extension
            extension = os.path.splitext(
                filename
            )[1].lower()

            if extension in SUPPORTED_DOCUMENT_FORMATS:

                document_paths.append(
                    file_path
                )

        return document_paths


    # =====================================================
    # LOAD DOCUMENTS
    # =====================================================

    def load_documents(
        self,
        document_paths: List[str]
    ) -> List[Document]:
        """
        Load all documents.

        A new IPDocumentLoader is created for each
        individual file because the loader requires
        file_path during initialization.
        """

        all_documents = []

        print("\n" + "-" * 70)
        print("DOCUMENT LOADING")
        print("-" * 70)

        for file_path in document_paths:

            print(
                f"\nLoading: "
                f"{os.path.basename(file_path)}"
            )

            try:

                # -----------------------------------------
                # Create loader for current file
                # -----------------------------------------

                loader = IPDocumentLoader(
                    file_path
                )

                # -----------------------------------------
                # Load pages
                # -----------------------------------------

                documents = loader.load()

                all_documents.extend(
                    documents
                )

                print(
                    f"Pages loaded: "
                    f"{len(documents)}"
                )

            except Exception as error:

                print(
                    f"Failed to load document: "
                    f"{error}"
                )

        return all_documents


    # =====================================================
    # CLEAN DOCUMENTS
    # =====================================================

    def clean_documents(
        self,
        documents: List[Document]
    ) -> List[Document]:
        """
        Clean document text while preserving metadata.
        """

        print("\n" + "-" * 70)
        print("TEXT CLEANING")
        print("-" * 70)

        cleaned_documents = clean_documents(
            documents
        )

        print(
            f"\nDocuments cleaned: "
            f"{len(cleaned_documents)}"
        )

        return cleaned_documents


    # =====================================================
    # CHUNK DOCUMENTS
    # =====================================================

    def chunk_documents(
        self,
        documents: List[Document]
    ) -> List[Document]:
        """
        Split cleaned documents into legal-aware chunks.
        """

        print("\n" + "-" * 70)
        print("LEGAL-AWARE CHUNKING")
        print("-" * 70)

        chunks = self.chunker.chunk_documents(
            documents
        )

        print(
            f"\nChunks created: "
            f"{len(chunks)}"
        )

        return chunks


    # =====================================================
    # CREATE VECTOR STORE
    # =====================================================

    def create_vector_store(
        self,
        chunks: List[Document]
    ):
        """
        Generate embeddings and create FAISS vector store.
        """

        print("\n" + "-" * 70)
        print("VECTOR STORE CREATION")
        print("-" * 70)

        if not chunks:

            raise ValueError(
                "No document chunks available for indexing."
            )

        # ---------------------------------------------
        # Initialize vector store
        # ---------------------------------------------

        self.vector_store = IPVectorStore(
            embedding_model=self.embedding_model
        )

        print("\nGenerating embeddings...")

        self.vector_store.create(
            chunks
        )

        print("\nVector store created successfully.")

        return self.vector_store


    # =====================================================
    # SAVE VECTOR STORE
    # =====================================================

    def save_vector_store(self):
        """
        Save FAISS vector store to disk.
        """

        if self.vector_store is None:

            raise ValueError(
                "Vector store has not been created yet."
            )

        print("\n" + "-" * 70)
        print("SAVING VECTOR STORE")
        print("-" * 70)

        self.vector_store.save(
            self.vector_db_dir
        )

        print(
            f"\nVector store saved to: "
            f"{self.vector_db_dir}"
        )


    # =====================================================
    # COMPLETE INGESTION PIPELINE
    # =====================================================

    def run(self):
        """
        Execute the complete document ingestion process.
        """

        print("\n" + "=" * 70)
        print("STARTING IP-SAKTI SAHAYAK INGESTION")
        print("=" * 70)


        # ---------------------------------------------
        # Step 1: Find documents
        # ---------------------------------------------

        document_paths = self.find_documents()

        if not document_paths:

            print(
                "\nNo supported documents found."
            )

            print(
                f"\nPlease place PDF or TXT files inside:"
                f"\n{self.raw_data_dir}"
            )

            return {
                "success": False,
                "documents": 0,
                "pages": 0,
                "chunks": 0
            }


        print(
            f"\nDocuments found: "
            f"{len(document_paths)}"
        )


        # ---------------------------------------------
        # Step 2: Load documents
        # ---------------------------------------------

        documents = self.load_documents(
            document_paths
        )


        if not documents:

            print(
                "\nNo documents could be loaded."
            )

            return {
                "success": False,
                "documents": len(document_paths),
                "pages": 0,
                "chunks": 0
            }


        # ---------------------------------------------
        # Step 3: Clean documents
        # ---------------------------------------------

        cleaned_documents = self.clean_documents(
            documents
        )


        # ---------------------------------------------
        # Step 4: Create chunks
        # ---------------------------------------------

        chunks = self.chunk_documents(
            cleaned_documents
        )


        if not chunks:

            print(
                "\nNo chunks were generated."
            )

            return {
                "success": False,
                "documents": len(document_paths),
                "pages": len(documents),
                "chunks": 0
            }


        # ---------------------------------------------
        # Step 5: Create vector store
        # ---------------------------------------------

        self.create_vector_store(
            chunks
        )


        # ---------------------------------------------
        # Step 6: Save vector store
        # ---------------------------------------------

        self.save_vector_store()


        # ---------------------------------------------
        # Final statistics
        # ---------------------------------------------

        statistics = {
            "success": True,
            "documents": len(document_paths),
            "pages": len(documents),
            "cleaned_pages": len(cleaned_documents),
            "chunks": len(chunks),
            "vector_store": self.vector_db_dir
        }


        print("\n" + "=" * 70)
        print("INGESTION COMPLETED SUCCESSFULLY")
        print("=" * 70)

        print(
            f"\nDocuments: "
            f"{statistics['documents']}"
        )

        print(
            f"Pages: "
            f"{statistics['pages']}"
        )

        print(
            f"Chunks: "
            f"{statistics['chunks']}"
        )

        print(
            f"Vector Store: "
            f"{statistics['vector_store']}"
        )

        return statistics


# =====================================================
# PIPELINE FACTORY
# =====================================================

def create_ingestion_pipeline():
    """
    Create and return the document ingestion pipeline.
    """

    return IPDocumentIngestion()


# =====================================================
# INGESTION TEST
# =====================================================

def test_ingestion():
    """
    Test the complete document ingestion pipeline.
    """

    print("\n" + "=" * 70)
    print("TESTING IP-SAKTI SAHAYAK INGESTION")
    print("=" * 70)

    pipeline = create_ingestion_pipeline()

    result = pipeline.run()

    print("\n" + "=" * 70)
    print("INGESTION TEST RESULT")
    print("=" * 70)

    print(
        f"\nSuccess: "
        f"{result.get('success')}"
    )

    print(
        f"Documents: "
        f"{result.get('documents')}"
    )

    print(
        f"Pages: "
        f"{result.get('pages')}"
    )

    print(
        f"Chunks: "
        f"{result.get('chunks')}"
    )


# =====================================================
# MAIN EXECUTION
# =====================================================

if __name__ == "__main__":

    test_ingestion()
