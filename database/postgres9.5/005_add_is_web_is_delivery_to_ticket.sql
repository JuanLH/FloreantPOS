-- Schema Migration: Add IS_WEB and IS_DELIVERY columns to TICKET (PostgreSQL 9.5 compatible)

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE LOWER(table_name) = 'ticket' AND LOWER(column_name) = 'is_web'
    ) THEN
        ALTER TABLE TICKET ADD COLUMN IS_WEB BOOLEAN DEFAULT FALSE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE LOWER(table_name) = 'ticket' AND LOWER(column_name) = 'is_delivery'
    ) THEN
        ALTER TABLE TICKET ADD COLUMN IS_DELIVERY BOOLEAN DEFAULT FALSE;
    END IF;
END $$;
