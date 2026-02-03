# 📘 EPIC 3 — Adaptive Learning Planning  
**System:** LearnSmart  
**Base microservices:** planning-service, ai-service

---

## US-020 | Generate Initial Learning Plan

**As** a student,  
**I want** to generate an initial AI-driven personalized learning plan,  
**so that** I have a structured path with modules and activities adapted to my goals and level.

### Endpoints
`POST /plans`  
`POST /v1/plans/generate`

### Acceptance Criteria

- The student can request a plan by specifying learning goals.
- The system invokes the AI engine to generate the plan.
- The generated plan contains sequentially ordered modules.
- Each module contains activities with specific content.
- The plan considers skill prerequisites.
- The plan considers the student’s current level:
  - based on a diagnostic assessment.
- The plan includes:
  - total estimated time
  - estimated time per module.
- The plan is stored and associated with the user with status `active`.
- Plan generation time is under **10 seconds**.

---

## US-021 | Adaptive Replanning

**As** a student,  
**I want** my learning plan to be automatically replanned when my progress changes,  
**so that** my learning path remains optimal and up to date.

### Endpoints
`POST /plans/{planId}/replan`  
`POST /v1/plans/replan`

### Acceptance Criteria

- Replanning is automatically triggered when:
  - the student completes a module
  - the student repeatedly fails an assessment (3+ attempts)
  - more than 7 days pass without activity
  - the student adds or modifies learning goals.
- The system invokes the AI engine with updated context.
- A new plan is generated preserving existing progress.
- Completed activities are not repeated:
  - unless explicit reinforcement is required.
- The user is notified when the plan is updated.
- A history of previous plans is preserved.
- The student can reject the replanning and keep the current plan.

---

## US-022 | View Plan Modules

**As** a student,  
**I want** to view the modules of my current learning plan,  
**so that** I can understand the full structure of my learning path and my progress.

### Endpoint
`GET /plans/{planId}/modules`

### Acceptance Criteria

- Returns the authenticated user’s active plan.
- Includes a list of modules with:
  - name
  - description
  - order
  - status (`pending`, `in_progress`, `completed`).
- Each module includes its activities with individual status.
- Displays:
  - progress percentage per module
  - total plan progress.
- Indicates the current module and next activity to complete.
- Includes estimated completion dates.
- If the user has no active plan:
  - returns `HTTP 404 Not Found`
  - with a message suggesting plan creation.

---

## US-023 | Update Activity Status

**As** a student,  
**I want** to update the status of an activity,  
**so that** my progress in the learning plan is properly recorded.

### Endpoint
`PATCH /plans/{planId}/activities/{activityId}`

### Acceptance Criteria

- The student can mark an activity as `completed`.
- The completion date and time are recorded.
- Time spent on the activity is recorded (if available).
- When the last activity of a module is completed:
  - the module is marked as `completed`.
- When the last module is completed:
  - the plan is marked as `completed`.
- A tracking event is generated for analytics.
- The system evaluates whether adaptive replanning should be triggered.
- Returns the updated state of the activity and its module.

---
