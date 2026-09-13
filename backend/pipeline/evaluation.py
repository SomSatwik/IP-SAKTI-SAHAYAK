
"""
=====================================================
IP-SAKTI SAHAYAK
RAG Evaluation Module
=====================================================

Purpose:
    Evaluate the basic quality of the IP-SAKTI SAHAYAK
    Retrieval-Augmented Generation pipeline.

The evaluation checks:

    1. Question processing
    2. Document retrieval
    3. Evidence availability
    4. Answer generation
    5. Citation presence
    6. Basic grounding indicators

This is an MVP evaluation module.

Later versions can include:

    • Retrieval Precision
    • Retrieval Recall
    • MRR
    • nDCG
    • Faithfulness
    • Answer Relevance
    • Citation Accuracy
    • LLM-as-a-Judge
    • Human Evaluation
    • Benchmark Dataset Evaluation

Pipeline:

    Test Questions
          ↓
    RAG Pipeline
          ↓
    Retrieval
          ↓
    Evidence
          ↓
    Answer
          ↓
    Evaluation
          ↓
    Evaluation Report

=====================================================
"""

from typing import List, Dict, Any

from .rag_pipeline import create_rag_pipeline


# =====================================================
# TEST QUESTIONS
# =====================================================

TEST_QUESTIONS = [

    "What is a patent in India?",

    "What rights does a patent provide to the patent owner?",

    "What is traditional knowledge?",

    "What is the role of the Traditional Knowledge Digital Library?",

    "How is Ayurveda-related intellectual property protected?"

]


# =====================================================
# EVALUATION CLASS
# =====================================================

class RAGEvaluator:
    """
    Evaluate the IP-SAKTI SAHAYAK RAG pipeline.

    Parameters:
        rag_pipeline:
            Initialized RAG pipeline.
    """

    def __init__(self, rag_pipeline):

        self.rag_pipeline = rag_pipeline


    # =================================================
    # EVALUATE SINGLE QUESTION
    # =================================================

    def evaluate_question(
        self,
        question: str
    ) -> Dict[str, Any]:
        """
        Evaluate one question using the RAG pipeline.

        Returns:
            Dictionary containing the question,
            answer, evidence status, retrieved
            document count and citation information.
        """

        print("\n" + "=" * 70)

        print(
            f"Evaluating Question:\n{question}"
        )

        print("=" * 70)


        # =================================================
        # RUN RAG PIPELINE
        # =================================================

        try:

            result = self.rag_pipeline.run(
                question
            )

        except Exception as error:

            print(
                f"\nEvaluation failed: {error}"
            )

            return {

                "question": question,

                "success": False,

                "answer": "",

                "evidence_status": "ERROR",

                "retrieved_documents": 0,

                "citations_found": False,

                "error": str(error)

            }


        # =================================================
        # EXTRACT RESULT
        # =================================================

        answer = result.get(
            "answer",
            ""
        )


        evidence_status = result.get(
            "evidence_status",
            "UNKNOWN"
        )


        retrieved_documents = result.get(
            "retrieved_documents",
            0
        )


        sources = result.get(
            "sources",
            []
        )


        # =================================================
        # CHECK CITATIONS
        # =================================================

        citations_found = self.check_citations(
            answer
        )


        # =================================================
        # CHECK ANSWER
        # =================================================

        answer_generated = bool(
            answer.strip()
        )


        # =================================================
        # OVERALL SUCCESS
        # =================================================

        success = (

            answer_generated

            and

            evidence_status != "ERROR"

        )


        evaluation = {

            "question": question,

            "success": success,

            "answer": answer,

            "evidence_status": evidence_status,

            "retrieved_documents": retrieved_documents,

            "sources_count": len(sources),

            "citations_found": citations_found,

            "answer_generated": answer_generated

        }


        # =================================================
        # DISPLAY RESULT
        # =================================================

        print(
            f"\nEvidence Status: "
            f"{evidence_status}"
        )

        print(
            f"Retrieved Documents: "
            f"{retrieved_documents}"
        )

        print(
            f"Sources: "
            f"{len(sources)}"
        )

        print(
            f"Citations Found: "
            f"{citations_found}"
        )

        print(
            f"Answer Generated: "
            f"{answer_generated}"
        )


        return evaluation


    # =================================================
    # CITATION CHECK
    # =================================================

    @staticmethod
    def check_citations(
        answer: str
    ) -> bool:
        """
        Check whether the generated answer contains
        source citation markers.

        Expected citation format:

            [SOURCE 1]

        or:

            [SOURCE 2]

        Returns:
            True if a citation marker exists.
        """

        if not answer:

            return False


        answer_upper = answer.upper()


        for source_number in range(
            1,
            20
        ):

            citation = (
                f"[SOURCE {source_number}]"
            )


            if citation in answer_upper:

                return True


        return False


    # =================================================
    # RUN COMPLETE EVALUATION
    # =================================================

    def evaluate(
        self,
        questions: List[str]
    ) -> List[Dict[str, Any]]:
        """
        Evaluate multiple questions.

        Parameters:
            questions:
                List of test questions.

        Returns:
            List containing evaluation results.
        """

        results = []


        print("\n")

        print("=" * 70)

        print(
            "IP-SAKTI SAHAYAK"
        )

        print(
            "RAG EVALUATION"
        )

        print("=" * 70)


        for question in questions:

            result = self.evaluate_question(
                question
            )

            results.append(
                result
            )


        return results


    # =================================================
    # GENERATE SUMMARY
    # =================================================

    def generate_summary(
        self,
        results: List[Dict[str, Any]]
    ) -> Dict[str, Any]:
        """
        Generate a summary of the evaluation.

        Returns:
            Dictionary containing basic evaluation
            statistics.
        """

        if not results:

            return {

                "total_questions": 0,

                "successful_questions": 0,

                "success_rate": 0.0,

                "citation_rate": 0.0,

                "average_retrieved_documents": 0.0

            }


        total_questions = len(
            results
        )


        successful_questions = sum(

            1

            for result in results

            if result.get(
                "success",
                False
            )

        )


        citation_questions = sum(

            1

            for result in results

            if result.get(
                "citations_found",
                False
            )

        )


        total_documents = sum(

            result.get(
                "retrieved_documents",
                0
            )

            for result in results

        )


        success_rate = (

            successful_questions
            /
            total_questions
            *
            100

        )


        citation_rate = (

            citation_questions
            /
            total_questions
            *
            100

        )


        average_retrieved_documents = (

            total_documents
            /
            total_questions

        )


        summary = {

            "total_questions":
                total_questions,

            "successful_questions":
                successful_questions,

            "success_rate":
                round(
                    success_rate,
                    2
                ),

            "citation_rate":
                round(
                    citation_rate,
                    2
                ),

            "average_retrieved_documents":
                round(
                    average_retrieved_documents,
                    2
                )

        }


        return summary


    # =================================================
    # DISPLAY SUMMARY
    # =================================================

    def display_summary(
        self,
        summary: Dict[str, Any]
    ):
        """
        Display the evaluation summary.
        """

        print("\n")

        print("=" * 70)

        print(
            "EVALUATION SUMMARY"
        )

        print("=" * 70)


        print(
            f"\nTotal Questions: "
            f"{summary['total_questions']}"
        )


        print(
            f"Successful Questions: "
            f"{summary['successful_questions']}"
        )


        print(
            f"Success Rate: "
            f"{summary['success_rate']}%"
        )


        print(
            f"Citation Rate: "
            f"{summary['citation_rate']}%"
        )


        print(
            f"Average Retrieved Documents: "
            f"{summary['average_retrieved_documents']}"
        )


        print("\n" + "=" * 70)


# =====================================================
# CREATE EVALUATOR
# =====================================================

def create_evaluator():
    """
    Create an RAGEvaluator instance.

    Returns:
        Initialized RAGEvaluator.
    """

    rag_pipeline = create_rag_pipeline()

    return RAGEvaluator(
        rag_pipeline
    )


# =====================================================
# RUN EVALUATION
# =====================================================

def run_evaluation(
    questions: List[str] = None
):
    """
    Run the complete RAG evaluation.

    Parameters:
        questions:
            Optional list of test questions.

    Returns:
        Evaluation results and summary.
    """

    if questions is None:

        questions = TEST_QUESTIONS


    # =================================================
    # CREATE EVALUATOR
    # =================================================

    evaluator = create_evaluator()


    # =================================================
    # EVALUATE QUESTIONS
    # =================================================

    results = evaluator.evaluate(
        questions
    )


    # =================================================
    # GENERATE SUMMARY
    # =================================================

    summary = evaluator.generate_summary(
        results
    )


    # =================================================
    # DISPLAY SUMMARY
    # =================================================

    evaluator.display_summary(
        summary
    )


    return results, summary


# =====================================================
# TEST FUNCTION
# =====================================================

def test_evaluation():
    """
    Run a small evaluation test.

    This function uses only the first two
    test questions to reduce API usage.
    """

    print(
        "\nRunning IP-SAKTI evaluation test..."
    )


    questions = TEST_QUESTIONS[:2]


    results, summary = run_evaluation(
        questions
    )


    print(
        "\nEvaluation completed successfully."
    )


    return results, summary


# =====================================================
# MAIN EXECUTION
# =====================================================

if __name__ == "__main__":

    test_evaluation()
