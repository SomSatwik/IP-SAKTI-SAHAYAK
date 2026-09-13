
"""
=====================================================
IP-SAKTI SAHAYAK
Large Language Model Module
=====================================================

Purpose:
    Connect the IP-SAKTI RAG pipeline with the
    Groq Large Language Model.

Responsibilities:

    - Initialize the Groq LLM
    - Send grounded prompts to the model
    - Generate answers from retrieved evidence
    - Support query rewriting
    - Support evidence sufficiency checks
    - Keep LLM logic separate from retrieval

Pipeline:

    Retrieved Evidence
          ↓
    RAG Prompt
          ↓
    Groq LLM
          ↓
    Generated Response
          ↓
    Citation Verification

IMPORTANT:

    The LLM should not independently provide legal
    advice or invent legal information.

    The retrieved evidence and system prompt are
    responsible for grounding the response.
=====================================================
"""

from typing import Optional

from langchain_groq import ChatGroq

from .config import (
    GROQ_API_KEY,
    MODEL_NAME,
    TEMPERATURE,
    MAX_TOKENS
)

from .prompts import (
    SYSTEM_PROMPT,
    IPPromptBuilder
)


# =====================================================
# LLM CLASS
# =====================================================

class IPLLM:
    """
    Wrapper around the Groq Chat LLM used by
    IP-SAKTI Sahayak.
    """

    def __init__(
        self,
        api_key: Optional[str] = None,
        model_name: str = MODEL_NAME,
        temperature: float = TEMPERATURE,
        max_tokens: int = MAX_TOKENS
    ):
        """
        Initialize the Groq LLM.
        """

        self.api_key = (
            api_key
            if api_key
            else GROQ_API_KEY
        )

        self.model_name = model_name
        self.temperature = temperature
        self.max_tokens = max_tokens

        # -------------------------------------------------
        # Validate API key
        # -------------------------------------------------

        if not self.api_key:
            raise ValueError(
                "Groq API key is not configured. "
                "Set GROQ_API_KEY before initializing "
                "the LLM."
            )

        # -------------------------------------------------
        # Initialize Groq
        # -------------------------------------------------

        print("\n" + "=" * 60)
        print("INITIALIZING GROQ LLM")
        print("=" * 60)

        print(
            f"Model: {self.model_name}"
        )

        self.llm = ChatGroq(
            api_key=self.api_key,
            model=self.model_name,
            temperature=self.temperature,
            max_tokens=self.max_tokens
        )

        print(
            "Groq LLM initialized successfully!"
        )

        # -------------------------------------------------
        # Prompt builder
        # -------------------------------------------------

        self.prompt_builder = IPPromptBuilder()


    # =================================================
    # GENERATE RESPONSE
    # =================================================

    def generate(
        self,
        question: str,
        context: str
    ) -> str:
        """
        Generate a grounded response using the
        supplied retrieved evidence.
        """

        if not question or not question.strip():
            raise ValueError(
                "Question cannot be empty."
            )

        if not context or not context.strip():
            raise ValueError(
                "Context cannot be empty."
            )

        # -------------------------------------------------
        # Build RAG prompt
        # -------------------------------------------------

        rag_prompt = (
            self.prompt_builder
            .build_rag_prompt(
                question=question,
                context=context
            )
        )

        # -------------------------------------------------
        # Create message structure
        # -------------------------------------------------

        messages = [
            (
                "system",
                SYSTEM_PROMPT
            ),
            (
                "human",
                rag_prompt
            )
        ]

        # -------------------------------------------------
        # Call LLM
        # -------------------------------------------------

        response = self.llm.invoke(
            messages
        )

        return response.content


    # =================================================
    # QUERY REWRITE
    # =================================================

    def rewrite_query(
        self,
        question: str
    ) -> str:
        """
        Rewrite a user question into a concise
        retrieval-friendly search query.
        """

        if not question or not question.strip():
            raise ValueError(
                "Question cannot be empty."
            )

        prompt = (
            self.prompt_builder
            .build_query_rewrite_prompt(
                question
            )
        )

        messages = [
            (
                "system",
                (
                    "You are a search query optimization "
                    "assistant for an Intellectual Property "
                    "and regulatory knowledge base."
                )
            ),
            (
                "human",
                prompt
            )
        ]

        response = self.llm.invoke(
            messages
        )

        return response.content.strip()


    # =================================================
    # CHECK EVIDENCE SUFFICIENCY
    # =================================================

    def check_evidence(
        self,
        question: str,
        context: str
    ) -> str:
        """
        Ask the LLM whether the retrieved evidence
        is sufficient to answer the question.

        Returns:

            SUFFICIENT

        or

            INSUFFICIENT
        """

        if not question.strip():
            raise ValueError(
                "Question cannot be empty."
            )

        if not context.strip():
            return "INSUFFICIENT"

        prompt = (
            self.prompt_builder
            .build_abstention_prompt(
                question=question,
                context=context
            )
        )

        messages = [
            (
                "system",
                (
                    "You are an evidence verification "
                    "assistant. Return only the required "
                    "classification."
                )
            ),
            (
                "human",
                prompt
            )
        ]

        response = self.llm.invoke(
            messages
        )

        result = response.content.strip().upper()

        # -------------------------------------------------
        # Normalize response
        # -------------------------------------------------

        if result == "SUFFICIENT":
            return "SUFFICIENT"

        return "INSUFFICIENT"


    # =================================================
    # GET MODEL INFORMATION
    # =================================================

    def get_model_info(self) -> dict:
        """
        Return information about the configured
        language model.
        """

        return {
            "provider": "Groq",
            "model": self.model_name,
            "temperature": self.temperature,
            "max_tokens": self.max_tokens
        }


# =====================================================
# HELPER FUNCTION
# =====================================================

def create_llm() -> IPLLM:
    """
    Create the default IP-SAKTI LLM.
    """

    return IPLLM()


# =====================================================
# TEST FUNCTION
# =====================================================

def test_llm():
    """
    Test the Groq LLM using a small sample of
    retrieved legal evidence.
    """

    print("\n" + "=" * 70)
    print("LLM TEST")
    print("=" * 70)

    # -------------------------------------------------
    # Initialize model
    # -------------------------------------------------

    llm = IPLLM()

    # -------------------------------------------------
    # Sample question
    # -------------------------------------------------

    question = (
        "What conditions must an invention satisfy "
        "for patent protection?"
    )

    # -------------------------------------------------
    # Sample evidence
    # -------------------------------------------------

    context = """
[SOURCE 1]
Document: Indian Patent Test Document
Authority: Indian Patent Authority
Jurisdiction: India
Section: Section 3
Page: 1

Content:
An invention must be novel and involve an
inventive step to qualify for patent protection.
"""

    # -------------------------------------------------
    # Generate answer
    # -------------------------------------------------

    print("\nGenerating answer...")

    answer = llm.generate(
        question=question,
        context=context
    )

    print("\n" + "=" * 70)
    print("GENERATED ANSWER")
    print("=" * 70)

    print(answer)

    # -------------------------------------------------
    # Test query rewriting
    # -------------------------------------------------

    print("\n" + "=" * 70)
    print("QUERY REWRITE TEST")
    print("=" * 70)

    rewritten_query = llm.rewrite_query(
        question
    )

    print(
        f"\nOriginal:\n{question}"
    )

    print(
        f"\nRewritten:\n{rewritten_query}"
    )

    # -------------------------------------------------
    # Test evidence verification
    # -------------------------------------------------

    print("\n" + "=" * 70)
    print("EVIDENCE VERIFICATION TEST")
    print("=" * 70)

    evidence_status = llm.check_evidence(
        question=question,
        context=context
    )

    print(
        f"\nEvidence status: "
        f"{evidence_status}"
    )

    # -------------------------------------------------
    # Model information
    # -------------------------------------------------

    print("\n" + "=" * 70)
    print("MODEL INFORMATION")
    print("=" * 70)

    print(
        llm.get_model_info()
    )

    print("\n" + "=" * 70)
    print("LLM TEST COMPLETED!")
    print("=" * 70)


# =====================================================
# MAIN
# =====================================================

if __name__ == "__main__":
    test_llm()
