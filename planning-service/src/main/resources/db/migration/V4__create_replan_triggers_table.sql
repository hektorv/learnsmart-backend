-- US-107: Create replan triggers table for automatic replanning detection
-- Migration: V4__create_replan_triggers_table.sql

CREATE TABLE replan_triggers (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    plan_id UUID NOT NULL,
    trigger_type VARCHAR(50) NOT NULL,
    trigger_reason TEXT NOT NULL,
    severity VARCHAR(20) NOT NULL,
    detected_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    evaluated_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    metadata TEXT,
    
    CONSTRAINT fk_trigger_plan FOREIGN KEY (plan_id) 
        REFERENCES learning_plans(id) ON DELETE CASCADE,
    CONSTRAINT chk_trigger_type CHECK (trigger_type IN 
        ('PROGRESS_DEVIATION', 'MASTERY_CHANGE', 'INACTIVITY', 'GOAL_UPDATED')),
    CONSTRAINT chk_severity CHECK (severity IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT chk_status CHECK (status IN 
        ('PENDING', 'SUGGESTED', 'DISMISSED', 'EXECUTED'))
);

-- Indexes for efficient querying
CREATE INDEX idx_trigger_plan_status ON replan_triggers(plan_id, status);
CREATE INDEX idx_trigger_status_detected ON replan_triggers(status, detected_at DESC);

-- Add trigger_id to plan_replan_history to link automatic replans
ALTER TABLE plan_replans_history ADD COLUMN trigger_id UUID;
ALTER TABLE plan_replans_history ADD CONSTRAINT fk_replan_trigger 
    FOREIGN KEY (trigger_id) REFERENCES replan_triggers(id) ON DELETE SET NULL;

-- Comments for documentation
COMMENT ON TABLE replan_triggers IS 'Automatic replanning triggers based on progress, mastery, and activity (US-107)';
COMMENT ON COLUMN replan_triggers.trigger_type IS 'Type of trigger: PROGRESS_DEVIATION, MASTERY_CHANGE, INACTIVITY, or GOAL_UPDATED';
COMMENT ON COLUMN replan_triggers.severity IS 'Trigger severity: LOW, MEDIUM, or HIGH';
COMMENT ON COLUMN replan_triggers.status IS 'Trigger status: PENDING, SUGGESTED, DISMISSED, or EXECUTED';
COMMENT ON COLUMN replan_triggers.metadata IS 'JSON metadata with trigger-specific details';
