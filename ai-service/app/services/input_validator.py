import re
from fastapi import HTTPException

# Keywords often used in Jailbreak attempts
BLOCKED_PHRASES = [
    "ignore previous instructions",
    "system prompt",
    "you are a large language model",
    "dan mode",
    "developer mode",
    "act as a",
]

# Max length for free-text fields to prevent token exhaustion or buffer overflows
MAX_TEXT_LENGTH = 2000

class InputValidator:
    """
    Sanitizes and validates user input to prevent Prompt Injection attacks.
    """

    @staticmethod
    def validate_text(text: str, context: str = "input") -> str:
        """
        Validates a single string.
        Raises HTTPException if blocked phrases are found or length is exceeded.
        Returns the sanitized text.
        """
        if not text:
            return ""

        if len(text) > MAX_TEXT_LENGTH:
             raise HTTPException(status_code=400, detail=f"{context} exceeds maximum length of {MAX_TEXT_LENGTH} characters.")

        lower_text = text.lower()
        for phrase in BLOCKED_PHRASES:
            if phrase in lower_text:
                # Log security event here in a real system
                print(f"SECURITY ALERT: Blocked phrase '{phrase}' detected in {context}.")
                raise HTTPException(status_code=400, detail="Input contains prohibited content.")

        # Basic sanitization: strip XML-like tags to prevent interfering with our own delimiters
        # We replace < and > with HTML entities or just remove them if strictly text
        # For now, let's just escape them to be safe.
        sanitized = text.replace("<", "&lt;").replace(">", "&gt;")
        
        return sanitized

    @staticmethod
    def validate_obj(obj: any, depth=0):
        """
        Recursively validates strings within a dictionary or list.
        """
        if depth > 5: # Prevent infinite recursion
            return obj

        if isinstance(obj, str):
            return InputValidator.validate_text(obj)
        elif isinstance(obj, list):
            return [InputValidator.validate_obj(item, depth + 1) for item in obj]
        elif isinstance(obj, dict):
            return {k: InputValidator.validate_obj(v, depth + 1) for k, v in obj.items()}
        else:
            return obj
