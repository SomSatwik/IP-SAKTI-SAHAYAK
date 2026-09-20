"""
IP-SAKTI SAHAYAK
Groq LLM Client & Inference Engine
==================================
Wrapper for Groq API with deterministic parameters, automated retries,
model fallback, and comprehensive exception handling.
"""

import logging
import time
from typing import Optional, Dict, Any
from groq import Groq
from config import Config
from ai.prompts import LEGAL_SYSTEM_PROMPT

logger = logging.getLogger(__name__)


class GroqLLMClient:
    """Client for Groq Cloud API with model fallback and resilience."""

    def __init__(self):
        self.api_key = Config.GROQ_API_KEY
        self.model = Config.GROQ_MODEL
        self.fallback_models = ["openai/gpt-oss-120b", "openai/gpt-oss-20b", "qwen/qwen3.8-27b", "groq/compound"]
        self.client = None
        self._init_client()

    def _init_client(self):
        """Initialize the Groq client."""
        if not self.api_key or self.api_key.startswith("your_"):
            logger.warning("Groq API key not set or invalid.")
            self.client = None
            return

        try:
            self.client = Groq(api_key=self.api_key)
            logger.info("Groq client initialized successfully with model %s", self.model)
        except Exception as e:
            logger.error("Failed to initialize Groq client: %s", str(e))
            self.client = None

    def is_available(self) -> bool:
        """Check if Groq client is configured."""
        return self.client is not None

    def generate_response(
        self,
        prompt: str,
        system_prompt: Optional[str] = None,
        temperature: Optional[float] = None,
        max_tokens: Optional[int] = None
    ) -> Dict[str, Any]:
        """
        Invoke Groq LLM with system instructions and user prompt.
        Returns dict with status, content, model, and latency.
        """
        if not self.client:
            return {
                "success": False,
                "error": "Groq client is not initialized. Please verify your GROQ_API_KEY in .env.",
                "content": "Service temporarily unavailable. Please check system configuration."
            }

        sys_instruction = system_prompt or LEGAL_SYSTEM_PROMPT
        temp = temperature if temperature is not None else Config.GROQ_TEMPERATURE
        max_t = max_tokens or Config.GROQ_MAX_TOKENS

        models_to_try = [self.model] + [m for m in self.fallback_models if m != self.model]
        start_time = time.time()

        for current_model in models_to_try:
            try:
                response = self.client.chat.completions.create(
                    model=current_model,
                    messages=[
                        {"role": "system", "content": sys_instruction},
                        {"role": "user", "content": prompt}
                    ],
                    temperature=temp,
                    max_tokens=max_t,
                    stream=False
                )

                latency_ms = int((time.time() - start_time) * 1000)
                content = response.choices[0].message.content

                return {
                    "success": True,
                    "content": content,
                    "model_used": current_model,
                    "latency_ms": latency_ms,
                    "prompt_tokens": response.usage.prompt_tokens if response.usage else 0,
                    "completion_tokens": response.usage.completion_tokens if response.usage else 0
                }

            except Exception as e:
                logger.warning("Groq API error on model %s: %s", current_model, str(e))
                time.sleep(0.5)

        latency_ms = int((time.time() - start_time) * 1000)
        return {
            "success": False,
            "error": "Failed to generate LLM response across available models.",
            "content": "An error occurred while contacting the Groq AI service. Please try again shortly.",
            "latency_ms": latency_ms
        }


# Global singleton instance
groq_client = GroqLLMClient()
