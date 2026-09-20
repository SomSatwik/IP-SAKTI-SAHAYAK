"""
IP-SAKTI SAHAYAK
Local Cache & Content Freshness Manager
======================================
Provides local disk/memory caching with content-hash based deduplication,
TTL management, and cache validation for real-time web scraping and queries.
"""

import hashlib
import json
import logging
import time
from pathlib import Path
from typing import Any, Optional
from config import Config

logger = logging.getLogger(__name__)


class CacheManager:
    """Local JSON/Disk cache with TTL and SHA-256 content verification."""

    def __init__(self, cache_dir: Optional[Path] = None):
        self.cache_dir = cache_dir or Config.CACHE_DIR
        self.cache_dir.mkdir(parents=True, exist_ok=True)
        self._memory_cache = {}

    @staticmethod
    def compute_hash(content: str) -> str:
        """Compute SHA-256 hash for content or URL."""
        return hashlib.sha256(content.encode("utf-8", errors="ignore")).hexdigest()

    def _get_cache_path(self, key: str) -> Path:
        safe_key = self.compute_hash(key)
        return self.cache_dir / f"{safe_key}.json"

    def get(self, key: str) -> Optional[Any]:
        """Retrieve item from memory or disk cache if not expired."""
        now = time.time()

        # Check memory first
        if key in self._memory_cache:
            entry = self._memory_cache[key]
            if entry["expires_at"] is None or entry["expires_at"] > now:
                return entry["data"]
            else:
                del self._memory_cache[key]

        # Check disk cache
        cache_file = self._get_cache_path(key)
        if cache_file.exists():
            try:
                with open(cache_file, "r", encoding="utf-8") as f:
                    entry = json.load(f)
                if entry["expires_at"] is None or entry["expires_at"] > now:
                    self._memory_cache[key] = entry
                    return entry["data"]
                else:
                    cache_file.unlink(missing_ok=True)
            except Exception as e:
                logger.warning("Error reading cache file %s: %s", cache_file, str(e))

        return None

    def set(self, key: str, value: Any, ttl_seconds: Optional[int] = 3600 * 24) -> None:
        """Store item in cache with expiration timestamp."""
        now = time.time()
        expires_at = now + ttl_seconds if ttl_seconds else None

        entry = {
            "key": key,
            "created_at": now,
            "expires_at": expires_at,
            "data": value
        }

        # Store in memory
        self._memory_cache[key] = entry

        # Persist to disk
        cache_file = self._get_cache_path(key)
        try:
            with open(cache_file, "w", encoding="utf-8") as f:
                json.dump(entry, f, ensure_ascii=False, indent=2)
        except Exception as e:
            logger.warning("Error persisting cache file %s: %s", cache_file, str(e))

    def invalidate(self, key: str) -> None:
        """Remove item from cache."""
        self._memory_cache.pop(key, None)
        cache_file = self._get_cache_path(key)
        if cache_file.exists():
            cache_file.unlink(missing_ok=True)

    def clear(self) -> None:
        """Clear all cache items."""
        self._memory_cache.clear()
        for f in self.cache_dir.glob("*.json"):
            try:
                f.unlink(missing_ok=True)
            except Exception:
                pass


# Global singleton instance
cache = CacheManager()
