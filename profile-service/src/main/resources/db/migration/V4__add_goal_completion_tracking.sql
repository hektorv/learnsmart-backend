-- US-096: Add completion tracking to user goals
-- Migration: V4__add_goal_completion_tracking.sql

ALTER TABLE user_goals ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE;
ALTER TABLE user_goals ADD COLUMN completion_percentage INTEGER DEFAULT 0;
ALTER TABLE user_goals ADD COLUMN status VARCHAR(20) DEFAULT 'active';

-- Constraints
ALTER TABLE user_goals ADD CONSTRAINT chk_completion_percentage 
    CHECK (completion_percentage >= 0 AND completion_percentage <= 100);
ALTER TABLE user_goals ADD CONSTRAINT chk_status 
    CHECK (status IN ('active', 'in_progress', 'completed'));

-- Indexes for efficient querying
CREATE INDEX idx_goal_status ON user_goals(status);
CREATE INDEX idx_goal_completed_at ON user_goals(completed_at);

-- Comments for documentation
COMMENT ON COLUMN user_goals.completed_at IS 'Goal completion timestamp (US-096)';
COMMENT ON COLUMN user_goals.completion_percentage IS 'Goal progress 0-100% (US-096)';
COMMENT ON COLUMN user_goals.status IS 'Goal lifecycle status: active, in_progress, completed (US-096)';
