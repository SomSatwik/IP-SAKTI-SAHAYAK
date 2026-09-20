"""
Routes package for IP-SAKTI SAHAYAK.
"""
from .chat_routes import chat_bp
from .search_routes import search_bp
from .source_routes import source_bp
from .admin_routes import admin_bp

__all__ = ["chat_bp", "search_bp", "source_bp", "admin_bp"]
