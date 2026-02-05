from fastapi import FastAPI, HTTPException
from contextlib import asynccontextmanager
import py_eureka_client.eureka_client as eureka_client
import os
import asyncio

@asynccontextmanager
async def lifespan(app: FastAPI):
    # Retrieve env vars for Eureka
    eureka_server = os.getenv("EUREKA_URL", "http://eureka:8761/eureka/")
    instance_port = int(os.getenv("PORT", 8000))
    app_name = "ai-service"
    
    # Start Eureka Client
    print(f"Stats Eureka Client: {eureka_server} for {app_name}:{instance_port}")
    await eureka_client.init_async(
        eureka_server=eureka_server,
        app_name=app_name,
        instance_port=instance_port
    )
    yield
    # Stop Eureka Client
    print("Stopping Eureka Client")
    await eureka_client.stop_async()

from pydantic import BaseModel
from typing import List, Optional, Dict, Any
import uuid
from app.services.llm_service import llm_service
from app.services.input_validator import InputValidator

app = FastAPI(title="AI Service", lifespan=lifespan)

# --- Models ---
# Using generic Dict for complex nested objects to stay flexible with OAS changes
# in a real implementation, we would duplicate Pydantic models from OAS.

class GeneratePlanRequest(BaseModel):
    userId: str
    profile: Dict[str, Any]
    goals: List[Dict[str, Any]]
    studyPreferences: Optional[Dict[str, Any]] = None
    contentCatalog: List[Dict[str, Any]] = []

class GeneratePlanResponse(BaseModel):
    plan: Dict[str, Any]
    rawModelOutput: Dict[str, Any] = {}

class ReplanRequest(BaseModel):
    userId: str
    currentPlan: Dict[str, Any]
    recentEvents: List[Dict[str, Any]] = []
    updatedSkillState: List[Dict[str, Any]] = []

class ReplanResponse(BaseModel):
    plan: Dict[str, Any]
    changeSummary: str

class NextItemRequest(BaseModel):
    userId: str
    domain: str
    skillState: List[Dict[str, Any]] = []
    recentHistory: List[Dict[str, Any]] = []
    excludeItemIds: List[str] = []

class NextItemResponse(BaseModel):
    item: Dict[str, Any]
    rationale: str

class FeedbackRequest(BaseModel):
    userId: str
    item: Dict[str, Any]
    userResponse: Dict[str, Any]
    skillStateBefore: List[Dict[str, Any]] = []

class FeedbackResponse(BaseModel):
    isCorrect: bool
    feedbackMessage: str
    remediationSuggestions: List[str] = []

class GenerateDiagnosticTestRequest(BaseModel):
    domain: str
    level: str = "BEGINNER"
    nQuestions: int = 5

class GenerateDiagnosticTestResponse(BaseModel):
    questions: List[Dict[str, Any]]

# --- Endpoints ---

@app.get("/health")
def health():
    return {"status": "ok", "provider": llm_service.model if llm_service.client else "mock"}

@app.post("/v1/plans", response_model=GeneratePlanResponse)
def generate_plan(request: GeneratePlanRequest):
    try:
        # Serialize for validation (simple approach) or validate specific fields
        # Ideally, we validate the dict structure recursively
        val_profile = InputValidator.validate_obj(request.profile)
        val_goals = InputValidator.validate_obj(request.goals)
        val_catalog = InputValidator.validate_obj(request.contentCatalog)

        result = llm_service.generate_plan(
            user_profile=val_profile,
            goals=val_goals,
            content_catalog=val_catalog
        )
        return GeneratePlanResponse(
            plan=result.get("plan", {}),
            rawModelOutput=result.get("rawModelOutput", {})
        )
    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/v1/plans/adjustments", response_model=ReplanResponse)
def replan(request: ReplanRequest):
    try:
        val_plan = InputValidator.validate_obj(request.currentPlan)
        val_events = InputValidator.validate_obj(request.recentEvents)
        val_skill = InputValidator.validate_obj(request.updatedSkillState)

        result = llm_service.replan(
            current_plan=val_plan,
            recent_events=val_events,
            skill_state=val_skill
        )
        return ReplanResponse(
            plan=result.get("plan", request.currentPlan),
            changeSummary=result.get("changeSummary", "No changes applied.")
        )
    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/v1/assessments/items", response_model=NextItemResponse)
def next_item(request: NextItemRequest):
    try:
        # Heuristic: Calculate average mastery from skillState for prompt context
        mastery = 0.5
        if request.skillState:
             mastery = sum([s.get("mastery", 0.5) for s in request.skillState]) / len(request.skillState)

        val_domain = InputValidator.validate_text(request.domain)
        val_history = InputValidator.validate_obj(request.recentHistory)

        result = llm_service.generate_next_item(
            domain=val_domain,
            mastery=mastery,
            recent_history=val_history,
            exclude_item_ids=request.excludeItemIds
        )
        return NextItemResponse(
            item=result.get("item", {}),
            rationale=result.get("rationale", "")
        )
    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/v1/assessments/feedback", response_model=FeedbackResponse)
def feedback(request: FeedbackRequest):
    try:
        item = request.item
        # ... existing logic ...
        response = request.userResponse
        
        # Extract basic info safely
        stem = item.get("stem", "Question")
        # Assuming multiple choice for simplicity in finding correct answer
        options = item.get("options", [])
        correct_opt = next((o for o in options if o.get("isCorrect")), None)
        correct_stmt = correct_opt.get("statement") if correct_opt else "Unknown"
        
        user_sel_id = response.get("selectedOptionId")
        user_opt = next((o for o in options if o.get("optionId") == user_sel_id), None)
        user_stmt = user_opt.get("statement") if user_opt else str(response.get("openAnswer", ""))
        
        # Determine correctness (if not already simulated by frontend, AI validates it)
        is_correct = (user_sel_id == correct_opt.get("optionId")) if correct_opt and user_sel_id else False

        # Validate text inputs
        val_stem = InputValidator.validate_text(stem, "item_stem")
        val_correct = InputValidator.validate_text(correct_stmt, "correct_answer")
        val_user = InputValidator.validate_text(user_stmt, "user_answer")

        result = llm_service.generate_feedback(
            item_stem=val_stem,
            correct_answer=val_correct,
            user_answer=val_user,
            is_correct=is_correct
        )
        
        return FeedbackResponse(
            isCorrect=result.get("isCorrect", is_correct),
            feedbackMessage=result.get("feedbackMessage", ""),
            remediationSuggestions=result.get("remediationSuggestions", [])
        )
    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

class GenerateDiagnosticTestRequest(BaseModel):
    domain: str
    level: str = "BEGINNER"
    nQuestions: int = 5

class GenerateDiagnosticTestResponse(BaseModel):
    questions: List[Dict[str, Any]]

@app.post("/v1/assessments/diagnostic-tests", response_model=GenerateDiagnosticTestResponse)
def generate_diagnostic_test(request: GenerateDiagnosticTestRequest):
    try:
        val_domain = InputValidator.validate_text(request.domain)
        result = llm_service.generate_diagnostic_test(
            domain=val_domain,
            level=request.level,
            n_questions=request.nQuestions
        )
        return GenerateDiagnosticTestResponse(questions=result.get("questions", []))
    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

class GenerateContentRequest(BaseModel):
    domain: str # Code or Name
    skillIds: Optional[List[str]] = None
    nLessons: int = 3
    level: str = "beginner"
    difficulty: Optional[float] = 0.5
    locale: str = "es-ES"

class GenerateContentResponse(BaseModel):
    lessons: List[Dict[str, Any]]

@app.post("/v1/contents/lessons", response_model=GenerateContentResponse)
def generate_lessons(request: GenerateContentRequest):
    try:
        val_domain = InputValidator.validate_text(request.domain)
        val_locale = InputValidator.validate_text(request.locale)

        result = llm_service.generate_lessons(
            domain=val_domain,
            n_lessons=request.nLessons,
            locale=val_locale
        )
        return GenerateContentResponse(lessons=result.get("lessons", []))
    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

class GenerateAssessmentItemsRequest(BaseModel):
    domain: str
    skillIds: Optional[List[str]] = None
    nItems: int = 5
    itemType: str = "multiple_choice"
    difficultyRange: Optional[Dict[str, float]] = None
    locale: str = "es-ES"

class GenerateAssessmentItemsResponse(BaseModel):
    items: List[Dict[str, Any]]

@app.post("/v1/contents/assessment-items", response_model=GenerateAssessmentItemsResponse)
def generate_assessment_items(request: GenerateAssessmentItemsRequest):
    try:
        val_domain = InputValidator.validate_text(request.domain)
        
        # MOCK IMPLEMENTATION (Should call llm_service)
        # Assuming llm_service has a method or using generic mock for now
        # Ideally we add generate_assessment_items to llm_service.py
        
        # Calling a hypothetical method (that we will add/ensure exists)
        # result = llm_service.generate_items(...)
        
        # For now, inline mock or we update llm_service next.
        # Let's return a mock structure.
        
        items = []
        for i in range(request.nItems):
            items.append({
                "tempId": str(uuid.uuid4()),
                "domain": request.domain,
                "type": request.itemType,
                "stem": f"Generated question {i+1} for {request.domain}",
                "options": [
                    {"optionId": "a", "statement": "Option A (Correct)", "isCorrect": True},
                    {"optionId": "b", "statement": "Option B", "isCorrect": False}
                ],
                "difficulty": 0.5
            })
            
        return GenerateAssessmentItemsResponse(items=items)

    except HTTPException as e:
        raise e
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)


