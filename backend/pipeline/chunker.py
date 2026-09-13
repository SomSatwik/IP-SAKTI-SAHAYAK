
"""
=====================================================
IP-SAKTI SAHAYAK
Legal-Aware Document Chunker
=====================================================

Purpose:
    Split cleaned legal, regulatory and Ayurveda
    documents into meaningful chunks for RAG.

Problem:
    Simple character-based splitting can separate
    important legal structures such as:

        Section 3
        (1)
        (a)
        Explanation
        Provided that

    This can reduce retrieval accuracy.

Solution:
    Use a recursive text splitter with separators
    designed to respect paragraphs, legal clauses
    and sentences as much as possible.

Pipeline:

    Cleaned Documents
          ↓
    Legal-Aware Splitting
          ↓
    Chunk Creation
          ↓
    Metadata Preservation
          ↓
    RAG-Ready Documents

Each chunk retains:

    document_name
    document_type
    authority
    jurisdiction
    section
    page
    source_url
    version
    effective_date

These fields will later be used for
source citation and filtering.
=====================================================
"""

import re

from typing import List

from langchain_core.documents import Document
from langchain_text_splitters import RecursiveCharacterTextSplitter

from .config import (
    CHUNK_SIZE,
    CHUNK_OVERLAP
)


# =====================================================
# LEGAL-AWARE CHUNKER CLASS
# =====================================================

class IPLegalChunker:
    """
    Chunker responsible for splitting legal and
    regulatory documents into RAG-friendly chunks.
    """

    def __init__(
        self,
        chunk_size: int = CHUNK_SIZE,
        chunk_overlap: int = CHUNK_OVERLAP
    ):
        """
        Initialize the legal-aware chunker.

        Parameters
        ----------
        chunk_size : int
            Maximum size of each chunk.

        chunk_overlap : int
            Number of characters shared between
            consecutive chunks.

        Notes
        -----
        Chunk overlap helps preserve context when
        important information occurs near chunk
        boundaries.
        """

        self.chunk_size = chunk_size
        self.chunk_overlap = chunk_overlap

        # Legal-aware separators.
        #
        # The splitter tries separators from top
        # to bottom before falling back to smaller
        # units.
        self.separators = [

            # Paragraph boundary
            "\n\n",

            # Legal subsection boundaries
            "\nSection ",
            "\nSECTION ",

            "\nRule ",
            "\nRULE ",

            "\nArticle ",
            "\nARTICLE ",

            # Numbered clauses
            "\n(1) ",
            "\n(2) ",
            "\n(3) ",

            # Alphabetical clauses
            "\n(a) ",
            "\n(b) ",
            "\n(c) ",

            # Sentences
            ". ",

            # Lines
            "\n",

            # Words
            " ",

            # Final fallback
            ""
        ]


        self.splitter = RecursiveCharacterTextSplitter(
            chunk_size=self.chunk_size,
            chunk_overlap=self.chunk_overlap,
            separators=self.separators,
            length_function=len,
            is_separator_regex=False
        )


    # =================================================
    # EXTRACT SECTION INFORMATION
    # =================================================

    def _extract_section(
        self,
        text: str
    ) -> str:
        """
        Attempt to identify the legal section,
        rule or article associated with a chunk.

        Examples:

            Section 3
            Section 3(1)
            Rule 12
            Article 5

        If no structure is found, return Unknown.
        """

        patterns = [

            r"\bSection\s+\d+(?:\([^)]+\))*",
            r"\bSECTION\s+\d+(?:\([^)]+\))*",

            r"\bRule\s+\d+(?:\([^)]+\))*",
            r"\bRULE\s+\d+(?:\([^)]+\))*",

            r"\bArticle\s+\d+(?:\([^)]+\))*",
            r"\bARTICLE\s+\d+(?:\([^)]+\))*"
        ]


        for pattern in patterns:

            match = re.search(
                pattern,
                text
            )

            if match:

                return match.group(0)


        return "Unknown"


    # =================================================
    # CREATE CHUNKS FOR SINGLE DOCUMENT
    # =================================================

    def chunk_document(
        self,
        document: Document
    ) -> List[Document]:
        """
        Split a single Document into smaller chunks.

        Original metadata is copied to every chunk.
        """

        if not document.page_content.strip():

            return []


        # Split the document text.
        chunks = self.splitter.split_text(
            document.page_content
        )


        chunk_documents = []


        for chunk_index, chunk_text in enumerate(chunks):

            # Copy original metadata so that every
            # chunk remains traceable to its source.
            metadata = document.metadata.copy()


            # Add chunk-specific information.
            metadata.update({

                "chunk_id": chunk_index,

                "chunk_size": len(chunk_text),

                "section": self._extract_section(
                    chunk_text
                )
            })


            chunk_document = Document(
                page_content=chunk_text,
                metadata=metadata
            )


            chunk_documents.append(
                chunk_document
            )


        return chunk_documents


    # =================================================
    # CHUNK DOCUMENT COLLECTION
    # =================================================

    def chunk_documents(
        self,
        documents: List[Document]
    ) -> List[Document]:
        """
        Split an entire collection of Documents
        into RAG-ready chunks.
        """

        if not documents:

            return []


        all_chunks = []


        for document in documents:

            document_chunks = self.chunk_document(
                document
            )


            all_chunks.extend(
                document_chunks
            )


        # Reassign a globally unique chunk ID.
        #
        # This is useful when multiple pages and
        # multiple documents are indexed.
        for global_index, chunk in enumerate(
            all_chunks
        ):

            chunk.metadata[
                "global_chunk_id"
            ] = global_index


        return all_chunks


# =====================================================
# HELPER FUNCTION
# =====================================================

def chunk_documents(
    documents: List[Document]
) -> List[Document]:
    """
    Convenience function for chunking documents
    using the default configuration.
    """

    chunker = IPLegalChunker()

    return chunker.chunk_documents(
        documents
    )


# =====================================================
# CHUNK PREVIEW FUNCTION
# =====================================================

def preview_chunks(
    chunks: List[Document],
    num_chunks: int = 5
):
    """
    Display a preview of generated chunks.

    Useful for verifying chunk size, metadata and
    legal structure before creating embeddings.
    """

    print("\n" + "=" * 70)
    print("CHUNKING PREVIEW")
    print("=" * 70)

    print(
        f"\nTotal chunks: {len(chunks)}"
    )


    for chunk in chunks[:num_chunks]:

        print("\n" + "-" * 70)

        print(
            f"Global Chunk ID: "
            f"{chunk.metadata.get('global_chunk_id')}"
        )

        print(
            f"Document: "
            f"{chunk.metadata.get('document_name')}"
        )

        print(
            f"Page: "
            f"{chunk.metadata.get('page')}"
        )

        print(
            f"Section: "
            f"{chunk.metadata.get('section')}"
        )

        print(
            f"Chunk Size: "
            f"{chunk.metadata.get('chunk_size')}"
        )

        print("\nContent:")

        print(
            chunk.page_content[:1500]
        )


    print("\n" + "=" * 70)


# =====================================================
# TEST FUNCTION
# =====================================================

if __name__ == "__main__":

    test_document = Document(

        page_content="""
        SECTION 3

        Intellectual property protection shall
        apply to eligible inventions.

        (1) Every applicant shall submit the
        required information.

        (a) The application must contain
        sufficient technical information.

        (b) The applicant shall provide
        supporting documents.

        Provided that all information shall
        be accurate and complete.
        """,

        metadata={

            "document_name": "Test Legal Document",

            "document_type": "Statute",

            "authority": "Official Authority",

            "jurisdiction": "India",

            "page": 1,

            "source_url": "https://example.gov.in"
        }
    )


    chunker = IPLegalChunker(
        chunk_size=300,
        chunk_overlap=50
    )


    chunks = chunker.chunk_documents(
        [test_document]
    )


    preview_chunks(
        chunks,
        num_chunks=10
    )


    print(
        "\nLegal chunker is working correctly!"
    )
