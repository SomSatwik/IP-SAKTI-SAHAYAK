
"""
=====================================================
IP-SAKTI SAHAYAK
Legal and Regulatory Retriever
=====================================================

Purpose:
    Retrieve relevant legal, regulatory and Ayurveda
    knowledge from the FAISS vector database.

Pipeline:

    User Query
          ↓
    BGE-M3 Embedding
          ↓
    FAISS Vector Search
          ↓
    Relevant Documents
          ↓
    Evidence Preparation
          ↓
    RAG Pipeline
          ↓
    Groq LLM

Responsibilities:

    1. Retrieve relevant document chunks.
    2. Preserve document metadata.
    3. Prepare evidence for the LLM.
    4. Prepare citation metadata.
    5. Support similarity-score retrieval.
    6. Maintain compatibility with LangChain
       Document objects.

=====================================================
"""

from typing import List, Dict, Any

from langchain_core.documents import Document

from .vector_store import IPVectorStore

from .config import (
    TOP_K,
    MAX_CONTEXT_LENGTH
)


# =====================================================
# RETRIEVER CLASS
# =====================================================

class IPRetriever:
    """
    Retrieve relevant legal and regulatory evidence
    from the IP-SAKTI FAISS vector database.
    """

    def __init__(
        self,
        vector_store: IPVectorStore,
        top_k: int = TOP_K
    ):
        """
        Initialize the retriever.

        Parameters:
            vector_store:
                Initialized FAISS vector store.

            top_k:
                Number of documents to retrieve.
        """

        self.vector_store = vector_store
        self.top_k = top_k


    # =====================================================
    # BASIC DOCUMENT RETRIEVAL
    # =====================================================

    def retrieve(
        self,
        query: str
    ) -> List[Document]:
        """
        Retrieve relevant LangChain Document objects.

        Parameters:
            query:
                User's question.

        Returns:
            List of LangChain Document objects.
        """

        if not query or not query.strip():

            return []


        print(
            f"\nSearching for: {query}"
        )


        # =================================================
        # FAISS SIMILARITY SEARCH
        # =================================================

        documents = self.vector_store.similarity_search(
            query=query,
            k=self.top_k
        )


        print(
            f"Retrieved {len(documents)} documents."
        )


        return documents


    # =====================================================
    # RETRIEVAL WITH SCORES
    # =====================================================

    def retrieve_with_scores(
        self,
        query: str
    ) -> List[Dict[str, Any]]:
        """
        Retrieve documents together with FAISS scores.

        Parameters:
            query:
                User's question.

        Returns:
            List containing document and score.
        """

        if not query or not query.strip():

            return []


        results = (
            self.vector_store
            .similarity_search_with_score(
                query=query,
                k=self.top_k
            )
        )


        formatted_results = []


        for document, score in results:

            formatted_results.append(
                {
                    "document": document,
                    "score": float(score)
                }
            )


        return formatted_results


    # =====================================================
    # FILTER RESULTS
    # =====================================================

    def filter_results(
        self,
        results: List[Dict[str, Any]]
    ) -> List[Dict[str, Any]]:
        """
        Remove invalid retrieval results.

        Parameters:
            results:
                Results containing documents and scores.

        Returns:
            Valid retrieval results.
        """

        if not results:

            return []


        filtered_results = []


        for result in results:

            if not isinstance(
                result,
                dict
            ):

                continue


            document = result.get(
                "document"
            )

            score = result.get(
                "score"
            )


            if document is None:

                continue


            if score is None:

                continue


            if not isinstance(
                document,
                Document
            ):

                continue


            if score >= 0:

                filtered_results.append(
                    result
                )


        return filtered_results


    # =====================================================
    # RETRIEVE EVIDENCE
    # =====================================================

    def retrieve_evidence(
        self,
        query: str
    ) -> List[Dict[str, Any]]:
        """
        Retrieve and filter evidence with scores.

        Parameters:
            query:
                User's question.

        Returns:
            Filtered retrieval results.
        """

        results = self.retrieve_with_scores(
            query
        )


        return self.filter_results(
            results
        )


    # =====================================================
    # NORMALIZE DOCUMENT
    # =====================================================

    def _normalize_document(
        self,
        item: Any
    ) -> Document:
        """
        Convert supported retrieval formats into a
        standard LangChain Document.

        Supported formats:

            1. Document
            2. {"document": Document, "score": ...}

        Parameters:
            item:
                Retrieved item.

        Returns:
            LangChain Document.

        Raises:
            TypeError:
                If the item is not a supported format.
        """

        # =================================================
        # FORMAT 1: DIRECT DOCUMENT
        # =================================================

        if isinstance(
            item,
            Document
        ):

            return item


        # =================================================
        # FORMAT 2: DICTIONARY RESULT
        # =================================================

        if isinstance(
            item,
            dict
        ):

            document = item.get(
                "document"
            )


            if isinstance(
                document,
                Document
            ):

                return document


        # =================================================
        # INVALID FORMAT
        # =================================================

        raise TypeError(
            "Unsupported retrieval result type. "
            "Expected a LangChain Document or a "
            "dictionary containing a Document."
        )


    # =====================================================
    # GET CITATION METADATA
    # =====================================================

    def get_citation_metadata(
        self,
        documents: List[Any]
    ) -> List[Dict[str, Any]]:
        """
        Extract citation metadata from retrieved
        documents.

        Parameters:
            documents:
                Retrieved documents.

        Returns:
            Citation metadata.
        """

        citations = []


        for index, item in enumerate(
            documents,
            start=1
        ):

            document = self._normalize_document(
                item
            )


            metadata = document.metadata


            citation = {
                "source_number": index,

                "document_name": metadata.get(
                    "document_name",
                    "Unknown"
                ),

                "document_type": metadata.get(
                    "document_type",
                    "Unknown"
                ),

                "authority": metadata.get(
                    "authority",
                    "Unknown"
                ),

                "jurisdiction": metadata.get(
                    "jurisdiction",
                    "Unknown"
                ),

                "section": metadata.get(
                    "section",
                    "Unknown"
                ),

                "page": metadata.get(
                    "page",
                    "Unknown"
                ),

                "source_url": metadata.get(
                    "source_url",
                    ""
                ),

                "version": metadata.get(
                    "version",
                    "Unknown"
                ),

                "effective_date": metadata.get(
                    "effective_date",
                    "Unknown"
                )
            }


            citations.append(
                citation
            )


        return citations


    # =====================================================
    # PREPARE CONTEXT
    # =====================================================

    def prepare_context(
        self,
        results: List[Any]
    ) -> str:
        """
        Convert retrieved results into structured
        evidence context for the LLM.

        Parameters:
            results:
                Retrieved Document objects or retrieval
                dictionaries.

        Returns:
            Formatted evidence string.

        Example:

            [SOURCE 1]
            Document: Patents Act
            Authority: Government of India
            Jurisdiction: India
            Section: Section 48
            Page: 20
            URL: ...
            Content:
            ...

        =================================================
        """

        if not results:

            return ""


        context_parts = []

        current_length = 0


        # =================================================
        # PROCESS EACH RESULT
        # =================================================

        for index, item in enumerate(
            results,
            start=1
        ):

            # =================================================
            # NORMALIZE RESULT
            # =================================================

            document = self._normalize_document(
                item
            )


            # =================================================
            # DOCUMENT METADATA
            # =================================================

            metadata = document.metadata


            document_name = metadata.get(
                "document_name",
                "Unknown"
            )

            authority = metadata.get(
                "authority",
                "Unknown"
            )

            jurisdiction = metadata.get(
                "jurisdiction",
                "Unknown"
            )

            section = metadata.get(
                "section",
                "Unknown"
            )

            page = metadata.get(
                "page",
                "Unknown"
            )

            source_url = metadata.get(
                "source_url",
                ""
            )


            # =================================================
            # BUILD SOURCE BLOCK
            # =================================================

            source_block = (
                f"[SOURCE {index}]\n"
                f"Document: {document_name}\n"
                f"Authority: {authority}\n"
                f"Jurisdiction: {jurisdiction}\n"
                f"Section: {section}\n"
                f"Page: {page}\n"
                f"URL: {source_url}\n"
                f"Content:\n"
                f"{document.page_content}\n"
            )


            # =================================================
            # CONTEXT LENGTH CONTROL
            # =================================================

            if (
                current_length
                + len(source_block)
                > MAX_CONTEXT_LENGTH
            ):

                break


            context_parts.append(
                source_block
            )


            current_length += len(
                source_block
            )


        # =================================================
        # RETURN FINAL CONTEXT
        # =================================================

        return "\n".join(
            context_parts
        )


    # =====================================================
    # COMPLETE SEARCH
    # =====================================================

    def search(
        self,
        query: str
    ) -> Dict[str, Any]:
        """
        Perform complete retrieval and evidence
        preparation.

        Parameters:
            query:
                User's question.

        Returns:
            Dictionary containing:

                query
                documents
                context
                citations
        """

        documents = self.retrieve(
            query
        )


        context = self.prepare_context(
            documents
        )


        citations = self.get_citation_metadata(
            documents
        )


        return {
            "query": query,
            "documents": documents,
            "context": context,
            "citations": citations
        }


# =====================================================
# RETRIEVER CREATION HELPER
# =====================================================

def create_retriever(
    vector_store: IPVectorStore,
    top_k: int = TOP_K
) -> IPRetriever:
    """
    Create an IP-SAKTI retriever.

    Parameters:
        vector_store:
            Initialized FAISS vector store.

        top_k:
            Number of documents to retrieve.

    Returns:
        IPRetriever instance.
    """

    return IPRetriever(
        vector_store=vector_store,
        top_k=top_k
    )


# =====================================================
# PREVIEW RETRIEVAL
# =====================================================

def preview_retrieval(
    documents: List[Any],
    num_results: int = 5
):
    """
    Display retrieved documents for debugging.

    Parameters:
        documents:
            Retrieved documents.

        num_results:
            Maximum number of results to display.
    """

    print("\n" + "=" * 70)
    print("RETRIEVAL PREVIEW")
    print("=" * 70)


    if not documents:

        print(
            "\nNo documents retrieved."
        )

        return


    print(
        f"\nTotal retrieved: "
        f"{len(documents)}"
    )


    for index, item in enumerate(
        documents[:num_results],
        start=1
    ):

        document = IPRetriever._normalize_document(
            None,
            item
        )


        metadata = document.metadata


        print(
            "\n" + "-" * 70
        )


        print(
            f"[SOURCE {index}]"
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
            f"Section: "
            f"{metadata.get('section', 'Unknown')}"
        )


        print(
            f"Page: "
            f"{metadata.get('page', 'Unknown')}"
        )


        print(
            "\nContent:"
        )


        print(
            document.page_content[:1500]
        )


    print(
        "\n" + "=" * 70
    )


# =====================================================
# TEST FUNCTION
# =====================================================

def test_retriever():
    """
    Test the complete retrieval layer.
    """

    print("\n" + "=" * 70)
    print("RETRIEVER TEST")
    print("=" * 70)


    # =================================================
    # IMPORT EMBEDDING MODEL
    # =================================================

    from embeddings import create_embedding_model


    # =================================================
    # CREATE EMBEDDING MODEL
    # =================================================

    embedding_model = create_embedding_model()


    # =================================================
    # LOAD VECTOR STORE
    # =================================================

    vector_store = IPVectorStore(
        embedding_model=embedding_model
    )


    vector_store.load(
        "data/index"
    )


    # =================================================
    # CREATE RETRIEVER
    # =================================================

    retriever = create_retriever(
        vector_store=vector_store
    )


    # =================================================
    # TEST QUERY
    # =================================================

    query = (
        "What is a patent and what rights does a "
        "patent owner have in India?"
    )


    # =================================================
    # RETRIEVE DOCUMENTS
    # =================================================

    documents = retriever.retrieve(
        query
    )


    # =================================================
    # DISPLAY RETRIEVAL
    # =================================================

    preview_retrieval(
        documents
    )


    # =================================================
    # PREPARE CONTEXT
    # =================================================

    context = retriever.prepare_context(
        documents
    )


    print(
        "\n" + "=" * 70
    )

    print(
        "PREPARED CONTEXT"
    )

    print(
        "=" * 70
    )

    print(
        context
    )


    # =================================================
    # FINAL STATUS
    # =================================================

    print(
        "\n" + "=" * 70
    )

    print(
        "RETRIEVER TEST COMPLETED"
    )

    print(
        "=" * 70
    )


# =====================================================
# MAIN EXECUTION
# =====================================================

if __name__ == "__main__":

    test_retriever()
