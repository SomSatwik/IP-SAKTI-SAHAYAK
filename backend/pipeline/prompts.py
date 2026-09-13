
"""
=====================================================
IP-SAKTI SAHAYAK
RAG Prompt Templates
=====================================================

Purpose:
    Define the prompts used by the IP-SAKTI RAG
    pipeline to generate grounded and source-cited
    answers.

Core Principles:

    1. Answer only from retrieved evidence.
    2. Never invent legal provisions.
    3. Never fabricate citations.
    4. Clearly identify uncertainty.
    5. Abstain when evidence is insufficient.
    6. Respect jurisdiction.
    7. Preserve important legal terminology.
    8. Distinguish legal information from legal advice.

Pipeline:

    User Query
         ↓
    Retrieved Evidence
         ↓
    RAG Prompt
         ↓
    LLM
         ↓
    Grounded Answer
         ↓
    Citation Verification

=====================================================
"""

from typing import List, Dict, Any


# =====================================================
# SYSTEM PROMPT
# =====================================================

SYSTEM_PROMPT = """
You are IP-SAKTI Sahayak, an AI-powered research
assistant for Intellectual Property, Ayurveda,
Traditional Knowledge, Biodiversity and related
regulatory matters.

Your primary responsibility is to provide accurate,
grounded and source-cited information.

STRICT RULES:

1. USE ONLY THE PROVIDED EVIDENCE
-----------------------------------------------------
Answer the user's question using only the retrieved
evidence supplied in the context.

Do not rely on your general knowledge when the
required information is not present in the evidence.

2. NEVER INVENT LEGAL INFORMATION
-----------------------------------------------------
Never fabricate:

- laws
- sections
- rules
- regulations
- articles
- legal requirements
- deadlines
- authorities
- case names
- case outcomes
- citations
- URLs

3. CITATIONS ARE REQUIRED
-----------------------------------------------------
Whenever you make a factual legal or regulatory
claim based on the evidence, cite the corresponding
source using:

[SOURCE 1]
[SOURCE 2]
[SOURCE 3]

Use only source numbers that actually exist in
the provided evidence.

4. HANDLE INSUFFICIENT EVIDENCE SAFELY
-----------------------------------------------------
If the retrieved evidence does not contain enough
information to answer the question reliably, clearly
state:

"Based on the available sources, I do not have
sufficient evidence to answer this reliably."

Do not guess.

5. RESPECT JURISDICTION
-----------------------------------------------------
Legal and regulatory requirements can differ between
countries and jurisdictions.

Always consider the jurisdiction stated in the
evidence and the user's question.

Do not apply Indian law to another jurisdiction
unless the evidence explicitly supports it.

6. DISTINGUISH INFORMATION FROM LEGAL ADVICE
-----------------------------------------------------
Provide legal and regulatory information for
research and educational purposes.

Do not present the response as personalized legal
advice.

7. PRESERVE LEGAL MEANING
-----------------------------------------------------
Do not unnecessarily simplify or alter the meaning
of legal provisions.

If explaining a provision in simpler language,
clearly distinguish the explanation from the
original legal requirement.

8. SOURCE PRIORITY
-----------------------------------------------------
Prefer authoritative sources such as:

- Government authorities
- Statutes
- Regulations
- Official registries
- Official guidelines
- International treaties
- Official IP organizations
- Pharmacopoeial standards
- Official case-law sources

9. DO NOT OVERSTATE CERTAINTY
-----------------------------------------------------
If the evidence is ambiguous, outdated, incomplete
or conflicting, explicitly mention the limitation.

10. ANSWER STRUCTURE
-----------------------------------------------------
Prefer the following structure when appropriate:

Answer

Key Points

Sources

Keep the answer concise but sufficiently detailed
to answer the user's question.
"""


# =====================================================
# RAG PROMPT TEMPLATE
# =====================================================

RAG_PROMPT_TEMPLATE = """
You are answering a user question using retrieved
authoritative evidence.

USER QUESTION:
{question}

RETRIEVED EVIDENCE:
{context}

=====================================================
INSTRUCTIONS
=====================================================

1. Answer the question using ONLY the retrieved
   evidence.

2. Every important factual claim should be supported
   by one or more source citations.

3. Use citations in this format:

   [SOURCE 1]
   [SOURCE 2]

4. Do not create source numbers that are not present.

5. Do not invent legal sections, rules, cases,
   regulations, dates or requirements.

6. If the evidence is insufficient, say so clearly
   instead of guessing.

7. Respect the jurisdiction of each source.

8. If multiple sources provide relevant information,
   synthesize them carefully.

9. If sources conflict, explicitly identify the
   conflict instead of silently choosing one.

10. Do not provide personalized legal advice.

=====================================================
RESPONSE FORMAT
=====================================================

Answer:
Provide a direct answer to the user's question.

Key Points:
List the most important points when useful.

Sources:
List the source numbers used in the answer.

=====================================================
"""


# =====================================================
# QUERY REWRITE PROMPT
# =====================================================

QUERY_REWRITE_PROMPT = """
Rewrite the following user question into a concise
search query suitable for retrieving authoritative
Intellectual Property, Ayurveda or regulatory
documents.

USER QUESTION:
{question}

Rules:

- Preserve the user's intent.
- Preserve important legal terminology.
- Include jurisdiction if explicitly mentioned.
- Do not answer the question.
- Do not add information that was not provided.

Return only the rewritten search query.
"""


# =====================================================
# SOURCE SUMMARY PROMPT
# =====================================================

SOURCE_SUMMARY_PROMPT = """
Summarize the following retrieved legal or regulatory
source for use by a downstream RAG system.

SOURCE:
{source}

Rules:

- Preserve legal meaning.
- Do not introduce new facts.
- Do not invent missing information.
- Preserve section numbers and important terminology.
- Keep the summary concise.
"""


# =====================================================
# ABSTENTION PROMPT
# =====================================================

ABSTENTION_PROMPT = """
Determine whether the retrieved evidence is sufficient
to answer the user's question reliably.

USER QUESTION:
{question}

RETRIEVED EVIDENCE:
{context}

Return exactly one of:

SUFFICIENT

or

INSUFFICIENT

Choose INSUFFICIENT when:

- the evidence does not address the question,
- the evidence is too vague,
- important information is missing,
- the sources conflict without resolution,
- or answering would require unsupported assumptions.
"""


# =====================================================
# CITATION INSTRUCTION
# =====================================================

CITATION_INSTRUCTION = """
CITATION RULE:

Only cite information that is supported by the
retrieved evidence.

Use:

[SOURCE 1]
[SOURCE 2]

Never:

- invent source numbers,
- invent URLs,
- invent document names,
- invent legal provisions,
- cite sources that were not retrieved.

If evidence is insufficient, explicitly state that
the available sources are insufficient.
"""


# =====================================================
# PROMPT BUILDER CLASS
# =====================================================

class IPPromptBuilder:
    """
    Build prompts for different stages of the
    IP-SAKTI RAG pipeline.
    """

    def __init__(
        self,
        system_prompt: str = SYSTEM_PROMPT
    ):
        self.system_prompt = system_prompt

    # =================================================
    # BUILD RAG PROMPT
    # =================================================

    def build_rag_prompt(
        self,
        question: str,
        context: str
    ) -> str:
        """
        Build the main grounded RAG prompt.
        """

        if not question.strip():
            raise ValueError(
                "Question cannot be empty."
            )

        if not context.strip():
            raise ValueError(
                "Context cannot be empty."
            )

        return RAG_PROMPT_TEMPLATE.format(
            question=question,
            context=context
        )

    # =================================================
    # BUILD QUERY REWRITE PROMPT
    # =================================================

    def build_query_rewrite_prompt(
        self,
        question: str
    ) -> str:
        """
        Build a prompt for query rewriting.
        """

        return QUERY_REWRITE_PROMPT.format(
            question=question
        )

    # =================================================
    # BUILD ABSTENTION PROMPT
    # =================================================

    def build_abstention_prompt(
        self,
        question: str,
        context: str
    ) -> str:
        """
        Build a prompt that determines whether
        enough evidence exists to answer.
        """

        return ABSTENTION_PROMPT.format(
            question=question,
            context=context
        )

    # =================================================
    # BUILD SOURCE SUMMARY PROMPT
    # =================================================

    def build_source_summary_prompt(
        self,
        source: str
    ) -> str:
        """
        Build a prompt for summarizing retrieved
        source material.
        """

        return SOURCE_SUMMARY_PROMPT.format(
            source=source
        )


# =====================================================
# HELPER FUNCTION
# =====================================================

def create_prompt_builder() -> IPPromptBuilder:
    """
    Create the default IP-SAKTI prompt builder.
    """

    return IPPromptBuilder()


# =====================================================
# PROMPT PREVIEW FUNCTION
# =====================================================

def preview_rag_prompt(
    question: str,
    context: str
):
    """
    Display the generated RAG prompt for debugging.
    """

    builder = IPPromptBuilder()

    prompt = builder.build_rag_prompt(
        question=question,
        context=context
    )

    print("\n" + "=" * 70)
    print("RAG PROMPT PREVIEW")
    print("=" * 70)

    print(prompt)

    print("\n" + "=" * 70)


# =====================================================
# TEST FUNCTION
# =====================================================

def test_prompts():
    """
    Test the prompt builder using sample evidence.
    """

    print("\n" + "=" * 70)
    print("PROMPT MODULE TEST")
    print("=" * 70)

    builder = IPPromptBuilder()

    # -------------------------------------------------
    # Sample question
    # -------------------------------------------------

    question = (
        "What conditions must an invention satisfy "
        "for patent protection in India?"
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
    # Build prompt
    # -------------------------------------------------

    prompt = builder.build_rag_prompt(
        question=question,
        context=context
    )

    print("\nGenerated RAG Prompt:")
    print("-" * 70)
    print(prompt)

    # -------------------------------------------------
    # Test query rewrite
    # -------------------------------------------------

    rewrite_prompt = (
        builder.build_query_rewrite_prompt(
            question
        )
    )

    print("\nQuery Rewrite Prompt:")
    print("-" * 70)
    print(rewrite_prompt)

    # -------------------------------------------------
    # Test abstention prompt
    # -------------------------------------------------

    abstention_prompt = (
        builder.build_abstention_prompt(
            question=question,
            context=context
        )
    )

    print("\nAbstention Prompt:")
    print("-" * 70)
    print(abstention_prompt)

    print("\n" + "=" * 70)
    print("PROMPT MODULE TEST COMPLETED!")
    print("=" * 70)


# =====================================================
# MAIN
# =====================================================

if __name__ == "__main__":
    test_prompts()
