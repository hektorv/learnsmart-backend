DROP TABLE IF EXISTS plan_replans_history;
DROP TABLE IF EXISTS replan_triggers;
DROP TABLE IF EXISTS plan_activities;
DROP TABLE IF EXISTS plan_modules;
DROP TABLE IF EXISTS certificates;
DROP TABLE IF EXISTS learning_plans;

CREATE TABLE IF NOT EXISTS learning_plans (
    id              UUID PRIMARY KEY,
    user_id         VARCHAR(50) NOT NULL,
    goal_id         VARCHAR(50),
    status          VARCHAR(20) NOT NULL DEFAULT 'active',
    start_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    end_date        DATE,
    hours_per_week  NUMERIC(4,1),
    generated_by    VARCHAR(20) NOT NULL DEFAULT 'ai',
    raw_plan_ai     TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS plan_modules (
    id              UUID PRIMARY KEY,
    plan_id         UUID NOT NULL REFERENCES learning_plans(id) ON DELETE CASCADE,
    position        INT NOT NULL,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    estimated_hours NUMERIC(5,2),
    status          VARCHAR(20) NOT NULL DEFAULT 'pending',
    target_skills   TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (plan_id, position)
);

CREATE TABLE IF NOT EXISTS plan_activities (
    id                      UUID PRIMARY KEY,
    module_id               UUID NOT NULL REFERENCES plan_modules(id) ON DELETE CASCADE,
    position                INT NOT NULL,
    activity_type           VARCHAR(30) NOT NULL,
    status                  VARCHAR(20) NOT NULL DEFAULT 'pending',
    content_ref             TEXT NOT NULL,
    estimated_minutes       INT,
    override_estimated_minutes INT,
    started_at              TIMESTAMPTZ,
    completed_at            TIMESTAMPTZ,
    actual_minutes_spent    INT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (module_id, position)
);

CREATE TABLE IF NOT EXISTS plan_replans_history (
    id              UUID PRIMARY KEY,
    plan_id         UUID NOT NULL REFERENCES learning_plans(id) ON DELETE CASCADE,
    reason          TEXT,
    request_payload TEXT,
    response_payload TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS certificates (
    id              UUID PRIMARY KEY,
    user_id         UUID NOT NULL,
    plan_id         UUID NOT NULL UNIQUE REFERENCES learning_plans(id) ON DELETE CASCADE,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    issued_at       TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS replan_triggers (
    id UUID PRIMARY KEY,
    plan_id UUID NOT NULL REFERENCES learning_plans(id) ON DELETE CASCADE,
    trigger_type VARCHAR(50) NOT NULL,
    trigger_reason TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    detected_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    evaluated_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    metadata TEXT
);

CREATE INDEX IF NOT EXISTS idx_trigger_plan_status ON replan_triggers(plan_id, status);
CREATE INDEX IF NOT EXISTS idx_trigger_status_detected ON replan_triggers(status, detected_at DESC);
