# AI Service

Python-based microservice that provides LLM capabilities for generating personalized learning plans and content.

## Configuration

| Environment Variable | Default | Description |
|----------------------|---------|-------------|
| `PORT` | `8001` | Server port (FastAPI) |
| `OPENAI_API_KEY` | *(Required)* | OpenAI API Key |
| `OPENAI_MODEL` | `gpt-3.5-turbo` | Model to use for generation |

## Setup

Requires Python 3.9+

```bash
# Install dependencies
pip install -r requirements.txt

# Run server
uvicorn app.main:app --host 0.0.0.0 --port 8001 --reload
```

## API Endpoints

### Planning
- `POST /v1/plans` - Generate a new learning plan based on profile/goals
- `POST /v1/plans/adjustments` - Adjust existing plan based on progress

### Assessment
- `POST /v1/assessment/next-item` - Select next best item for adaptive test
- `POST /v1/assessment/feedback` - Evaluate response and provide AI feedback
- `POST /v1/assessment/diagnostic` - Generate diagnostic test questions

### Content
- `POST /v1/content/generate-lessons` - Generate lesson content/structure

### System
- `GET /health` - Service health status
