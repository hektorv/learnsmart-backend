-- US-094: Create audit log table for user profile changes
-- Migration: V3__create_audit_log_table.sql

CREATE TABLE user_audit_log (
    id UUID PRIMARY KEY DEFAULT RANDOM_UUID(),
    user_id UUID NOT NULL,
    performed_by UUID NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id UUID NOT NULL,
    action VARCHAR(20) NOT NULL,
    field_name VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    ip_address VARCHAR(45),
    user_agent TEXT,
    
    CONSTRAINT chk_entity_type CHECK (entity_type IN ('PROFILE', 'GOAL', 'PREFERENCES')),
    CONSTRAINT chk_action CHECK (action IN ('CREATE', 'UPDATE', 'DELETE'))
);

-- Indexes for efficient querying
CREATE INDEX idx_audit_user_timestamp ON user_audit_log(user_id, timestamp DESC);
CREATE INDEX idx_audit_entity ON user_audit_log(entity_type, entity_id, timestamp DESC);
CREATE INDEX idx_audit_performed_by ON user_audit_log(performed_by, timestamp DESC);
CREATE INDEX idx_audit_timestamp ON user_audit_log(timestamp DESC);

-- Comments for documentation
COMMENT ON TABLE user_audit_log IS 'Audit trail for all profile, goal, and preferences changes (US-094)';
COMMENT ON COLUMN user_audit_log.user_id IS 'User who owns the entity being modified';
COMMENT ON COLUMN user_audit_log.performed_by IS 'User who performed the action (may differ for admin actions)';
COMMENT ON COLUMN user_audit_log.entity_type IS 'Type of entity: PROFILE, GOAL, or PREFERENCES';
COMMENT ON COLUMN user_audit_log.action IS 'Action performed: CREATE, UPDATE, or DELETE';
COMMENT ON COLUMN user_audit_log.field_name IS 'For UPDATE actions, the specific field that changed';
COMMENT ON COLUMN user_audit_log.old_value IS 'JSON snapshot of the value before the change';
COMMENT ON COLUMN user_audit_log.new_value IS 'JSON snapshot of the value after the change';
