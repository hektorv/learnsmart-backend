# 📘 EPIC 6 — Artificial Intelligence Engine  
**System:** LearnSmart  
**Used by:** planning-service, assessment-service  
**Role:** AI coprocessor (not a standalone business service)

---

## US-050 | AI Plan Generation

**As** the planning service,  
**I want** to invoke the AI engine to generate a personalized learning plan from scratch,  
**so that** structured modules and activities are created based on the student profile.

### Endpoint
`POST /v1/plans/generate`

### Acceptance Criteria

- Receives as input:
  - student profile
  - learning goals
  - current skill levels
  - available study time.
- Generates a structured learning plan with modules and activities.
- Each module includes:
  - name
  - description
  - target skills
  - estimated duration.
- Each activity includes:
  - type
  - associated content
  - order
  - estimated duration.
- Respects skill prerequisites.
- Prioritizes according to student goals.
- The plan is pedagogically coherent:
  - logical sequencing.
- Response time is under **8 seconds**.
- Returns structured JSON according to the defined schema.

---

## US-051 | AI Replanning

**As** the planning service,  
**I want** to invoke the AI engine to dynamically replan a learning path,  
**so that** the plan is coherently adjusted based on changes in student progress.

### Endpoint
`POST /v1/plans/replan`

### Acceptance Criteria

- Receives as input:
  - current plan
  - progress (completed activities)
  - recent events
  - new or updated goals.
- Generates a new plan incorporating existing progress.
- Identifies activities to:
  - add
  - remove
  - reorder.
- Does not repeat content already successfully completed.
- Adds reinforcement for areas where the student struggled.
- Maintains coherence with original and new goals.
- Response time is under **5 seconds**.
- Includes an explanation of the changes made.

---

## US-052 | AI Adaptive Item Selection

**As** the assessment service,  
**I want** to invoke the AI engine to select the next adaptive item,  
**so that** questions are chosen in alignment with the student’s current performance.

### Endpoint
`POST /v1/assessments/next-item`

### Acceptance Criteria

- Receives as input:
  - evaluated skill
  - session response history
  - current estimated level
  - items already presented.
- Selects the optimal item using a CAT (Computerized Adaptive Testing) algorithm.
- The selected item provides maximum information at the estimated level.
- Avoids items already answered in the session.
- Ensures balance across subtopics within the skill.
- Response time is under **500 ms**.
- Returns:
  - selected item ID
  - difficulty
  - selection justification.
- If no items are available:
  - indicates assessment completion.

---

## US-053 | AI Feedback Generation

**As** the assessment service,  
**I want** to invoke the AI engine to generate pedagogical feedback,  
**so that** clear, correct, and personalized explanations are provided in natural language.

### Endpoint
`POST /v1/assessments/feedback`

### Acceptance Criteria

- Receives as input:
  - question
  - student answer
  - correct answer
  - student context.
- Generates an explanation of the underlying concept.
- Explains why the given answer was incorrect (if applicable).
- Provides a hint or alternative way of understanding.
- Tone is empathetic, motivating, and pedagogical.
- Adapted to the student’s level:
  - appropriate vocabulary.
- Appropriate length:
  - 50–200 words.
- Generation time is under **2 seconds**.
- Supports follow-up requests such as:
  - “explain in another way”.

---
