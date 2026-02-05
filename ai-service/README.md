# AI Service

Python-based microservice that provides LLM capabilities for generating personalized learning plans and content.

## Configuration

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `PORT` | `8000` | Server port (FastAPI) |
| `OPENAI_API_KEY` | *(Required)* | OpenAI API Key |
| `OPENAI_MODEL` | `gpt-3.5-turbo` | Model to use for generation |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | Discovery service URL for Registration |
| `KEYCLOAK_INTERNAL_URL` | `http://keycloak:8080` | Internal URL for S2S Keycloak communication |

## Setup

Requires Python 3.9+

```bash
# Install dependencies (incorporates py-eureka-client)
pip install -r requirements.txt

# Run server
uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```

## API Endpoints

### Planning
- `POST /v1/plans` - Generate a new learning plan based on profile/goals
- `POST /v1/plans/adjustments` - Adjust existing plan based on progress

### Assessment
- `POST /v1/assessments/items` - Select next best item for adaptive test
- `POST /v1/assessments/feedback` - Evaluate response and provide AI feedback
- `POST /v1/assessments/diagnostic-tests` - Generate diagnostic test questions

### Content
- `POST /v1/contents/lessons` - Generate lesson content/structure
- `POST /v1/contents/assessment-items` - Generate assessment questions from content

### Content
- `POST /v1/content/generate-lessons` - Generate lesson content/structure

### System
- `GET /health` - Service health status
