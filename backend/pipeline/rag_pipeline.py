
"""
=====================================================
IP-SAKTI SAHAYAK
Retrieval-Augmented Generation Pipeline
=====================================================

Purpose:
    Connect the retrieval system, evidence verification,
    prompt builder and Groq LLM into one complete
    Retrieval-Augmented Generation pipeline.

Pipeline:

    User Question
          ↓
    Query Processing
          ↓
    FAISS Retrieval
          ↓
    Evidence Preparation
          ↓
    Evidence Verification
          ↓
    Prompt Construction
          ↓
    Groq LLM
          ↓
    Grounded Answer
          ↓
    Source Extraction
          ↓
    Final Response

Important Principles:

    1. Answers must be grounded in retrieved evidence.
    2. Retrieved sources must be preserved.
    3. The system must not invent legal information.
    4. The system should abstain when evidence is insufficient.
    5. Jurisdiction must be respected.
    6. Legal information is not personalized legal advice.

=====================================================
"""

from typing import Dict, List, Any

from .retriever import create_retriever
from .llm import create_llm
from .prompts import create_prompt_builder

from .vector_store import IPVectorStore
from .embeddings import create_embedding_model

from .config import (
    VECTOR_DB_DIR,
    REQUIRE_CITATIONS,
    ENABLE_SAFE_ABSTENTION,
    LEGAL_DISCLAIMER
)


# =====================================================
# RAG PIPELINE CLASS
# =====================================================

class IPRAGPipeline:
    """
    Complete Retrieval-Augmented Generation pipeline
    for IP-SAKTI SAHAYAK.
    """

    def __init__(
        self,
        vector_db_dir: str = VECTOR_DB_DIR
    ):
        """
        Initialize the complete RAG pipeline.

        Parameters:
            vector_db_dir:
                Directory containing the saved FAISS
                vector database.
        """

        print("\n" + "=" * 70)
        print("INITIALIZING IP-SAKTI RAG PIPELINE")
        print("=" * 70)


        # =================================================
        # EMBEDDING MODEL
        # =================================================

        print("\nLoading embedding model...")

        self.embedding_model = create_embedding_model()


        # =================================================
        # VECTOR STORE
        # =================================================

        print("\nLoading vector database...")

        self.vector_store = IPVectorStore(
            embedding_model=self.embedding_model
        )

        self.vector_store.load(
            vector_db_dir
        )


        # =================================================
        # RETRIEVER
        # =================================================

        print("\nCreating retriever...")

        self.retriever = create_retriever(
            vector_store=self.vector_store
        )


        # =================================================
        # LLM
        # =================================================

        print("\nLoading Groq LLM...")

        self.llm = create_llm()


        # =================================================
        # PROMPT BUILDER
        # =================================================

        print("\nCreating prompt builder...")

        self.prompt_builder = create_prompt_builder()


        # =================================================
        # CONFIGURATION
        # =================================================

        self.require_citations = REQUIRE_CITATIONS
        self.enable_safe_abstention = ENABLE_SAFE_ABSTENTION


        print("\n" + "=" * 70)
        print("RAG PIPELINE INITIALIZED SUCCESSFULLY")
        print("=" * 70)


    # =====================================================
    # PROCESS USER QUERY
    # =====================================================

    def process_query(
        self,
        question: str
    ) -> str:
        """
        Clean and normalize the user's question.

        Parameters:
            question:
                Original user question.

        Returns:
            Processed question.
        """

        if not question:

            return ""


        question = question.strip()


        return question


    # =====================================================
    # RETRIEVE DOCUMENTS
    # =====================================================

    def retrieve_documents(
        self,
        question: str
    ) -> List[Any]:
        """
        Retrieve relevant evidence from the FAISS
        vector database.

        Parameters:
            question:
                User's legal or regulatory question.

        Returns:
            List of retrieved documents.
        """

        print("\n" + "=" * 70)
        print("RETRIEVING EVIDENCE")
        print("=" * 70)


        print(
            f"\nQuestion:\n{question}"
        )


        # =================================================
        # RETRIEVE DOCUMENTS
        # =================================================

        documents = self.retriever.retrieve(
            question
        )


        print(
            f"\nDocuments retrieved: "
            f"{len(documents)}"
        )


        return documents


    # =====================================================
    # PREPARE EVIDENCE
    # =====================================================

    def prepare_evidence(
        self,
        documents: List[Any]
    ) -> str:
        """
        Convert retrieved documents into structured
        evidence for the LLM.

        Parameters:
            documents:
                Retrieved document chunks.

        Returns:
            Formatted evidence context.
        """

        if not documents:

            return ""


        print(
            "\nPreparing evidence context..."
        )


        context = self.retriever.prepare_context(
            documents
        )


        return context


    # =====================================================
    # VERIFY EVIDENCE
    # =====================================================

    def verify_evidence(
        self,
        question: str,
        evidence: str
    ) -> str:
        """
        Verify whether retrieved evidence is sufficient
        to answer the question.

        Parameters:
            question:
                User question.

            evidence:
                Retrieved evidence.

        Returns:
            SUFFICIENT or INSUFFICIENT.
        """

        if not evidence.strip():

            return "INSUFFICIENT"


        print(
            "\nVerifying retrieved evidence..."
        )


        result = self.llm.check_evidence(
            question,
            evidence
        )


        print(
            f"Evidence status: {result}"
        )


        return result


    # =====================================================
    # GENERATE ANSWER
    # =====================================================

    def generate_answer(
        self,
        question: str,
        evidence: str
    ) -> str:
        """
        Generate a grounded answer using the LLM.

        Parameters:
            question:
                User's question.

            evidence:
                Retrieved evidence.

        Returns:
            Generated answer.
        """

        print("\n" + "=" * 70)
        print("GENERATING GROUNDED ANSWER")
        print("=" * 70)


        # =================================================
        # BUILD RAG PROMPT
        # =================================================

        prompt = self.prompt_builder.build_rag_prompt(
            question=question,
            context=evidence
        )


        # =================================================
        # CALL LLM
        # =================================================
        #
        # IPLLM.generate() expects:
        #
        #     generate(prompt, context)
        #
        # Therefore both values are passed explicitly.
        # =================================================

        answer = self.llm.generate(
            prompt,
            evidence
        )


        return answer


    # =====================================================
    # GENERATE ABSTENTION RESPONSE
    # =====================================================

    def generate_abstention_response(
        self,
        question: str
    ) -> str:
        """
        Generate a safe response when sufficient evidence
        is not available.

        Parameters:
            question:
                User's question.

        Returns:
            Safe abstention response.
        """

        print("\n" + "=" * 70)
        print("SAFE ABSTENTION")
        print("=" * 70)


        try:

            prompt = (
                self.prompt_builder
                .build_abstention_prompt(
                    question=question
                )
            )


            answer = self.llm.generate(
                prompt,
                ""
            )


            return answer


        except Exception:

            return (
                "I could not find sufficient authoritative "
                "evidence in the available knowledge base "
                "to answer this question reliably."
            )


    # =====================================================
    # EXTRACT SOURCES
    # =====================================================

    def extract_sources(
        self,
        documents: List[Any]
    ) -> List[Dict[str, Any]]:
        """
        Extract citation metadata from retrieved documents.

        Parameters:
            documents:
                Retrieved document chunks.

        Returns:
            List of citation metadata dictionaries.
        """

        sources = []


        for index, document in enumerate(
            documents,
            start=1
        ):

            metadata = document.metadata


            source = {
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


            sources.append(
                source
            )


        return sources


    # =====================================================
    # RUN COMPLETE PIPELINE
    # =====================================================

    def run(
        self,
        question: str
    ) -> Dict[str, Any]:
        """
        Run the complete RAG pipeline.

        Parameters:
            question:
                User's legal or regulatory question.

        Returns:
            Dictionary containing:

                question
                answer
                sources
                evidence_status
                retrieved_documents
                disclaimer
        """

        print("\n" + "=" * 70)
        print("IP-SAKTI SAHAYAK")
        print("RAG QUERY")
        print("=" * 70)


        # =================================================
        # STEP 1: PROCESS QUERY
        # =================================================

        processed_question = self.process_query(
            question
        )


        if not processed_question:

            return {
                "question": question,
                "answer": "Please enter a valid question.",
                "sources": [],
                "evidence_status": "INSUFFICIENT",
                "retrieved_documents": 0,
                "disclaimer": LEGAL_DISCLAIMER
            }


        # =================================================
        # STEP 2: RETRIEVE DOCUMENTS
        # =================================================

        documents = self.retrieve_documents(
            processed_question
        )


        # =================================================
        # STEP 3: PREPARE EVIDENCE
        # =================================================

        evidence = self.prepare_evidence(
            documents
        )


        # =================================================
        # STEP 4: VERIFY EVIDENCE
        # =================================================

        evidence_status = self.verify_evidence(
            processed_question,
            evidence
        )


        # =================================================
        # STEP 5: SAFE ABSTENTION
        # =================================================

        if (
            self.enable_safe_abstention
            and evidence_status != "SUFFICIENT"
        ):

            answer = self.generate_abstention_response(
                processed_question
            )


            return {
                "question": processed_question,

                "answer": answer,

                "sources": self.extract_sources(
                    documents
                ),

                "evidence_status": "INSUFFICIENT",

                "retrieved_documents": len(
                    documents
                ),

                "disclaimer": LEGAL_DISCLAIMER
            }


        # =================================================
        # STEP 6: GENERATE GROUNDED ANSWER
        # =================================================

        answer = self.generate_answer(
            processed_question,
            evidence
        )


        # =================================================
        # STEP 7: EXTRACT SOURCES
        # =================================================

        sources = self.extract_sources(
            documents
        )


        # =================================================
        # STEP 8: CREATE FINAL RESULT
        # =================================================

        result = {
            "question": processed_question,

            "answer": answer,

            "sources": sources,

            "evidence_status": evidence_status,

            "retrieved_documents": len(
                documents
            ),

            "disclaimer": LEGAL_DISCLAIMER
        }


        print("\n" + "=" * 70)
        print("RAG PIPELINE COMPLETED")
        print("=" * 70)


        return result


    # =====================================================
    # PREVIEW RESULT
    # =====================================================

    def preview_result(
        self,
        result: Dict[str, Any]
    ):
        """
        Display the final RAG response.

        Parameters:
            result:
                Result returned by run().
        """

        print("\n" + "=" * 70)
        print("IP-SAKTI SAHAYAK RESPONSE")
        print("=" * 70)


        # =================================================
        # QUESTION
        # =================================================

        print(
            f"\nQuestion:\n"
            f"{result.get('question', '')}"
        )


        print(
            "\n" + "-" * 70
        )


        # =================================================
        # ANSWER
        # =================================================

        print(
            "\nAnswer:\n"
        )


        print(
            result.get(
                "answer",
                "No answer generated."
            )
        )


        # =================================================
        # EVIDENCE STATUS
        # =================================================

        print(
            "\n" + "-" * 70
        )


        print(
            f"\nEvidence Status: "
            f"{result.get('evidence_status', 'Unknown')}"
        )


        print(
            f"Retrieved Documents: "
            f"{result.get('retrieved_documents', 0)}"
        )


        # =================================================
        # SOURCES
        # =================================================

        sources = result.get(
            "sources",
            []
        )


        print(
            "\nSources:"
        )


        if not sources:

            print(
                "No sources available."
            )


        else:

            for source in sources:

                print(
                    f"\n[Source "
                    f"{source.get('source_number')}]"
                )


                print(
                    f"Document: "
                    f"{source.get('document_name')}"
                )


                print(
                    f"Authority: "
                    f"{source.get('authority')}"
                )


                print(
                    f"Jurisdiction: "
                    f"{source.get('jurisdiction')}"
                )


                print(
                    f"Section: "
                    f"{source.get('section')}"
                )


                print(
                    f"Page: "
                    f"{source.get('page')}"
                )


                if source.get(
                    "source_url"
                ):

                    print(
                        f"URL: "
                        f"{source.get('source_url')}"
                    )


        # =================================================
        # DISCLAIMER
        # =================================================

        print(
            "\n" + "-" * 70
        )


        print(
            "\nDisclaimer:"
        )


        print(
            result.get(
                "disclaimer",
                LEGAL_DISCLAIMER
            )
        )


        print(
            "\n" + "=" * 70
        )


# =====================================================
# PIPELINE CREATION HELPER
# =====================================================

def create_rag_pipeline(
    vector_db_dir: str = VECTOR_DB_DIR
) -> IPRAGPipeline:
    """
    Create an IP-SAKTI RAG pipeline.

    Parameters:
        vector_db_dir:
            FAISS vector database directory.

    Returns:
        Initialized IPRAGPipeline instance.
    """

    return IPRAGPipeline(
        vector_db_dir=vector_db_dir
    )


# =====================================================
# TEST FUNCTION
# =====================================================

def test_rag_pipeline():
    """
    Run a complete end-to-end RAG test.
    """

    print("\n" + "=" * 70)
    print("RAG PIPELINE TEST")
    print("=" * 70)


    # =================================================
    # CREATE PIPELINE
    # =================================================

    pipeline = create_rag_pipeline()


    # =================================================
    # TEST QUESTION
    # =================================================

    question = (
        "What is a patent and what rights does a patent "
        "provide to the patent owner in India?"
    )


    # =================================================
    # RUN PIPELINE
    # =================================================

    result = pipeline.run(
        question
    )


    # =================================================
    # DISPLAY RESULT
    # =================================================

    pipeline.preview_result(
        result
    )


    print("\n" + "=" * 70)
    print("RAG PIPELINE TEST COMPLETED")
    print("=" * 70)


# =====================================================
# MAIN EXECUTION
# =====================================================

if __name__ == "__main__":

    test_rag_pipeline()
