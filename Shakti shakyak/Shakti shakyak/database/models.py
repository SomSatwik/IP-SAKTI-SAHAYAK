"""
IP-SAKTI SAHAYAK
SQLAlchemy ORM Data Models
==========================
Defines database schemas for queries, sources, indexed legal documents,
chunks, citations, regulatory updates, and compliance records.
"""

from datetime import datetime
from sqlalchemy import (
    Column, Integer, String, Text, Float, Boolean, DateTime, ForeignKey
)
from sqlalchemy.orm import declarative_base, relationship

Base = declarative_base()


class Source(Base):
    """Authoritative legal/regulatory source registry."""
    __tablename__ = "sources"

    id = Column(String(64), primary_key=True)
    name = Column(String(255), nullable=False)
    base_url = Column(String(512), nullable=False)
    jurisdiction = Column(String(64), default="India")
    domain = Column(String(255), default="General IP")
    authority = Column(String(255), nullable=False)
    authority_level = Column(String(32), default="LEVEL_1")
    enabled = Column(Boolean, default=True)
    priority = Column(Integer, default=1)
    created_at = Column(DateTime, default=datetime.utcnow)

    documents = relationship("Document", back_populates="source", cascade="all, delete-orphan")


class Document(Base):
    """Official legal acts, treaties, pharmacopoeias, or guidelines."""
    __tablename__ = "documents"

    id = Column(Integer, primary_key=True, autoincrement=True)
    source_id = Column(String(64), ForeignKey("sources.id"), nullable=True)
    title = Column(String(512), nullable=False)
    document_type = Column(String(64), default="Act")  # Act, Rule, Treaty, Notification, Guideline
    jurisdiction = Column(String(64), default="India")
    domain = Column(String(128), default="Patent")
    official_url = Column(String(1024), nullable=True)
    file_path = Column(String(1024), nullable=True)
    version = Column(String(64), default="1.0")
    effective_date = Column(String(64), nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    source = relationship("Source", back_populates="documents")
    chunks = relationship("DocumentChunk", back_populates="document", cascade="all, delete-orphan")


class DocumentChunk(Base):
    """Fine-grained legal chunk preserving section/clause hierarchy."""
    __tablename__ = "document_chunks"

    id = Column(Integer, primary_key=True, autoincrement=True)
    document_id = Column(Integer, ForeignKey("documents.id"), nullable=False)
    chunk_index = Column(Integer, nullable=False)
    section = Column(String(128), nullable=True)  # e.g., "Section 3(d)", "Section 3(p)"
    subsection = Column(String(128), nullable=True)
    text = Column(Text, nullable=False)
    content_hash = Column(String(64), index=True)
    created_at = Column(DateTime, default=datetime.utcnow)

    document = relationship("Document", back_populates="chunks")


class QueryLog(Base):
    """User query history, generated answer, confidence score, and metrics."""
    __tablename__ = "query_logs"

    id = Column(Integer, primary_key=True, autoincrement=True)
    conversation_id = Column(String(64), index=True)
    query_text = Column(Text, nullable=False)
    detected_domain = Column(String(128))
    detected_jurisdiction = Column(String(64))
    language = Column(String(32), default="en")
    answer_text = Column(Text)
    confidence_score = Column(Float, default=0.0)
    confidence_level = Column(String(32), default="LOW")
    is_abstained = Column(Boolean, default=False)
    abstention_reason = Column(String(255), nullable=True)
    latency_ms = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.utcnow)

    citations = relationship("Citation", back_populates="query_log", cascade="all, delete-orphan")


class Citation(Base):
    """Traceable citation linking claims directly to verified legal chunks."""
    __tablename__ = "citations"

    id = Column(Integer, primary_key=True, autoincrement=True)
    query_log_id = Column(Integer, ForeignKey("query_logs.id"), nullable=False)
    citation_index = Column(Integer, nullable=False)  # 1 for [Source 1], 2 for [Source 2]
    document_title = Column(String(512), nullable=False)
    authority = Column(String(255))
    jurisdiction = Column(String(64))
    section = Column(String(128))
    source_url = Column(String(1024))
    text_snippet = Column(Text)
    is_verified = Column(Boolean, default=True)

    query_log = relationship("QueryLog", back_populates="citations")


class RegulationUpdate(Base):
    """Monitored regulatory changes, gazette notifications, and rule amendments."""
    __tablename__ = "regulation_updates"

    id = Column(Integer, primary_key=True, autoincrement=True)
    source_name = Column(String(255), nullable=False)
    title = Column(String(512), nullable=False)
    notification_number = Column(String(128), nullable=True)
    category = Column(String(128), default="Ayurveda / IP")
    summary = Column(Text)
    source_url = Column(String(1024))
    issued_date = Column(String(64))
    detected_at = Column(DateTime, default=datetime.utcnow)


class ComplianceCheck(Base):
    """Logged preliminary compliance evaluations (e.g., NBA Section 6 / ABS)."""
    __tablename__ = "compliance_checks"

    id = Column(Integer, primary_key=True, autoincrement=True)
    project_title = Column(String(255))
    applicant_type = Column(String(64))  # Indian Entity, Foreign Entity, NRI
    uses_biological_resource = Column(Boolean, default=True)
    commercial_intent = Column(Boolean, default=True)
    ip_protection_intended = Column(Boolean, default=True)
    required_form = Column(String(64))  # Form I, Form II, Form III
    recommendation = Column(Text)
    created_at = Column(DateTime, default=datetime.utcnow)
