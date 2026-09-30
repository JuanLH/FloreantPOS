-- Schema Migration: Ensure CUSTOMER.EMAIL is VARCHAR(60) and UNIQUE
ALTER TABLE CUSTOMER ALTER COLUMN EMAIL TYPE VARCHAR(60);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = ANY(c.conkey)
        WHERE c.conrelid = 'customer'::regclass
          AND c.contype = 'u'
          AND a.attname = 'email'
    ) THEN
        ALTER TABLE CUSTOMER ADD CONSTRAINT uq_customer_email UNIQUE (EMAIL);
    END IF;
END $$;

-- Schema Migration: Add LABEL, LATITUDE, and LONGITUDE columns to DELIVERY_ADDRESS (PostgreSQL 9.5 compatible)
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_address' AND column_name = 'label') THEN
        ALTER TABLE DELIVERY_ADDRESS ADD COLUMN LABEL VARCHAR(30);
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_address' AND column_name = 'latitude') THEN
        ALTER TABLE DELIVERY_ADDRESS ADD COLUMN LATITUDE DOUBLE PRECISION;
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'delivery_address' AND column_name = 'longitude') THEN
        ALTER TABLE DELIVERY_ADDRESS ADD COLUMN LONGITUDE DOUBLE PRECISION;
    END IF;
END $$;

-- Schema Migration: Ensure unique compound key between LABEL and CUSTOMER_ID on DELIVERY_ADDRESS
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint c
        WHERE c.conrelid = 'delivery_address'::regclass
          AND c.contype = 'u'
          AND c.conname = 'uq_delivery_address_label_customer_id'
    ) THEN
        ALTER TABLE DELIVERY_ADDRESS ADD CONSTRAINT uq_delivery_address_label_customer_id UNIQUE (LABEL, CUSTOMER_ID);
    END IF;
END $$;


