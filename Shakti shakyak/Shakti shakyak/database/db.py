"""
IP-SAKTI SAHAYAK
Database Engine & Session Management
===================================
SQLAlchemy engine initialization, scoped session factory, and database lifecycle.
"""

import logging
from contextlib import contextmanager
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker, scoped_session
from config import Config

logger = logging.getLogger(__name__)

# Engine configuration
connect_args = {"check_same_thread": False} if "sqlite" in Config.DATABASE_URL else {}
engine = create_engine(
    Config.DATABASE_URL,
    connect_args=connect_args,
    echo=False,
    pool_pre_ping=True
)

# Session factory
SessionFactory = sessionmaker(autocommit=False, autoflush=False, bind=engine)
ScopedSession = scoped_session(SessionFactory)


def init_db():
    """Create all database tables according to registered models."""
    from database.models import Base
    try:
        Base.metadata.create_all(bind=engine)
        logger.info("Database initialized successfully at %s", Config.DATABASE_URL)
    except Exception as e:
        logger.error("Failed to initialize database: %s", str(e))
        raise


@contextmanager
def get_db_session():
    """Context manager for thread-safe transactional database sessions."""
    session = ScopedSession()
    try:
        yield session
        session.commit()
    except Exception as e:
        session.rollback()
        logger.error("Database transaction rolled back: %s", str(e))
        raise
    finally:
        session.close()
