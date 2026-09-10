-- =============================================================================
-- Migration: 004_add_updated_at_columns.sql
-- Database : FloreantPOS (PostgreSQL 9.5+)
-- Purpose  : Adds UPDATED_AT columns and auto-updating triggers to core tables
--            to enable delta synchronization with the Virtual Menu system.
-- =============================================================================

-- 1. Create or replace trigger function to update timestamp on row changes
CREATE OR REPLACE FUNCTION fn_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.UPDATED_AT = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 2. Add UPDATED_AT column to MENU_ITEM
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'menu_item' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE MENU_ITEM ADD COLUMN UPDATED_AT TIMESTAMP DEFAULT NOW();
    END IF;
END $$;

DROP TRIGGER IF EXISTS trg_menu_item_updated_at ON MENU_ITEM;
CREATE TRIGGER trg_menu_item_updated_at
BEFORE UPDATE ON MENU_ITEM
FOR EACH ROW
EXECUTE PROCEDURE fn_set_updated_at();

-- 3. Add UPDATED_AT column to MENU_CATEGORY
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'menu_category' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE MENU_CATEGORY ADD COLUMN UPDATED_AT TIMESTAMP DEFAULT NOW();
    END IF;
END $$;

DROP TRIGGER IF EXISTS trg_menu_category_updated_at ON MENU_CATEGORY;
CREATE TRIGGER trg_menu_category_updated_at
BEFORE UPDATE ON MENU_CATEGORY
FOR EACH ROW
EXECUTE PROCEDURE fn_set_updated_at();

-- 4. Add UPDATED_AT column to MENU_ITEM_MODIFIER
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'menu_item_modifier' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE MENU_ITEM_MODIFIER ADD COLUMN UPDATED_AT TIMESTAMP DEFAULT NOW();
    END IF;
END $$;

DROP TRIGGER IF EXISTS trg_menu_item_modifier_updated_at ON MENU_ITEM_MODIFIER;
CREATE TRIGGER trg_menu_item_modifier_updated_at
BEFORE UPDATE ON MENU_ITEM_MODIFIER
FOR EACH ROW
EXECUTE PROCEDURE fn_set_updated_at();

-- 5. Add UPDATED_AT column to TICKET
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'ticket' AND column_name = 'updated_at'
    ) THEN
        ALTER TABLE TICKET ADD COLUMN UPDATED_AT TIMESTAMP DEFAULT NOW();
    END IF;
END $$;

DROP TRIGGER IF EXISTS trg_ticket_updated_at ON TICKET;
CREATE TRIGGER trg_ticket_updated_at
BEFORE UPDATE ON TICKET
FOR EACH ROW
EXECUTE PROCEDURE fn_set_updated_at();

-- 6. Ensure default system user 'ONLINE_USER' exists for web voids/cancellations
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM USERS WHERE USER_NAME = 'ONLINE_USER'
    ) THEN
        INSERT INTO USERS (
            USER_ID,
            USER_NAME,
            FIRST_NAME,
            LAST_NAME,
            ACTIVE,
            AUTO_ID
        ) VALUES (
            '9999',
            'ONLINE_USER',
            'Online',
            'System',
            true,
            (SELECT COALESCE(MAX(AUTO_ID), 0) + 1 FROM USERS)
        );
    END IF;
END $$;
