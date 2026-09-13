
"""
=====================================================
IP-SAKTI SAHAYAK
Document Loader
=====================================================

Purpose:
    Load authoritative legal, regulatory and
    Ayurveda-related documents and preserve
    important metadata for RAG and citations.

Pipeline:

    PDF
     ↓
    Page Extraction
     ↓
    Metadata Enrichment
     ↓
    LangChain Documents
     ↓
    Text Cleaning / Chunking
=====================================================
"""

import os

from typing import List

from langchain_core.documents import Document
from langchain_community.document_loaders import PyPDFLoader


# =====================================================
# DOCUMENT LOADER CLASS
# =====================================================

class IPDocumentLoader:
    """
    Loader responsible for reading authoritative
    IP, Ayurveda and regulatory PDF documents.
    """

    def __init__(
        self,
        file_path: str,
        document_name: str = None,
        document_type: str = "Unknown",
        authority: str = "Unknown",
        jurisdiction: str = "India",
        version: str = "Current",
        effective_date: str = "Unknown",
        source_url: str = ""
    ):
        """
        Initialize the document loader.

        Parameters
        ----------
        file_path : str
            Path to the PDF document.

        document_name : str
            Official name of the document.

        document_type : str
            Type of document such as:
            Act, Regulation, Rule, Treaty,
            Official Guidance, etc.

        authority : str
            Organization responsible for the document.

        jurisdiction : str
            Applicable jurisdiction.

        version : str
            Version/status of the document.

        effective_date : str
            Date from which the document is effective.

        source_url : str
            Official URL from which the document
            was obtained.
        """

        self.file_path = file_path
        self.document_name = (
            document_name
            if document_name
            else os.path.basename(file_path)
        )

        self.document_type = document_type
        self.authority = authority
        self.jurisdiction = jurisdiction
        self.version = version
        self.effective_date = effective_date
        self.source_url = source_url


    # =================================================
    # LOAD PDF
    # =================================================

    def load(self) -> List[Document]:
        """
        Load the PDF and return one Document object
        for each page.
        """

        if not os.path.exists(self.file_path):
            raise FileNotFoundError(
                f"Document not found: {self.file_path}"
            )

        if not self.file_path.lower().endswith(".pdf"):
            raise ValueError(
                "Only PDF documents are currently supported."
            )

        print(
            f"Loading document: {self.document_name}"
        )

        loader = PyPDFLoader(self.file_path)

        documents = loader.load()

        print(
            f"Pages extracted: {len(documents)}"
        )

        # Add our own metadata
        documents = self._add_metadata(documents)

        return documents


    # =================================================
    # ADD METADATA
    # =================================================

    def _add_metadata(
        self,
        documents: List[Document]
    ) -> List[Document]:
        """
        Add structured metadata required for
        retrieval, filtering and citations.
        """

        for index, document in enumerate(documents):

            # Page numbers are converted to
            # human-readable numbering.
            page_number = index + 1

            document.metadata.update({

                # Basic document information
                "document_name": self.document_name,

                "document_type": self.document_type,

                # Responsible authority
                "authority": self.authority,

                # Jurisdiction
                "jurisdiction": self.jurisdiction,

                # Version information
                "version": self.version,

                # Effective date
                "effective_date": self.effective_date,

                # Official source
                "source_url": self.source_url,

                # Human-readable page number
                "page": page_number,

                # Original file path
                "file_path": self.file_path
            })

        return documents


    # =================================================
    # GET DOCUMENT INFORMATION
    # =================================================

    def get_document_info(self) -> dict:
        """
        Return structured information about the
        loaded document.
        """

        return {
            "document_name": self.document_name,
            "document_type": self.document_type,
            "authority": self.authority,
            "jurisdiction": self.jurisdiction,
            "version": self.version,
            "effective_date": self.effective_date,
            "source_url": self.source_url,
            "file_path": self.file_path
        }


# =====================================================
# HELPER FUNCTION
# =====================================================

def load_document(
    file_path: str,
    document_name: str = None,
    document_type: str = "Unknown",
    authority: str = "Unknown",
    jurisdiction: str = "India",
    version: str = "Current",
    effective_date: str = "Unknown",
    source_url: str = ""
) -> List[Document]:
    """
    Convenience function for loading a document.

    This allows us to load a PDF without manually
    creating an IPDocumentLoader object.
    """

    loader = IPDocumentLoader(
        file_path=file_path,
        document_name=document_name,
        document_type=document_type,
        authority=authority,
        jurisdiction=jurisdiction,
        version=version,
        effective_date=effective_date,
        source_url=source_url
    )

    return loader.load()


# =====================================================
# TEST FUNCTION
# =====================================================

def preview_document(
    documents: List[Document],
    num_pages: int = 2
):
    """
    Display a small preview of loaded pages.

    Useful during development to verify that
    PDF extraction is working correctly.
    """

    print("\n" + "=" * 60)
    print("DOCUMENT PREVIEW")
    print("=" * 60)

    for document in documents[:num_pages]:

        print(
            f"\nPage: {document.metadata.get('page')}"
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
            f"\n{document.page_content[:1000]}"
        )

        print("\n" + "-" * 60)
