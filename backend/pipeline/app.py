
"""
=====================================================
IP-SAKTI SAHAYAK
Streamlit Application
=====================================================

Purpose:
    Provide a user-friendly web interface for the
    IP-SAKTI SAHAYAK RAG system.

The application allows users to:

    1. Ask IP and regulatory questions.
    2. Retrieve authoritative evidence.
    3. Generate grounded answers using the LLM.
    4. View source information.
    5. View evidence status.
    6. Understand that responses are informational
       and not legal advice.

Architecture:

    Streamlit UI
          ↓
    RAG Pipeline
          ↓
    Retriever
          ↓
    FAISS
          ↓
    Evidence
          ↓
    Groq LLM
          ↓
    Answer + Sources

=====================================================
"""

import streamlit as st

from .rag_pipeline import create_rag_pipeline


# =====================================================
# PAGE CONFIGURATION
# =====================================================

st.set_page_config(
    page_title="IP-SAKTI SAHAYAK",
    page_icon="⚖️",
    layout="wide"
)


# =====================================================
# APPLICATION HEADER
# =====================================================

st.title("⚖️ IP-SAKTI SAHAYAK")

st.subheader(
    "AI-Powered Intellectual Property & Regulatory Assistant"
)

st.write(
    "Ask questions related to patents, trademarks, "
    "copyright, Ayurveda, traditional knowledge, "
    "biodiversity and other intellectual property topics."
)


# =====================================================
# DISCLAIMER
# =====================================================

st.warning(
    "This system provides informational and research "
    "assistance only. It does not constitute legal advice."
)


# =====================================================
# LOAD RAG PIPELINE
# =====================================================

@st.cache_resource
def load_rag_pipeline():
    """
    Load and cache the RAG pipeline.

    The pipeline is expensive to initialize because
    it loads the embedding model, FAISS vector store
    and LLM.

    Streamlit caching prevents these components from
    being loaded again for every user interaction.

    Returns:
        Initialized IP-SAKTI RAG pipeline.
    """

    return create_rag_pipeline()


# =====================================================
# INITIALIZE PIPELINE
# =====================================================

with st.spinner(
    "Loading IP-SAKTI knowledge system..."
):

    try:

        rag_pipeline = load_rag_pipeline()

        pipeline_loaded = True

    except Exception as error:

        pipeline_loaded = False

        st.error(
            "Unable to initialize the RAG pipeline."
        )

        st.exception(
            error
        )


# =====================================================
# SAMPLE QUESTIONS
# =====================================================

st.markdown(
    "### 💡 Example Questions"
)


sample_questions = [
    "What is a patent in India?",
    "What rights does a patent owner have?",
    "What is traditional knowledge?",
    "How is Ayurveda-related intellectual property protected?",
    "What is the role of the TKDL?"
]


# =====================================================
# SAMPLE QUESTION BUTTONS
# =====================================================

selected_question = None


columns = st.columns(
    len(sample_questions)
)


for index, question in enumerate(
    sample_questions
):

    with columns[index]:

        if st.button(
            question,
            key=f"sample_{index}",
            use_container_width=True
        ):

            selected_question = question


# =====================================================
# USER QUESTION INPUT
# =====================================================

st.markdown(
    "### 🔎 Ask Your Question"
)


question = st.text_area(
    "Enter your IP or regulatory question:",
    value=selected_question or "",
    height=120,
    placeholder=(
        "Example: What rights does a patent "
        "provide to its owner in India?"
    )
)


# =====================================================
# ASK BUTTON
# =====================================================

ask_button = st.button(
    "🚀 Ask IP-SAKTI",
    type="primary",
    use_container_width=True
)


# =====================================================
# PROCESS QUESTION
# =====================================================

if ask_button:

    if not pipeline_loaded:

        st.error(
            "The RAG pipeline could not be loaded. "
            "Please check the error shown above."
        )


    elif not question.strip():

        st.warning(
            "Please enter a question."
        )


    else:

        # =================================================
        # RUN RAG PIPELINE
        # =================================================

        with st.spinner(
            "Searching authoritative sources and "
            "generating answer..."
        ):

            try:

                result = rag_pipeline.run(
                    question
                )


                # =================================================
                # STORE RESULT IN SESSION
                # =================================================

                st.session_state[
                    "last_result"
                ] = result


            except Exception as error:

                st.error(
                    "An error occurred while processing "
                    "your question."
                )

                st.exception(
                    error
                )


# =====================================================
# DISPLAY LAST RESULT
# =====================================================

if "last_result" in st.session_state:

    result = st.session_state[
        "last_result"
    ]


    # =================================================
    # RESPONSE HEADER
    # =================================================

    st.markdown(
        "---"
    )

    st.markdown(
        "## 🧠 Answer"
    )


    # =================================================
    # ANSWER
    # =================================================

    st.markdown(
        result.get(
            "answer",
            "No answer generated."
        )
    )


    # =================================================
    # EVIDENCE STATUS
    # =================================================

    st.markdown(
        "### 📊 Evidence Status"
    )


    evidence_status = result.get(
        "evidence_status",
        "UNKNOWN"
    )


    if evidence_status == "SUFFICIENT":

        st.success(
            "Sufficient evidence was found "
            "in the knowledge base."
        )

    else:

        st.warning(
            "The available evidence may not be "
            "sufficient to answer the question reliably."
        )


    # =================================================
    # RETRIEVED DOCUMENT COUNT
    # =================================================

    retrieved_documents = result.get(
        "retrieved_documents",
        0
    )


    st.info(
        f"Retrieved document chunks: "
        f"{retrieved_documents}"
    )


    # =================================================
    # SOURCES
    # =================================================

    st.markdown(
        "### 📚 Sources"
    )


    sources = result.get(
        "sources",
        []
    )


    if not sources:

        st.write(
            "No source information is available."
        )


    else:

        for source in sources:

            source_number = source.get(
                "source_number",
                "?"
            )


            with st.expander(
                f"Source {source_number}: "
                f"{source.get('document_name', 'Unknown')}"
            ):

                # =============================================
                # SOURCE INFORMATION
                # =============================================

                st.write(
                    f"**Document:** "
                    f"{source.get('document_name', 'Unknown')}"
                )


                st.write(
                    f"**Document Type:** "
                    f"{source.get('document_type', 'Unknown')}"
                )


                st.write(
                    f"**Authority:** "
                    f"{source.get('authority', 'Unknown')}"
                )


                st.write(
                    f"**Jurisdiction:** "
                    f"{source.get('jurisdiction', 'Unknown')}"
                )


                st.write(
                    f"**Section:** "
                    f"{source.get('section', 'Unknown')}"
                )


                st.write(
                    f"**Page:** "
                    f"{source.get('page', 'Unknown')}"
                )


                st.write(
                    f"**Version:** "
                    f"{source.get('version', 'Unknown')}"
                )


                st.write(
                    f"**Effective Date:** "
                    f"{source.get('effective_date', 'Unknown')}"
                )


                source_url = source.get(
                    "source_url",
                    ""
                )


                if source_url:

                    st.write(
                        "**Source URL:**"
                    )

                    st.link_button(
                        "Open Source",
                        source_url
                    )


    # =================================================
    # DISCLAIMER
    # =================================================

    st.markdown(
        "---"
    )


    st.caption(
        result.get(
            "disclaimer",
            "This information is provided for "
            "informational purposes only and does "
            "not constitute legal advice."
        )
    )


# =====================================================
# SIDEBAR
# =====================================================

with st.sidebar:

    st.header(
        "⚖️ IP-SAKTI SAHAYAK"
    )


    st.write(
        "A Retrieval-Augmented Generation system "
        "for intellectual property and regulatory "
        "guidance."
    )


    st.markdown(
        "### Supported Areas"
    )


    st.write(
        """
        • Patents

        • Trademarks

        • Copyright

        • Geographical Indications

        • Designs

        • Ayurveda

        • Traditional Knowledge

        • Biodiversity

        • Access & Benefit Sharing

        • International IP
        """
    )


    st.markdown(
        "### System"
    )


    st.write(
        """
        **Embedding Model:** BGE-M3

        **Vector Database:** FAISS

        **LLM:** Groq

        **Architecture:** RAG
        """
    )


    st.markdown(
        "---"
    )


    st.caption(
        "IP-SAKTI SAHAYAK — SIH 2026"
    )
