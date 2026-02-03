CREATE TABLE user_profiles (
    user_id       UUID PRIMARY KEY,
    auth_user_id  VARCHAR(255) NOT NULL,
    email         VARCHAR(255) NOT NULL,
    display_name  VARCHAR(120),
    birth_year    INT,
    locale        VARCHAR(10),
    timezone      VARCHAR(50),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_goals (
    id           UUID PRIMARY KEY,
    user_id      UUID NOT NULL,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    domain       VARCHAR(50),
    target_level VARCHAR(50),
    due_date     DATE,
    intensity    VARCHAR(20),  -- 'minimum', 'standard', 'aggressive'
    is_active    BOOLEAN NOT NULL DEFAULT true,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_study_preferences (
    user_id                 UUID PRIMARY KEY,
    hours_per_week          FLOAT,
    preferred_days          TEXT[], -- Array of strings
    preferred_session_min   INT,
    content_format_priority JSONB,
    notifications_enabled   BOOLEAN DEFAULT true,
    raw_preferences         JSONB,
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- opcional, si quieres FK directa al perfil
-- ALTER TABLE user_goals
--   ADD CONSTRAINT fk_user_goals_user
--   FOREIGN KEY (user_id) REFERENCES user_profiles(user_id);


CREATE TABLE badges (
    id            UUID PRIMARY KEY,
    code          VARCHAR(100) NOT NULL UNIQUE,
    name          VARCHAR(200) NOT NULL,
    description   TEXT,
    domain        VARCHAR(50),
    level         VARCHAR(50),
    icon_url      VARCHAR(500),
    criteria_type VARCHAR(50),
    criteria_meta JSONB,
    is_active     BOOLEAN NOT NULL DEFAULT true,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);


CREATE TABLE user_badges (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL,
    badge_id    UUID NOT NULL REFERENCES badges(id),
    awarded_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    source      VARCHAR(50),
    reason      TEXT,
    metadata    JSONB,
    CONSTRAINT uq_user_badge UNIQUE (user_id, badge_id)
    -- opcional: FK a user_profiles
    -- ,CONSTRAINT fk_user_badges_user
    --  FOREIGN KEY (user_id) REFERENCES user_profiles(user_id)
);

