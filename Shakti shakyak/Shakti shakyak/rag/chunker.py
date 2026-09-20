"""
IP-SAKTI SAHAYAK
Legal-Aware Document Chunker
============================
Hierarchical text chunker specifically engineered for legal statutes, patent acts,
and regulatory texts. Preserves Acts, Chapters, Sections, Subsections, and Clauses.
"""

import re
import hashlib
from typing import List, Dict, Any, Optional


class LegalChunker:
    """Specialized chunker preserving legal structure, section headers, and metadata."""

    def __init__(self, target_chunk_size: int = 800, overlap_size: int = 150):
        self.target_chunk_size = target_chunk_size
        self.overlap_size = overlap_size

        # Regex patterns for Indian & International legal documents
        self.section_pattern = re.compile(
            r"(?P<sec_tag>(?:Section|Sec\.|Article|Rule|Clause)\s+\d+(?:[a-zA-Z]|\([0-9a-zA-Z]+\))*)",
            re.IGNORECASE
        )
        self.chapter_pattern = re.compile(
            r"(?:CHAPTER|Chapter|PART|Part)\s+([IVXLCDM\d]+[A-Z]*)",
            re.IGNORECASE
        )

    def extract_section_identifier(self, text: str) -> Optional[str]:
        """Extract legal section/article identifier from a snippet of text."""
        match = self.section_pattern.search(text)
        if match:
            return match.group("sec_tag").strip()
        return None

    def chunk_legal_text(
        self,
        text: str,
        metadata: Dict[str, Any]
    ) -> List[Dict[str, Any]]:
        """
        Split a legal document into coherent, self-contained chunks while
        preserving section headings, authority, and lineage.
        """
        if not text or not text.strip():
            return []

        # Split document by double newlines or section boundaries
        raw_paragraphs = [p.strip() for p in re.split(r"\n\s*\n", text) if p.strip()]
        chunks: List[Dict[str, Any]] = []

        current_chunk_text = ""
        current_section = metadata.get("section") or "General"
        chunk_idx = 0

        for para in raw_paragraphs:
            # Check if this paragraph introduces a new section or article
            sec_match = self.extract_section_identifier(para[:120])
            if sec_match:
                current_section = sec_match

            # If appending this paragraph exceeds target size and current text isn't empty, flush
            if len(current_chunk_text) + len(para) > self.target_chunk_size and current_chunk_text:
                chunk_record = self._build_chunk(
                    text=current_chunk_text.strip(),
                    chunk_index=chunk_idx,
                    section=current_section,
                    metadata=metadata
                )
                chunks.append(chunk_record)
                chunk_idx += 1

                # Keep overlap from the end of the previous chunk
                words = current_chunk_text.split()
                overlap = " ".join(words[-25:]) if len(words) > 25 else ""
                current_chunk_text = overlap + " " + para
            else:
                current_chunk_text = (current_chunk_text + "\n" + para).strip()

        # Flush remaining text
        if current_chunk_text.strip():
            chunk_record = self._build_chunk(
                text=current_chunk_text.strip(),
                chunk_index=chunk_idx,
                section=current_section,
                metadata=metadata
            )
            chunks.append(chunk_record)

        return chunks

    def _build_chunk(
        self,
        text: str,
        chunk_index: int,
        section: str,
        metadata: Dict[str, Any]
    ) -> Dict[str, Any]:
        """Construct a metadata-rich chunk dictionary."""
        content_hash = hashlib.sha256(text.encode("utf-8", errors="ignore")).hexdigest()

        return {
            "chunk_index": chunk_index,
            "section": section,
            "text": text,
            "content_hash": content_hash,
            "document_title": metadata.get("title", "Unknown Legal Document"),
            "document_type": metadata.get("document_type", "Act"),
            "source_id": metadata.get("source_id", "official_source"),
            "authority": metadata.get("authority", "Official Legal Authority"),
            "authority_level": metadata.get("authority_level", "LEVEL_1"),
            "jurisdiction": metadata.get("jurisdiction", "India"),
            "domain": metadata.get("domain", "General IP"),
            "source_url": metadata.get("source_url", "https://ipindia.gov.in"),
            "version": metadata.get("version", "1.0"),
            "effective_date": metadata.get("effective_date", "Current"),
            "token_count": len(text.split())
        }
