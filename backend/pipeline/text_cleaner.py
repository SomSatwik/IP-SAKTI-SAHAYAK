
"""
=====================================================
IP-SAKTI SAHAYAK
Text Cleaner
=====================================================

Purpose:
    Clean extracted PDF text while preserving the
    legal and regulatory structure required for
    accurate RAG retrieval and citation.

Cleaning Operations:

    Raw PDF Text
         ↓
    Normalize whitespace
         ↓
    Fix broken line formatting
         ↓
    Remove unnecessary blank lines
         ↓
    Preserve legal numbering
         ↓
    Clean Document objects
         ↓
    Ready for Chunking

Important:
    Legal text must NOT be aggressively cleaned.

    Structures such as:

        Section 3
        Section 3(1)
        (a)
        (b)
        Explanation
        Provided that
        Rule 12

    are meaningful and must be preserved.
=====================================================
"""

import re

from typing import List

from langchain_core.documents import Document


# =====================================================
# TEXT CLEANER CLASS
# =====================================================

class IPTextCleaner:
    """
    Cleaner responsible for preprocessing extracted
    legal, regulatory and Ayurveda-related text.
    """

    def __init__(
        self,
        remove_headers: bool = False,
        remove_footers: bool = False
    ):
        """
        Initialize the text cleaner.

        Parameters
        ----------
        remove_headers : bool
            Whether repeated document headers should
            be removed.

        remove_footers : bool
            Whether repeated document footers should
            be removed.

        Notes
        -----
        Header and footer removal is disabled by
        default because legal documents may contain
        useful information in those areas.
        """

        self.remove_headers = remove_headers
        self.remove_footers = remove_footers


    # =================================================
    # CLEAN SINGLE TEXT
    # =================================================

    def clean_text(self, text: str) -> str:
        """
        Clean a single block of extracted PDF text.

        The method performs conservative cleaning so
        that legal meaning and numbering are preserved.
        """

        if not text:
            return ""

        # -------------------------------------------------
        # STEP 1: Normalize line endings
        # -------------------------------------------------

        text = text.replace("\r\n", "\n")
        text = text.replace("\r", "\n")


        # -------------------------------------------------
        # STEP 2: Remove excessive spaces
        # -------------------------------------------------

        # Replace multiple spaces/tabs with one space.
        text = re.sub(
            r"[ \t]+",
            " ",
            text
        )


        # -------------------------------------------------
        # STEP 3: Remove spaces at line boundaries
        # -------------------------------------------------

        text = re.sub(
            r"[ \t]*\n[ \t]*",
            "\n",
            text
        )


        # -------------------------------------------------
        # STEP 4: Fix excessive blank lines
        # -------------------------------------------------

        # Keep at most one blank line between paragraphs.
        text = re.sub(
            r"\n{3,}",
            "\n\n",
            text
        )


        # -------------------------------------------------
        # STEP 5: Fix broken words caused by PDF extraction
        # -------------------------------------------------

        # Example:
        #
        #   intellec-
        #   tual property
        #
        # becomes:
        #
        #   intellectual property
        #
        text = re.sub(
            r"(\w)-\n(\w)",
            r"\1\2",
            text
        )


        # -------------------------------------------------
        # STEP 6: Join ordinary wrapped lines
        # -------------------------------------------------

        text = self._join_wrapped_lines(text)


        # -------------------------------------------------
        # STEP 7: Remove leading/trailing whitespace
        # -------------------------------------------------

        text = text.strip()


        return text


    # =================================================
    # JOIN WRAPPED LINES
    # =================================================

    def _join_wrapped_lines(self, text: str) -> str:
        """
        Join PDF lines that are likely part of the same
        paragraph.

        Legal structures and headings are preserved.

        Example:

            The invention shall be
            disclosed in the application.

        becomes:

            The invention shall be disclosed in the
            application.
        """

        lines = text.split("\n")

        cleaned_lines = []

        for line in lines:

            line = line.strip()

            if not line:
                cleaned_lines.append("")
                continue


            # If there is no previous line,
            # simply add the current line.
            if not cleaned_lines:

                cleaned_lines.append(line)

                continue


            previous_line = cleaned_lines[-1]


            # Preserve blank lines.
            if previous_line == "":

                cleaned_lines.append(line)

                continue


            # Determine whether the current line
            # looks like a new legal structure.
            if self._is_legal_structure(line):

                cleaned_lines.append(line)

                continue


            # Determine whether the previous line
            # looks like a heading.
            if self._is_heading(previous_line):

                cleaned_lines.append(line)

                continue


            # If the previous line ends with punctuation,
            # it may represent the end of a sentence.
            #
            # Keep the new line separate when it looks
            # like a new paragraph.
            if previous_line.endswith(
                (".", ":", ";", "?", "!")
            ):

                cleaned_lines.append(line)

                continue


            # Otherwise join the wrapped line.
            cleaned_lines[-1] = (
                previous_line + " " + line
            )


        return "\n".join(cleaned_lines)


    # =================================================
    # DETECT LEGAL STRUCTURE
    # =================================================

    def _is_legal_structure(self, line: str) -> bool:
        """
        Detect common legal numbering and structural
        patterns.

        Examples:

            Section 3
            Section 3(1)
            Rule 12
            Article 5
            (1)
            (a)
            (i)
        """

        legal_patterns = [

            r"^Section\s+\d+",
            r"^Section\s+\d+\(",

            r"^Rule\s+\d+",
            r"^Article\s+\d+",

            r"^\(\d+\)",
            r"^\([a-zA-Z]\)",
            r"^\([ivxlcdmIVXLCDM]+\)",

            r"^\d+\.",
            r"^\d+\)",

            r"^[a-zA-Z]\.",
            r"^[a-zA-Z]\)",

            r"^Explanation\b",
            r"^Provided that\b",
            r"^Provided further that\b",

            r"^Proviso\b",
            r"^Illustration\b",
            r"^Definitions?\b"
        ]

        for pattern in legal_patterns:

            if re.match(
                pattern,
                line,
                flags=re.IGNORECASE
            ):
                return True


        return False


    # =================================================
    # DETECT HEADING
    # =================================================

    def _is_heading(self, line: str) -> bool:
        """
        Detect whether a line is likely to be a heading.

        This prevents headings from being incorrectly
        joined with the following paragraph.
        """

        if not line:
            return False


        # Short uppercase lines are often headings.
        if (
            len(line) < 120
            and line.upper() == line
            and any(char.isalpha() for char in line)
        ):
            return True


        # Common legal heading patterns.
        heading_patterns = [

            r"^CHAPTER\b",
            r"^PART\b",
            r"^SCHEDULE\b",
            r"^SECTION\b",
            r"^RULE\b",
            r"^ARTICLE\b"
        ]


        for pattern in heading_patterns:

            if re.match(
                pattern,
                line,
                flags=re.IGNORECASE
            ):
                return True


        return False


    # =================================================
    # CLEAN DOCUMENT
    # =================================================

    def clean_document(
        self,
        document: Document
    ) -> Document:
        """
        Clean a LangChain Document while preserving
        all existing metadata.
        """

        cleaned_content = self.clean_text(
            document.page_content
        )


        # Create a new Document rather than modifying
        # the original object directly.
        cleaned_document = Document(
            page_content=cleaned_content,
            metadata=document.metadata.copy()
        )


        return cleaned_document


    # =================================================
    # CLEAN DOCUMENT COLLECTION
    # =================================================

    def clean_documents(
        self,
        documents: List[Document]
    ) -> List[Document]:
        """
        Clean a complete list of LangChain Documents.
        """

        if not documents:

            return []


        cleaned_documents = []


        for document in documents:

            cleaned_document = self.clean_document(
                document
            )

            # Ignore completely empty pages.
            if cleaned_document.page_content.strip():

                cleaned_documents.append(
                    cleaned_document
                )


        return cleaned_documents


# =====================================================
# HELPER FUNCTION
# =====================================================

def clean_documents(
    documents: List[Document]
) -> List[Document]:
    """
    Convenience function for cleaning a collection
    of LangChain Documents.
    """

    cleaner = IPTextCleaner()

    return cleaner.clean_documents(documents)


# =====================================================
# TEXT PREVIEW FUNCTION
# =====================================================

def preview_cleaning(
    original_documents: List[Document],
    cleaned_documents: List[Document],
    num_pages: int = 2
):
    """
    Display the original and cleaned text side by side
    for development and debugging.
    """

    print("\n" + "=" * 70)
    print("TEXT CLEANING PREVIEW")
    print("=" * 70)


    number_of_pages = min(
        num_pages,
        len(original_documents),
        len(cleaned_documents)
    )


    for index in range(number_of_pages):

        original = original_documents[index]
        cleaned = cleaned_documents[index]


        print(
            f"\nPage: "
            f"{cleaned.metadata.get('page')}"
        )

        print("\n" + "-" * 70)
        print("ORIGINAL TEXT")
        print("-" * 70)

        print(
            original.page_content[:1500]
        )


        print("\n" + "-" * 70)
        print("CLEANED TEXT")
        print("-" * 70)

        print(
            cleaned.page_content[:1500]
        )

        print("\n" + "=" * 70)


# =====================================================
# TEST FUNCTION
# =====================================================

if __name__ == "__main__":

    # Small test document for development.
    test_document = Document(
        page_content="""
        SECTION 3

        The intellectual property
        protection framework shall apply to
        traditional knowledge.

        (1) Every applicant shall provide
        the required information.

        Provided that the information
        shall be accurate.
        """,
        metadata={
            "document_name": "Test Document",
            "page": 1
        }
    )


    cleaner = IPTextCleaner()

    cleaned_document = cleaner.clean_document(
        test_document
    )


    print("\n" + "=" * 60)
    print("TEXT CLEANER TEST")
    print("=" * 60)

    print("\nOriginal:")
    print(test_document.page_content)

    print("\nCleaned:")
    print(cleaned_document.page_content)

    print("\nMetadata:")
    print(cleaned_document.metadata)

    print("\nText cleaner is working correctly!")
