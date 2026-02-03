
import os
from dotenv import load_dotenv

load_dotenv()

class Settings:
    PROJECT_NAME: str = "AI Service"
    VERSION: str = "1.0.0"
    ENVIRONMENT: str = os.getenv("ENVIRONMENT", "development")
    USE_MOCK_AI: bool = os.getenv("USE_MOCK_AI", "false").lower() == "true"
    OPENAI_API_KEY: str = os.getenv("OPENAI_API_KEY", "")
    OPENAI_MODEL: str = os.getenv("OPENAI_MODEL", "gpt-3.5-turbo")

    def __init__(self):
        # US-085: Fail Fast if Key is missing in non-test env without explicit Mock flag
        if self.ENVIRONMENT != "test" and not self.USE_MOCK_AI:
            if not self.OPENAI_API_KEY:
                raise ValueError(
                    "CRITICAL: OPENAI_API_KEY is missing. "
                    "Set USE_MOCK_AI=true to enable mock mode, or provide a valid key."
                )

settings = Settings()
