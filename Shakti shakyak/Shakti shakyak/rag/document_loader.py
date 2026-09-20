"""
IP-SAKTI SAHAYAK
Authoritative Document Loader
=============================
Multi-format parser for legal texts (PDF, TXT, HTML, JSON).
Extracts text with page numbers, document hierarchy, and authoritative metadata.
"""

import json
import logging
from pathlib import Path
from typing import Dict, Any, List, Optional
from bs4 import BeautifulSoup
import pypdf

logger = logging.getLogger(__name__)


class DocumentLoader:
    """Loads authoritative legal documents across standard formats."""

    @staticmethod
    def load_txt(file_path: Path, metadata: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """Load standard plain text document."""
        with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
            text = f.read()
        doc_meta = metadata or {}
        doc_meta.setdefault("title", file_path.stem.replace("_", " ").title())
        doc_meta.setdefault("file_path", str(file_path))
        doc_meta.setdefault("document_type", "Text Document")
        return {"text": text, "metadata": doc_meta}

    @staticmethod
    def load_pdf(file_path: Path, metadata: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """Extract text from official PDF gazettes and acts with page tracking."""
        pages_text = []
        try:
            reader = pypdf.PdfReader(str(file_path))
            for idx, page in enumerate(reader.pages):
                extracted = page.extract_text() or ""
                if extracted.strip():
                    pages_text.append(f"[Page {idx + 1}]\n{extracted.strip()}")
        except Exception as e:
            logger.error("Failed to parse PDF %s: %s", file_path, str(e))
            raise

        full_text = "\n\n".join(pages_text)
        doc_meta = metadata or {}
        doc_meta.setdefault("title", file_path.stem.replace("_", " ").title())
        doc_meta.setdefault("file_path", str(file_path))
        doc_meta.setdefault("document_type", "Official PDF")
        doc_meta.setdefault("page_count", len(pages_text))
        return {"text": full_text, "metadata": doc_meta}

    @staticmethod
    def load_html(file_path: Path, metadata: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """Extract clean text and structure from HTML legal portals."""
        with open(file_path, "r", encoding="utf-8", errors="ignore") as f:
            html_content = f.read()

        soup = BeautifulSoup(html_content, "html.parser")
        for tag in soup(["script", "style", "nav", "footer", "header"]):
            tag.decompose()

        text = soup.get_text(separator="\n", strip=True)
        doc_meta = metadata or {}
        doc_meta.setdefault("title", soup.title.string.strip() if soup.title else file_path.stem)
        doc_meta.setdefault("file_path", str(file_path))
        doc_meta.setdefault("document_type", "HTML Gazette")
        return {"text": text, "metadata": doc_meta}

    @staticmethod
    def load_json(file_path: Path) -> List[Dict[str, Any]]:
        """Load structured legal statutory corpus stored in JSON format."""
        with open(file_path, "r", encoding="utf-8") as f:
            data = json.load(f)
        if isinstance(data, list):
            return data
        return [data]

    @classmethod
    def auto_load(cls, file_path: Path, metadata: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
        """Auto-detect format and parse appropriately."""
        ext = file_path.suffix.lower()
        if ext == ".pdf":
            return cls.load_pdf(file_path, metadata)
        elif ext in (".html", ".htm"):
            return cls.load_html(file_path, metadata)
        elif ext == ".json":
            docs = cls.load_json(file_path)
            # Combine items if needed or return first
            text = "\n\n".join(d.get("text", "") for d in docs)
            meta = docs[0].get("metadata", metadata or {})
            return {"text": text, "metadata": meta}
        else:
            return cls.load_txt(file_path, metadata)
