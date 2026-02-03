# 📘 EPIC 8 — Technical Refinement & Gap Closure
**System:** LearnSmart
**Scope:** Address technical debt and missing features identified during validation.

---

## US-080 | Implement Real AI Replanning (Fix Stub)

**As** a system,
**I want** to replace the mock implementation in `replan` logic,
**so that** the user receives a genuinely updated plan from the AI Service.

### Context
Currently, `LearningPlanServiceImpl.replan()` saves a history record but sets the AI response to a static mock: `{"mock": "replanned"}`.

### Tasks
1. Update `LearningPlanServiceImpl.replan` to build a real `ReplanRequest`.
2. Call `AiClient.replan(request)`.
3. Process the `ReplanResponse` to actually modify the `LearningPlan` (add/remove modules, change dates).

### Acceptance Criteria
- `replan()` invokes `ai-service` endpoint `/v1/plans/adjustments`.
- The returned plan structure is applied to the entity.
- Tests verify that a change in constraints actually alters the plan.

---

## US-081 | Explicit Skill Prerequisites Management

**As** an admin,
**I want** an explicit endpoint to manage skill dependencies,
**so that** I can visualize and modify the prerequisite graph directly.

### Context
US-013 was marked as partially implemented. `SkillController` lacks specific endpoints for prerequisites (`GET/PUT /skills/{id}/prerequisites`), relying potentially on generic updates.

### Tasks
1. Add `GET /skills/{id}/prerequisites` to `SkillController`.
2. Add `PUT /skills/{id}/prerequisites` (or `POST`) to manage links.
3. Validate cycles (graph loop detection) in `SkillService`.

### Acceptance Criteria
- Endpoints documented in OAS.
- Cycle detection throws `400 Bad Request`.

---

## US-082 | OAS & DDL Synchronization

**As** a developer,
**I want** to ensure the code matches the documentation (OAS/DDL),
**so that** the API contract remains truthful.

### Context
User reports that OAS and DDL files exist in `docs/` folders of services. We need to verify our current implementation against them.

### Tasks
1. Compare `profile-service/docs/openapi.yaml` vs `ProfileController`.
2. Compare `content-service/docs/openapi.yaml` vs `DomainController/SkillController`.
3. Compare `planning-service/docs/openapi.yaml` vs `LearningPlanController`.
4. Ensure generic DDLs match JPA entities.

### Acceptance Criteria
- All controllers cover the definitions in OAS.
- Any discrepancy is resolved (either update code or update OAS).

---

## US-083 | Adaptive Assessment (Real AI)

**As** a student,
**I want** the next question to be selected by IA based on my real-time performance,
**so that** the difficulty adjusts dynamically.

### Context
`AssessmentSessionServiceImpl.getNextItem()` currently picks a random active item.

### Tasks
1. Update `getNextItem` to call `AiClient.getNextItem(history, currentMastery)`.
2. Ensure fallback if AI fails (continue with random or heuristic).

---

## US-084 | AI Feedback Generation

**As** a student,
**I want** explanations for my mistakes,
**so that** I can learn from them immediately.

### Context
`AssessmentSessionServiceImpl.submitResponse()` uses static feedback ("Correct"/"Incorrect").

### Tasks
1. Update `submitResponse`: if answer is incorrect, call `AiClient.generateFeedback()`.
2. Store the AI feedback in `UserItemResponse`.

---

## US-085 | Strict AI Configuration (Fail Fast)

**As** a DevOps engineer,
**I want** the service to fail fast if the OpenAI API Key is missing in production,
**so that** we don't accidentally run with mock data in real environments.

### Context
Currently, `LLMService` falls back to `_mock_plan` silently if the key is missing. This violates the "Fail Fast" principle.

### Tasks
1. Modify `config.py` / `LLMService`: If `ENVIRONMENT != 'test'` and `OPENAI_API_KEY` is missing, raise a critical startup error.
2. Allow Mock mode *only* if explicitly enabled via a flag (e.g., `USE_MOCK_AI=true`).

---

## US-086 | Async Event Tracking

**As** the platform,
**I want** event recording to be non-blocking,
**so that** user experience is not degraded by analytics writes.

### Context
`TrackingService.createEvent` is currently synchronous. US-040 requires async storage.

### Tasks
1. Enable `@EnableAsync` in Tracking Service.
2. Annotate `createEvent` with `@Async` (or move repo call to an async handler).

---

## US-087 | Skill Dependency Cycle Detection

**As** an admin,
**I want** the system to prevent circular prerequisites,
**so that** the learning path graph remains valid (DAG).

### Context
`SkillServiceImpl.updatePrerequisites` saves dependencies without validation. US-013 requires cycle detection.

### Tasks
1. Implement a DFS/BFS check in `updatePrerequisites` before saving.
2. Return `400 Bad Request` if cycle detected.

---

## US-088 | Domain Status Filtering

**As** a user,
**I want** to see only published domains,
**so that** I don't access draft content.

### Context
`DomainServiceImpl.findAll` returns all records. US-010 requires filtering by `status='published'`.

### Tasks
1. Add `status` field to `Domain` entity (if missing) or use existing.
2. Update `findAll` to filter by status by default for public API.
