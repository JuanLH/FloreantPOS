-- Schema Migration: Add Online_Ordering_turnedOn and online_ordering_last_heartbeat columns to RESTAURANT (PostgreSQL 9.5+ compatible)

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE LOWER(table_name) = 'restaurant' AND LOWER(column_name) = 'online_ordering_turnedon'
    ) THEN
        ALTER TABLE RESTAURANT ADD COLUMN online_ordering_turnedon BOOLEAN DEFAULT FALSE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE LOWER(table_name) = 'restaurant' AND LOWER(column_name) = 'online_ordering_last_heartbeat'
    ) THEN
        ALTER TABLE RESTAURANT ADD COLUMN online_ordering_last_heartbeat TIMESTAMP WITH TIME ZONE NULL;
    END IF;
END $$;
