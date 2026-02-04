-- US-110: Add activity completion timestamps
-- Migration: V2__add_activity_timestamps.sql

ALTER TABLE plan_activities 
ADD COLUMN started_at TIMESTAMP WITH TIME ZONE,
ADD COLUMN completed_at TIMESTAMP WITH TIME ZONE,
ADD COLUMN actual_minutes_spent INTEGER;
