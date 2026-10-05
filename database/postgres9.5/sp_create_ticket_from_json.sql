-- =============================================================================
-- Stored Procedure : sp_create_ticket_from_json
-- Database         : FloreantPOS (PostgreSQL 9.5)
-- File             : database/postgres9.5/sp_create_ticket_from_json.sql
-- Description      : Receives a JSONB payload from an external API and
--                    atomically creates a complete ticket (header, items,
--                    modifiers, cooking instructions, customer, delivery
--                    address, table numbers) returning the new TICKET.ID.
--
-- Defaults applied for open design questions:
--   Q1  Customer resolution: Resolves customer by EMAIL in CUSTOMER table.
--                            If exists: gets AUTO_ID and assigns to TICKET.CUSTOMER_ID.
--                            If not exists: creates a CUSTOMER record and gets AUTO_ID.
--                            Ticket CANNOT have CUSTOMER_ID = NULL.
--   Q2  Delivery charge   : API passes delivery_charge in the JSON root.
--   Q3  System params     : p_owner_id / p_terminal_id / p_shift_id are
--                           optional (DEFAULT NULL) for headless/online orders.
--   Q4  GLOBAL_ID         : Generated inside the SP from MD5(NOW||RANDOM).
--   Q5  Initial status    : Ticket created as OPEN (paid=false, closed=false).
--   Q6  Modifiers/Add-ons : All modifiers go to TICKET_ITEM_MODIFIER_RELATION.
--                           Pass "is_addon": true to route to
--                           TICKET_ITEM_ADDON_RELATION instead.
--   Q7  Stock validation  : Pre-validates item existence & stock availability,
--                           and decrements MENU_ITEM.STOCK_AMOUNT prior to
--                           inserting the ticket.
--
-- Usage:
--   SELECT sp_create_ticket_from_json(
--       p_order_json  => '{ ... }'::jsonb,
--       p_owner_id    => 1,
--       p_terminal_id => 1,
--       p_shift_id    => 1
--   );
--
-- Re-runnable: DROP + CREATE guard makes re-deploying this file safe.
-- =============================================================================

DROP FUNCTION IF EXISTS sp_create_ticket_from_json(JSONB, INTEGER, INTEGER, INTEGER);

CREATE OR REPLACE FUNCTION sp_create_ticket_from_json(
    -- -------------------------------------------------------------------------
    -- Primary input: complete order JSON from the external API
    -- -------------------------------------------------------------------------
    p_order_json  JSONB,

    -- -------------------------------------------------------------------------
    -- Optional system-context parameters (leave NULL for headless/online orders)
    -- -------------------------------------------------------------------------
    p_owner_id    INTEGER DEFAULT 1,  -- FK -> USER.ID  (server / cashier)
    p_terminal_id INTEGER DEFAULT 222,  -- FK -> TERMINAL.ID
    p_shift_id    INTEGER DEFAULT 1   -- FK -> SHIFT.ID
)
RETURNS INTEGER   -- Returns the newly generated TICKET.ID
LANGUAGE plpgsql
AS $$
-- =============================================================================
-- VARIABLE DECLARATIONS
-- =============================================================================
DECLARE

    -- -------------------------------------------------------------------------
    -- Ticket-level variables  (extracted from p_order_json root)
    -- -------------------------------------------------------------------------
    v_order_type        TEXT;                       -- -> TICKET.TICKET_TYPE
    v_num_guests        INTEGER;                    -- -> TICKET.NUMBER_OF_GUESTS
    v_notes             TEXT;                       -- -> TICKET_PROPERTIES key='notes'
    v_delivery_charge   DOUBLE PRECISION;           -- -> TICKET.DELIVERY_CHARGE
    v_is_web            BOOLEAN := TRUE;            -- -> TICKET.IS_WEB
    v_is_delivery       BOOLEAN := FALSE;           -- -> TICKET.IS_DELIVERY
    v_owner_auto_id     INTEGER := 1;               -- resolved -> TICKET.OWNER_ID (FK -> USERS.AUTO_ID)

    v_ticket_id         INTEGER;                    -- generated PK returned to caller
    v_ticket_subtotal   DOUBLE PRECISION := 0.0;   -- accumulated -> TICKET.SUB_TOTAL
    v_ticket_tax        DOUBLE PRECISION := 0.0;   -- accumulated -> TICKET.TOTAL_TAX
    v_ticket_total      DOUBLE PRECISION := 0.0;   -- accumulated -> TICKET.TOTAL_PRICE

    v_create_date       TIMESTAMP;                  -- -> TICKET.CREATE_DATE
    v_creation_hour     INTEGER;                    -- -> TICKET.CREATION_HOUR
    v_global_id         TEXT;                       -- -> TICKET.GLOBAL_ID (16-char unique)

    -- -------------------------------------------------------------------------
    -- Customer variables  (extracted from p_order_json -> 'customer')
    -- -------------------------------------------------------------------------
    v_customer_obj      JSONB;                      -- the customer sub-object
    v_cust_first_name   TEXT;                       -- -> CUSTOMER.FIRST_NAME
    v_cust_last_name    TEXT;                       -- -> CUSTOMER.LAST_NAME
    v_cust_name         TEXT;                       -- -> CUSTOMER.name (full name fallback)
    v_cust_email        TEXT;                       -- -> CUSTOMER.EMAIL
    v_cust_mobile       TEXT;                       -- -> CUSTOMER.MOBILE_NO (primary lookup key)
    v_cust_home_phone   TEXT;                       -- -> CUSTOMER.HOMEPHONE_NO
    v_cust_address      TEXT;                       -- -> CUSTOMER.ADDRESS (home address)
    v_cust_city         TEXT;                       -- -> CUSTOMER.CITY
    v_cust_state        TEXT;                       -- -> CUSTOMER.STATE
    v_cust_zip          TEXT;                       -- -> CUSTOMER.ZIP_CODE
    v_cust_note         TEXT;                       -- -> CUSTOMER.NOTE
    v_customer_id       INTEGER;                    -- resolved/generated PK -> TICKET.CUSTOMER_ID

    -- -------------------------------------------------------------------------
    -- Delivery address variables  (extracted from p_order_json -> 'delivery_address')
    -- -------------------------------------------------------------------------
    v_delivery_obj       JSONB;                     -- the delivery_address sub-object
    v_del_label          TEXT;                      -- -> DELIVERY_ADDRESS.LABEL
    v_del_address        TEXT;                      -- -> DELIVERY_ADDRESS.ADDRESS
    v_del_phone_ext      TEXT;                      -- -> DELIVERY_ADDRESS.PHONE_EXTENSION
    v_del_room_no        TEXT;                      -- -> DELIVERY_ADDRESS.ROOM_NO
    v_del_distance       DOUBLE PRECISION;          -- -> DELIVERY_ADDRESS.DISTANCE
    v_del_latitude       DOUBLE PRECISION;          -- -> DELIVERY_ADDRESS.LATITUDE
    v_del_longitude      DOUBLE PRECISION;          -- -> DELIVERY_ADDRESS.LONGITUDE
    v_del_addr_full_str  TEXT;                      -- combined string -> TICKET.DELIVERY_ADDRESS

    -- -------------------------------------------------------------------------
    -- Per-item loop variables  (iterate over p_order_json -> 'items')
    -- -------------------------------------------------------------------------
    v_item_rec              JSONB;                  -- current element from items[]
    v_item_menu_item_id     INTEGER;                -- -> TICKET_ITEM.ITEM_ID  (FK -> MENU_ITEM)
    v_item_name             TEXT;                   -- -> TICKET_ITEM.ITEM_NAME
    v_item_qty              DOUBLE PRECISION;        -- -> TICKET_ITEM.ITEM_QUANTITY
    v_item_count            INTEGER;                -- -> TICKET_ITEM.ITEM_COUNT = ROUND(qty)
    v_item_unit_price       DOUBLE PRECISION;        -- -> TICKET_ITEM.ITEM_PRICE
    v_item_group_name       TEXT;                   -- -> TICKET_ITEM.GROUP_NAME  (denormalised)
    v_item_category_name    TEXT;                   -- -> TICKET_ITEM.CATEGORY_NAME (denormalised)
    v_item_tax_rate         DOUBLE PRECISION;        -- -> TICKET_ITEM.ITEM_TAX_RATE
    v_item_seat_number      INTEGER;                -- -> TICKET_ITEM.SEAT_NUMBER  (optional)

    -- Per-item computed amounts
    v_modifier_subtotal              DOUBLE PRECISION; -- sum of modifier prices for this item
    v_item_subtotal                  DOUBLE PRECISION; -- -> TICKET_ITEM.SUB_TOTAL
    v_item_subtotal_no_modifiers     DOUBLE PRECISION; -- -> TICKET_ITEM.SUB_TOTAL_WITHOUT_MODIFIERS
    v_item_tax                       DOUBLE PRECISION; -- -> TICKET_ITEM.TAX_AMOUNT
    v_item_tax_no_modifiers          DOUBLE PRECISION; -- -> TICKET_ITEM.TAX_AMOUNT_WITHOUT_MODIFIERS
    v_item_total                     DOUBLE PRECISION; -- -> TICKET_ITEM.TOTAL_PRICE
    v_item_total_no_modifiers        DOUBLE PRECISION; -- -> TICKET_ITEM.TOTAL_PRICE_WITHOUT_MODIFIERS

    v_ticket_item_id    INTEGER;                    -- generated PK for each TICKET_ITEM row

    -- -------------------------------------------------------------------------
    -- Per-modifier loop variables  (inner loop over item -> 'modifiers')
    -- -------------------------------------------------------------------------
    v_mod_rec           JSONB;                      -- current element from modifiers[]
    v_mod_modifier_id   INTEGER;                    -- -> TICKET_ITEM_MODIFIER.ITEM_ID  (FK -> MENU_MODIFIER)
    v_mod_name          TEXT;                       -- -> TICKET_ITEM_MODIFIER.MODIFIER_NAME
    v_mod_unit_price    DOUBLE PRECISION;            -- -> TICKET_ITEM_MODIFIER.MODIFIER_PRICE
    v_mod_tax_rate      DOUBLE PRECISION;            -- -> TICKET_ITEM_MODIFIER.MODIFIER_TAX_RATE
    v_mod_type          INTEGER;                    -- -> TICKET_ITEM_MODIFIER.MODIFIER_TYPE
    v_mod_item_count    INTEGER;                    -- -> TICKET_ITEM_MODIFIER.ITEM_COUNT
    v_mod_is_addon      BOOLEAN;                    -- true -> TICKET_ITEM_ADDON_RELATION; false -> MODIFIER_RELATION

    -- Per-modifier computed amounts
    v_mod_subtotal      DOUBLE PRECISION;            -- -> TICKET_ITEM_MODIFIER.SUBTOTAL_PRICE
    v_mod_tax           DOUBLE PRECISION;            -- -> TICKET_ITEM_MODIFIER.TAX_AMOUNT
    v_mod_total         DOUBLE PRECISION;            -- -> TICKET_ITEM_MODIFIER.TOTAL_PRICE

    v_modifier_row_id   INTEGER;                    -- generated PK used in relation insert
    v_mod_list_order    INTEGER;                    -- LIST_ORDER in TICKET_ITEM_MODIFIER_RELATION

    -- -------------------------------------------------------------------------
    -- Per-cooking-instruction loop variables  (inner loop over item -> 'cooking_instructions')
    -- -------------------------------------------------------------------------
    v_ci_rec            JSONB;                      -- current element from cooking_instructions[]
    v_ci_description    TEXT;                       -- -> TICKET_ITEM_COOKING_INSTRUCTION.description
    v_ci_printed        BOOLEAN;                    -- -> TICKET_ITEM_COOKING_INSTRUCTION.printedToKitchen
    v_ci_order          INTEGER;                    -- -> TICKET_ITEM_COOKING_INSTRUCTION.ITEM_ORDER

    -- -------------------------------------------------------------------------
    -- Table number loop variable
    -- -------------------------------------------------------------------------
    v_table_num         INTEGER;                    -- each entry in p_order_json -> 'table_numbers'

    -- -------------------------------------------------------------------------
    -- Stock validation and adjustment variables
    -- -------------------------------------------------------------------------
    v_val_rec           RECORD;                     -- aggregated item loop record
    v_mi_id             INTEGER;                    -- MENU_ITEM.ID
    v_mi_name           TEXT;                       -- MENU_ITEM.NAME
    v_mi_stock          DOUBLE PRECISION;           -- MENU_ITEM.STOCK_AMOUNT
    v_mi_disable_stock  BOOLEAN;                    -- MENU_ITEM.DISABLE_WHEN_STOCK_AMOUNT_IS_ZERO

-- =============================================================================
-- FUNCTION BODY
-- =============================================================================
BEGIN

    -- =========================================================================
    -- STEP 1 — Extract top-level scalar fields from the JSON root
    -- =========================================================================
    v_create_date     := NOW();
    v_creation_hour   := EXTRACT(HOUR FROM NOW())::INTEGER;

    -- 16-char GLOBAL_ID: check for idempotency if caller provided global_id
    IF p_order_json->>'global_id' IS NOT NULL AND p_order_json->>'global_id' <> '' THEN
        v_global_id := LEFT(p_order_json->>'global_id', 16);
        SELECT ID INTO v_ticket_id FROM TICKET WHERE GLOBAL_ID = v_global_id LIMIT 1;
        IF v_ticket_id IS NOT NULL THEN
            RETURN v_ticket_id;
        END IF;
    ELSE
        v_global_id := LEFT(MD5(NOW()::TEXT || RANDOM()::TEXT), 16);
    END IF;

    v_order_type      := p_order_json->>'order_type';           -- e.g. 'Delivery', 'Dine In'
    v_num_guests      := (p_order_json->>'number_of_guests')::INTEGER;
    v_notes           := p_order_json->>'notes';
    v_delivery_charge := COALESCE((p_order_json->>'delivery_charge')::DOUBLE PRECISION, 0.0);

    -- Web & Delivery flags: By default IS_WEB = TRUE; IS_DELIVERY = TRUE when user chooses delivery
    v_is_web          := COALESCE((p_order_json->>'is_web')::BOOLEAN, TRUE);
    IF (p_order_json->>'is_delivery') IS NOT NULL THEN
        v_is_delivery := (p_order_json->>'is_delivery')::BOOLEAN;
    ELSIF LOWER(COALESCE(v_order_type, '')) = 'delivery' 
       OR (p_order_json->'delivery_address' IS NOT NULL AND jsonb_typeof(p_order_json->'delivery_address') = 'object') THEN
        v_is_delivery := TRUE;
    ELSE
        v_is_delivery := FALSE;
    END IF;

    -- =========================================================================
    -- STEP 2 — Customer resolution and creation
    --   The ticket cannot have customer_id = NULL.
    --   1. Extract customer details from JSON payload.
    --   2. Check if customer email exists in the CUSTOMER table:
    --      - If exists: get AUTO_ID and use it in ticket customer_id.
    --      - If not exists: create a CUSTOMER record and get AUTO_ID.
    -- =========================================================================
    v_customer_obj := p_order_json->'customer';

    IF v_customer_obj IS NOT NULL THEN
        v_cust_first_name := TRIM(COALESCE(v_customer_obj->>'first_name', v_customer_obj->>'firstName'));
        v_cust_last_name  := TRIM(COALESCE(v_customer_obj->>'last_name', v_customer_obj->>'lastName'));
        v_cust_name       := COALESCE(
                                 TRIM(v_customer_obj->>'name'),
                                 NULLIF(TRIM(COALESCE(v_cust_first_name, '') || ' ' || COALESCE(v_cust_last_name, '')), '')
                             );
        v_cust_email      := LOWER(TRIM(COALESCE(v_customer_obj->>'email', p_order_json->>'customer_email', p_order_json->>'email')));
        v_cust_mobile     := COALESCE(v_customer_obj->>'mobile_no', v_customer_obj->>'mobilePhone', v_customer_obj->>'phone');
        v_cust_home_phone := COALESCE(v_customer_obj->>'home_phone_no', v_customer_obj->>'homePhone', v_customer_obj->>'home_phone');
        v_cust_address    := COALESCE(v_customer_obj->>'address', p_order_json->'delivery_address'->>'address');
        v_cust_city       := v_customer_obj->>'city';
        v_cust_state      := v_customer_obj->>'state';
        v_cust_zip        := COALESCE(v_customer_obj->>'zip_code', v_customer_obj->>'zip', v_customer_obj->>'zipCode');
        v_cust_note       := COALESCE(v_customer_obj->>'note', v_customer_obj->>'notes', v_customer_obj->>'delivery_notes');
    ELSE
        -- Support root-level customer fields if customer object is omitted
        v_cust_first_name := TRIM(COALESCE(p_order_json->>'first_name', p_order_json->>'firstName'));
        v_cust_last_name  := TRIM(COALESCE(p_order_json->>'last_name', p_order_json->>'lastName'));
        v_cust_name       := COALESCE(
                                 TRIM(p_order_json->>'customer_name'),
                                 NULLIF(TRIM(COALESCE(v_cust_first_name, '') || ' ' || COALESCE(v_cust_last_name, '')), '')
                             );
        v_cust_email      := LOWER(TRIM(COALESCE(p_order_json->>'customer_email', p_order_json->>'email')));
        v_cust_mobile     := COALESCE(p_order_json->>'customer_mobile', p_order_json->>'mobile_no', p_order_json->>'mobilePhone', p_order_json->>'phone');
    END IF;

    -- Normalize email and truncate to 60 characters
    IF v_cust_email IS NOT NULL THEN
        v_cust_email := NULLIF(SUBSTRING(v_cust_email FROM 1 FOR 60), '');
    END IF;

    -- 1. Check if caller supplied an explicit valid customer ID (auto_id or id)
    IF v_customer_obj IS NOT NULL AND (v_customer_obj->>'auto_id' IS NOT NULL OR v_customer_obj->>'id' IS NOT NULL) THEN
        SELECT AUTO_ID INTO v_customer_id
        FROM CUSTOMER
        WHERE AUTO_ID = COALESCE((v_customer_obj->>'auto_id')::INTEGER, (v_customer_obj->>'id')::INTEGER)
        LIMIT 1;
    END IF;

    -- 2. Check if the email exists in the CUSTOMER table
    IF v_customer_id IS NULL AND v_cust_email IS NOT NULL THEN
        SELECT AUTO_ID INTO v_customer_id
        FROM CUSTOMER
        WHERE LOWER(TRIM(EMAIL)) = v_cust_email
        LIMIT 1;
    END IF;

    -- 3. If customer not found, create new record in CUSTOMER table and get AUTO_ID
    IF v_customer_id IS NULL THEN
        IF v_cust_email IS NULL THEN
            RAISE EXCEPTION 'Customer email is required to resolve or create customer record';
        END IF;

        IF v_cust_name IS NULL OR v_cust_name = '' THEN
            v_cust_name := v_cust_email;
        END IF;

        BEGIN
            INSERT INTO CUSTOMER (
                FIRST_NAME,
                LAST_NAME,
                name,
                EMAIL,
                MOBILE_NO,
                HOMEPHONE_NO,
                ADDRESS,
                CITY,
                STATE,
                ZIP_CODE,
                NOTE
            ) VALUES (
                SUBSTRING(v_cust_first_name FROM 1 FOR 60),
                SUBSTRING(v_cust_last_name FROM 1 FOR 60),
                SUBSTRING(v_cust_name FROM 1 FOR 120),
                v_cust_email,
                SUBSTRING(v_cust_mobile FROM 1 FOR 30),
                SUBSTRING(v_cust_home_phone FROM 1 FOR 30),
                SUBSTRING(v_cust_address FROM 1 FOR 620),
                SUBSTRING(v_cust_city FROM 1 FOR 30),
                SUBSTRING(v_cust_state FROM 1 FOR 30),
                SUBSTRING(v_cust_zip FROM 1 FOR 10),
                SUBSTRING(v_cust_note FROM 1 FOR 255)
            )
            RETURNING AUTO_ID INTO v_customer_id;
        EXCEPTION
            WHEN unique_violation THEN
                -- In case of concurrent insert with the same email
                SELECT AUTO_ID INTO v_customer_id
                FROM CUSTOMER
                WHERE LOWER(TRIM(EMAIL)) = v_cust_email
                LIMIT 1;
        END;
    END IF;

    -- Final validation: Ticket can't have customer_id = NULL
    IF v_customer_id IS NULL THEN
        RAISE EXCEPTION 'Ticket cannot be created without a valid customer ID';
    END IF;

    -- =========================================================================
    -- STEP 3 — Delivery address insert
    --   Only executed when the 'delivery_address' key is present in the JSON.
    -- =========================================================================
    v_delivery_obj := p_order_json->'delivery_address';

    IF v_delivery_obj IS NOT NULL AND jsonb_typeof(v_delivery_obj) = 'object' THEN

        v_del_label     := SUBSTRING(COALESCE(v_delivery_obj->>'label', v_delivery_obj->>'address_label', p_order_json->>'address_label', p_order_json->>'label') FROM 1 FOR 30);
        v_del_address   := v_delivery_obj->>'address';
        v_del_phone_ext := v_delivery_obj->>'phone_extension';
        v_del_room_no   := v_delivery_obj->>'room_no';
        v_del_distance  := (v_delivery_obj->>'distance')::DOUBLE PRECISION;
        v_del_latitude  := (v_delivery_obj->>'latitude')::DOUBLE PRECISION;
        v_del_longitude := (v_delivery_obj->>'longitude')::DOUBLE PRECISION;

        IF v_del_label IS NOT NULL AND TRIM(v_del_label) <> '' THEN
            INSERT INTO DELIVERY_ADDRESS (
                LABEL,
                ADDRESS,
                PHONE_EXTENSION,
                ROOM_NO,
                DISTANCE,
                LATITUDE,
                LONGITUDE,
                CUSTOMER_ID
            ) VALUES (
                TRIM(v_del_label),
                v_del_address,
                v_del_phone_ext,
                v_del_room_no,
                v_del_distance,
                v_del_latitude,
                v_del_longitude,
                v_customer_id
            )
            ON CONFLICT (LABEL, CUSTOMER_ID) DO UPDATE SET
                ADDRESS = EXCLUDED.ADDRESS,
                PHONE_EXTENSION = EXCLUDED.PHONE_EXTENSION,
                ROOM_NO = EXCLUDED.ROOM_NO,
                DISTANCE = EXCLUDED.DISTANCE,
                LATITUDE = EXCLUDED.LATITUDE,
                LONGITUDE = EXCLUDED.LONGITUDE;
        ELSE
            INSERT INTO DELIVERY_ADDRESS (
                LABEL,
                ADDRESS,
                PHONE_EXTENSION,
                ROOM_NO,
                DISTANCE,
                LATITUDE,
                LONGITUDE,
                CUSTOMER_ID
            ) VALUES (
                NULL,
                v_del_address,
                v_del_phone_ext,
                v_del_room_no,
                v_del_distance,
                v_del_latitude,
                v_del_longitude,
                v_customer_id
            );
        END IF;

        -- Build a denormalised address string for TICKET.DELIVERY_ADDRESS column
        v_del_addr_full_str := TRIM(
            COALESCE(v_del_address, '') ||
            CASE WHEN v_del_room_no IS NOT NULL THEN ', ' || v_del_room_no ELSE '' END
        );

    END IF; -- END delivery address block

    -- =========================================================================
    -- STEP 4 — Stock validation and inventory adjustment
    --   Validates that:
    --     1. The order contains at least one item in the 'items' array.
    --     2. Each referenced MENU_ITEM exists in the database.
    --     3. Items with DISABLE_WHEN_STOCK_AMOUNT_IS_ZERO = TRUE have sufficient
    --        stock available (aggregated across all ticket lines for that item).
    --   If sufficient stock is available:
    --     - Decrements MENU_ITEM.STOCK_AMOUNT for stock-tracked items.
    --   If any item fails validation:
    --     - Raises an exception and aborts the entire transaction before the
    --       ticket is created.
    --   Row locks (FOR UPDATE) are acquired in ascending menu_item_id order to
    --   guarantee deadlock-free concurrent execution.
    -- =========================================================================
    IF COALESCE(p_order_json->'items', p_order_json->'ticket_items') IS NULL 
       OR jsonb_array_length(COALESCE(p_order_json->'items', p_order_json->'ticket_items')) = 0 THEN
        RAISE EXCEPTION 'Order contains no items';
    END IF;

    FOR v_val_rec IN
        SELECT
            COALESCE(item->>'item_id', item->>'menu_item_id', item->>'id')::INTEGER AS menu_item_id,
            COALESCE(item->>'name', 'Item')   AS item_name,
            SUM(COALESCE((item->>'quantity')::DOUBLE PRECISION, 1.0)) AS total_qty
        FROM jsonb_array_elements(COALESCE(p_order_json->'items', p_order_json->'ticket_items')) AS item
        WHERE COALESCE(item->>'item_id', item->>'menu_item_id', item->>'id') IS NOT NULL 
          AND COALESCE(item->>'item_id', item->>'menu_item_id', item->>'id') <> ''
        GROUP BY COALESCE(item->>'item_id', item->>'menu_item_id', item->>'id')::INTEGER, COALESCE(item->>'name', 'Item')
        ORDER BY COALESCE(item->>'item_id', item->>'menu_item_id', item->>'id')::INTEGER ASC
    LOOP
        IF v_val_rec.total_qty <= 0.0 THEN
            RAISE EXCEPTION 'Invalid quantity % for item ID % ("%")',
                v_val_rec.total_qty, v_val_rec.menu_item_id, v_val_rec.item_name;
        END IF;

        -- Lock the row and fetch current stock and configuration
        SELECT ID, NAME, STOCK_AMOUNT, DISABLE_WHEN_STOCK_AMOUNT_IS_ZERO
        INTO v_mi_id, v_mi_name, v_mi_stock, v_mi_disable_stock
        FROM MENU_ITEM
        WHERE ID = v_val_rec.menu_item_id
        FOR UPDATE;

        IF NOT FOUND THEN
            RAISE EXCEPTION 'Menu item with ID % ("%") not found in MENU_ITEM table',
                v_val_rec.menu_item_id, v_val_rec.item_name;
        END IF;

        -- Validate stock availability for items with stock enforcement enabled
        IF COALESCE(v_mi_disable_stock, FALSE) = TRUE THEN
            IF COALESCE(v_mi_stock, 0.0) < v_val_rec.total_qty THEN
                RAISE EXCEPTION 'Insufficient stock for menu item "%" (ID %). Available: %, requested: %',
                    v_mi_name, v_mi_id, COALESCE(v_mi_stock, 0.0), v_val_rec.total_qty;
            END IF;
        END IF;

        -- Adjust stock in MENU_ITEM table:
        -- Decrement stock if stock tracking is enabled or positive stock exists
        IF COALESCE(v_mi_disable_stock, FALSE) = TRUE OR COALESCE(v_mi_stock, 0.0) > 0.0 THEN
            UPDATE MENU_ITEM
            SET STOCK_AMOUNT = GREATEST(0.0, COALESCE(STOCK_AMOUNT, 0.0) - v_val_rec.total_qty)
            WHERE ID = v_mi_id;
        END IF;

    END LOOP;

    -- =========================================================================
    -- STEP 5 — Insert TICKET header row
    --   Financial amounts (SUB_TOTAL, TOTAL_TAX, TOTAL_PRICE) are seeded at
    --   0.0 here and updated with the real totals in STEP 10 after all items
    --   have been processed.
    -- =========================================================================

    -- Resolve owner AUTO_ID from USERS table:
    -- When p_owner_id > 1, resolve the corresponding AUTO_ID from USERS
    -- where USER_ID = p_owner_id (or fallback to AUTO_ID if directly passed).
    IF p_owner_id IS NOT NULL AND p_owner_id > 1 THEN
        SELECT AUTO_ID INTO v_owner_auto_id
        FROM USERS
        WHERE USER_ID = p_owner_id
        LIMIT 1;

        IF v_owner_auto_id IS NULL THEN
            SELECT AUTO_ID INTO v_owner_auto_id
            FROM USERS
            WHERE AUTO_ID = p_owner_id
            LIMIT 1;
        END IF;

        IF v_owner_auto_id IS NULL THEN
            v_owner_auto_id := 1;
        END IF;
    ELSE
        v_owner_auto_id := COALESCE(p_owner_id, 1);
    END IF;

    INSERT INTO TICKET (
        GLOBAL_ID,
        CREATE_DATE,
        CREATION_HOUR,
        TICKET_TYPE,
        NUMBER_OF_GUESTS,
        PAID,
        VOIDED,
        WASTED,
        REFUNDED,
        SETTLED,                -- Hibernate maps this column as 'closed'
        DRAWER_RESETTED,
        IS_TAX_EXEMPT,
        IS_RE_OPENED,
        BAR_TAB,
        SUB_TOTAL,
        TOTAL_DISCOUNT,
        TOTAL_TAX,
        TOTAL_PRICE,
        PAID_AMOUNT,
        DUE_AMOUNT,
        ADVANCE_AMOUNT,
        ADJUSTMENT_AMOUNT,
        DELIVERY_CHARGE,
        DELIVERY_ADDRESS,
        CUSTOMER_ID,
        OWNER_ID,
        TERMINAL_ID,
        SHIFT_ID,
        VERSION_NO,
        STATUS,
        IS_WEB,
        IS_DELIVERY
    ) VALUES (
        v_global_id,
        v_create_date,
        v_creation_hour,
        v_order_type,
        v_num_guests,
        FALSE,              -- paid           : ticket starts unpaid
        FALSE,              -- voided
        FALSE,              -- wasted
        FALSE,              -- refunded
        FALSE,              -- settled/closed : ticket starts open
        FALSE,              -- drawer_resetted
        FALSE,              -- is_tax_exempt
        FALSE,              -- is_re_opened
        FALSE,              -- bar_tab
        0.0,                -- sub_total      : placeholder, updated in STEP 9
        0.0,                -- total_discount
        0.0,                -- total_tax      : placeholder, updated in STEP 9
        0.0,                -- total_price    : placeholder, updated in STEP 9
        0.0,                -- paid_amount
        0.0,                -- due_amount     : updated in STEP 9
        0.0,                -- advance_amount
        0.0,                -- adjustment_amount
        v_delivery_charge,
        v_del_addr_full_str,
        v_customer_id,
        v_owner_auto_id,
        p_terminal_id,
        p_shift_id,
        0,                  -- version_no     : optimistic-locking seed
        'OPEN',             -- status
        v_is_web,
        v_is_delivery
    )
    RETURNING ID INTO v_ticket_id;

    -- Persist customer details as ticket properties
    IF v_cust_name IS NOT NULL AND TRIM(v_cust_name) <> '' THEN
        INSERT INTO TICKET_PROPERTIES (id, property_name, property_value)
        VALUES (v_ticket_id, 'CUSTOMER_NAME', TRIM(v_cust_name));
    END IF;

    IF v_cust_mobile IS NOT NULL AND TRIM(v_cust_mobile) <> '' THEN
        INSERT INTO TICKET_PROPERTIES (id, property_name, property_value)
        VALUES (v_ticket_id, 'CUSTOMER_MOBILE', TRIM(v_cust_mobile));
    END IF;

    IF v_cust_email IS NOT NULL AND TRIM(v_cust_email) <> '' THEN
        INSERT INTO TICKET_PROPERTIES (id, property_name, property_value)
        VALUES (v_ticket_id, 'CUSTOMER_EMAIL', TRIM(v_cust_email));
    END IF;

    -- Persist optional order notes as a ticket property
    IF v_notes IS NOT NULL AND TRIM(v_notes) <> '' THEN
        INSERT INTO TICKET_PROPERTIES (id, property_name, property_value)
        VALUES (v_ticket_id, 'notes', TRIM(v_notes));
    END IF;

    -- =========================================================================
    -- STEP 6 — Outer loop: iterate over items[]
    -- =========================================================================
    FOR v_item_rec IN
        SELECT jsonb_array_elements(COALESCE(p_order_json->'items', p_order_json->'ticket_items'))
    LOOP

        -- ---------------------------------------------------------------------
        -- 5a. Extract per-item scalar fields from the JSON element
        -- ---------------------------------------------------------------------
        v_item_menu_item_id  := COALESCE(v_item_rec->>'item_id', v_item_rec->>'menu_item_id', v_item_rec->>'id')::INTEGER;
        v_item_name          := v_item_rec->>'name';
        v_item_qty           := COALESCE((v_item_rec->>'quantity')::DOUBLE PRECISION, 1.0);
        v_item_count         := ROUND(v_item_qty)::INTEGER;
        v_item_unit_price    := COALESCE((v_item_rec->>'unit_price')::DOUBLE PRECISION, 0.0);
        v_item_group_name    := v_item_rec->>'group_name';
        v_item_category_name := v_item_rec->>'category_name';
        v_item_tax_rate      := COALESCE((v_item_rec->>'tax_rate')::DOUBLE PRECISION, 0.0);
        v_item_seat_number   := (v_item_rec->>'seat_number')::INTEGER;


        -- Reset per-item inner counters
        v_modifier_subtotal := 0.0;
        v_mod_list_order    := 0;
        v_ci_order          := 0;

        -- ---------------------------------------------------------------------
        -- 5b. Pre-calculate combined modifier price for this item
        --     v_modifier_subtotal = SUM(modifier.unit_price * modifier.item_count)
        --     This is needed to compute item subtotal before inserting the row.
        -- ---------------------------------------------------------------------
        FOR v_mod_rec IN
            SELECT jsonb_array_elements(v_item_rec->'modifiers')
        LOOP
            v_modifier_subtotal := v_modifier_subtotal +
                COALESCE((v_mod_rec->>'unit_price')::DOUBLE PRECISION, 0.0) *
                COALESCE((v_mod_rec->>'item_count')::INTEGER, (v_mod_rec->>'quantity')::INTEGER, (v_mod_rec->>'count')::INTEGER, 1);
        END LOOP;

        -- ---------------------------------------------------------------------
        -- 5c. Compute per-item financial amounts
        -- ---------------------------------------------------------------------

        -- Base item amounts (no modifiers)
        v_item_subtotal_no_modifiers := v_item_unit_price * v_item_qty;
        v_item_tax_no_modifiers      := v_item_subtotal_no_modifiers * v_item_tax_rate;
        v_item_total_no_modifiers    := v_item_subtotal_no_modifiers + v_item_tax_no_modifiers;

        -- Full amounts including modifiers:
        --   subtotal = (unit_price + modifier_prices) x quantity
        v_item_subtotal := (v_item_unit_price + v_modifier_subtotal) * v_item_qty;
        v_item_tax      := v_item_subtotal * v_item_tax_rate;
        v_item_total    := v_item_subtotal + v_item_tax;

        -- ---------------------------------------------------------------------
        -- 5d. Insert TICKET_ITEM row
        -- ---------------------------------------------------------------------
        INSERT INTO TICKET_ITEM (
            TICKET_ID,
            ITEM_ID,
            ITEM_NAME,
            ITEM_QUANTITY,
            ITEM_COUNT,
            ITEM_PRICE,
            GROUP_NAME,
            CATEGORY_NAME,
            ITEM_TAX_RATE,
            SEAT_NUMBER,
            SUB_TOTAL,
            SUB_TOTAL_WITHOUT_MODIFIERS,
            TAX_AMOUNT,
            TAX_AMOUNT_WITHOUT_MODIFIERS,
            TOTAL_PRICE,
            TOTAL_PRICE_WITHOUT_MODIFIERS,
            HAS_MODIIERS,               -- note: column name has deliberate typo in schema
            BEVERAGE,
            INVENTORY_HANDLED,
            PRINT_TO_KITCHEN,
            TREAT_AS_SEAT,
            FRACTIONAL_UNIT,
            PRINTED_TO_KITCHEN,
            STOCK_AMOUNT_ADJUSTED,
            PIZZA_TYPE,
            STATUS
        ) VALUES (
            v_ticket_id,
            v_item_menu_item_id,
            v_item_name,
            v_item_qty,
            v_item_count,
            v_item_unit_price,
            v_item_group_name,
            v_item_category_name,
            v_item_tax_rate,
            v_item_seat_number,
            v_item_subtotal,
            v_item_subtotal_no_modifiers,
            v_item_tax,
            v_item_tax_no_modifiers,
            v_item_total,
            v_item_total_no_modifiers,
            -- HAS_MODIIERS = true when the modifiers array is non-empty
            CASE WHEN jsonb_array_length(COALESCE(v_item_rec->'modifiers', '[]'::jsonb)) > 0
                 THEN TRUE ELSE FALSE END,
            FALSE,      -- beverage
            FALSE,      -- inventory_handled
            TRUE,       -- print_to_kitchen
            FALSE,      -- treat_as_seat
            FALSE,      -- fractional_unit
            FALSE,      -- printed_to_kitchen : not yet sent to kitchen printer
            TRUE,       -- stock_amount_adjusted : adjusted in STEP 4 before ticket creation
            FALSE,      -- pizza_type
            'OPEN'
        )
        RETURNING ID INTO v_ticket_item_id;

        -- ---------------------------------------------------------------------
        -- 5e. Roll up item totals into ticket-level accumulators
        -- ---------------------------------------------------------------------
        v_ticket_subtotal := v_ticket_subtotal + v_item_subtotal;
        v_ticket_tax      := v_ticket_tax      + v_item_tax;
        v_ticket_total    := v_ticket_total    + v_item_total;

        -- =====================================================================
        -- STEP 7 — Inner loop: modifiers for this item
        -- =====================================================================
        FOR v_mod_rec IN
            SELECT jsonb_array_elements(v_item_rec->'modifiers')
        LOOP

            -- -----------------------------------------------------------------
            -- 6a. Extract per-modifier scalar fields
            -- -----------------------------------------------------------------
            v_mod_modifier_id := COALESCE(v_mod_rec->>'modifier_id', v_mod_rec->>'item_id', v_mod_rec->>'id')::INTEGER;
            v_mod_name        := v_mod_rec->>'name';
            v_mod_unit_price  := COALESCE((v_mod_rec->>'unit_price')::DOUBLE PRECISION, 0.0);
            v_mod_tax_rate    := COALESCE((v_mod_rec->>'tax_rate')::DOUBLE PRECISION, 0.0);
            v_mod_type        := COALESCE((v_mod_rec->>'modifier_type')::INTEGER, 1);
            v_mod_item_count  := COALESCE((v_mod_rec->>'item_count')::INTEGER, (v_mod_rec->>'quantity')::INTEGER, (v_mod_rec->>'count')::INTEGER, 1) * GREATEST(v_item_count, 1);
            v_mod_is_addon    := COALESCE((v_mod_rec->>'is_addon')::BOOLEAN, FALSE);

            -- -----------------------------------------------------------------
            -- 6b. Compute per-modifier financial amounts
            --     subtotal = unit_price x item_count
            -- -----------------------------------------------------------------
            v_mod_subtotal := v_mod_unit_price * v_mod_item_count;
            v_mod_tax      := v_mod_subtotal * v_mod_tax_rate;
            v_mod_total    := v_mod_subtotal + v_mod_tax;

            -- -----------------------------------------------------------------
            -- 6c. Insert TICKET_ITEM_MODIFIER row
            -- -----------------------------------------------------------------
            INSERT INTO TICKET_ITEM_MODIFIER (
                TICKET_ITEM_ID,
                ITEM_ID,
                MODIFIER_NAME,
                ITEM_COUNT,
                MODIFIER_PRICE,
                MODIFIER_TAX_RATE,
                MODIFIER_TYPE,
                SUBTOTAL_PRICE,
                TAX_AMOUNT,
                TOTAL_PRICE,
                INFO_ONLY,
                PRINT_TO_KITCHEN,
                PRINTED_TO_KITCHEN,
                STATUS
            ) VALUES (
                v_ticket_item_id,
                v_mod_modifier_id,
                v_mod_name,
                v_mod_item_count,
                v_mod_unit_price,
                v_mod_tax_rate,
                v_mod_type,
                v_mod_subtotal,
                v_mod_tax,
                v_mod_total,
                FALSE,      -- info_only
                TRUE,       -- print_to_kitchen
                FALSE,      -- printed_to_kitchen : not yet sent
                'ACTIVE'
            )
            RETURNING ID INTO v_modifier_row_id;

            -- -----------------------------------------------------------------
            -- 6d. Insert relation row
            --     is_addon = true  -> TICKET_ITEM_ADDON_RELATION
            --     is_addon = false -> TICKET_ITEM_MODIFIER_RELATION  (default)
            -- -----------------------------------------------------------------
            IF v_mod_is_addon THEN
                INSERT INTO TICKET_ITEM_ADDON_RELATION (
                    TICKET_ITEM_ID,
                    MODIFIER_ID,
                    LIST_ORDER
                ) VALUES (
                    v_ticket_item_id,
                    v_modifier_row_id,
                    v_mod_list_order
                );
            ELSE
                INSERT INTO TICKET_ITEM_MODIFIER_RELATION (
                    TICKET_ITEM_ID,
                    MODIFIER_ID,
                    LIST_ORDER
                ) VALUES (
                    v_ticket_item_id,
                    v_modifier_row_id,
                    v_mod_list_order
                );
            END IF;

            v_mod_list_order := v_mod_list_order + 1;

        END LOOP; -- END modifiers inner loop

        -- =====================================================================
        -- STEP 8 — Inner loop: cooking instructions for this item
        -- =====================================================================
        v_ci_order := 0;

        IF v_item_rec ? 'cooking_instructions' 
           AND jsonb_typeof(v_item_rec->'cooking_instructions') = 'array' THEN

            FOR v_ci_rec IN
                SELECT jsonb_array_elements(v_item_rec->'cooking_instructions')
            LOOP

                -- Support both simple string element ("No Onion") and object element ({"description": ...})
                IF jsonb_typeof(v_ci_rec) = 'string' THEN
                    v_ci_description := TRIM(v_ci_rec#>>'{}');
                    v_ci_printed     := FALSE;
                ELSE
                    v_ci_description := TRIM(v_ci_rec->>'description');
                    v_ci_printed     := COALESCE((v_ci_rec->>'printed_to_kitchen')::BOOLEAN, FALSE);
                END IF;

                -- Validation: discard null or empty whitespace strings
                IF v_ci_description IS NOT NULL AND LENGTH(v_ci_description) > 0 THEN
                    -- Truncate to 60 characters to satisfy VARCHAR(60) constraint
                    v_ci_description := SUBSTRING(v_ci_description FROM 1 FOR 60);

                    -- Deduplication check per ticket item
                    IF NOT EXISTS (
                        SELECT 1 
                        FROM TICKET_ITEM_COOKING_INSTRUCTION
                        WHERE TICKET_ITEM_ID = v_ticket_item_id 
                          AND description = v_ci_description
                    ) THEN
                        INSERT INTO TICKET_ITEM_COOKING_INSTRUCTION (
                            TICKET_ITEM_ID,
                            ITEM_ORDER,
                            description,
                            printedToKitchen
                        ) VALUES (
                            v_ticket_item_id,
                            v_ci_order,
                            v_ci_description,
                            v_ci_printed
                        );

                        v_ci_order := v_ci_order + 1;
                    END IF;
                END IF;

            END LOOP; -- END cooking instructions inner loop

        END IF;

    END LOOP; -- END items outer loop

    -- =========================================================================
    -- STEP 9 — Insert table numbers into TICKET_TABLE_NUM
    -- =========================================================================
    IF p_order_json ? 'table_id' AND (p_order_json->>'table_id') IS NOT NULL AND (p_order_json->>'table_id') <> '' THEN
        INSERT INTO TICKET_TABLE_NUM (ticket_id, TABLE_ID)
        VALUES (v_ticket_id, (p_order_json->>'table_id')::INTEGER);
    ELSIF p_order_json ? 'table_numbers' AND jsonb_typeof(p_order_json->'table_numbers') = 'array' THEN
        FOR v_table_num IN
            SELECT DISTINCT jsonb_array_elements_text(p_order_json->'table_numbers')::INTEGER
        LOOP
            INSERT INTO TICKET_TABLE_NUM (ticket_id, TABLE_ID)
            VALUES (v_ticket_id, v_table_num);
        END LOOP;
    END IF;

    -- =========================================================================
    -- STEP 10 — Update TICKET row with the fully computed totals
    -- =========================================================================
    UPDATE TICKET
    SET
        SUB_TOTAL       = v_ticket_subtotal,
        TOTAL_TAX       = v_ticket_tax,
        TOTAL_PRICE     = v_ticket_total + v_delivery_charge,
        DUE_AMOUNT      = v_ticket_total + v_delivery_charge,
        ACTIVE_DATE     = v_create_date
    WHERE
        ID = v_ticket_id;

    -- =========================================================================
    -- STEP 11 — Notify FloreantPOS of data change via DATA_UPDATE_INFO
    --   FloreantPOS uses this table to detect changes and sync terminal data.
    -- =========================================================================
    UPDATE DATA_UPDATE_INFO
    SET    LAST_UPDATE_TIME = v_create_date
    WHERE  ID = (SELECT MIN(ID) FROM DATA_UPDATE_INFO);

    -- =========================================================================
    -- STEP 12 — Return the new TICKET.ID to the calling API
    -- =========================================================================
    RETURN v_ticket_id;

EXCEPTION
    WHEN OTHERS THEN
        -- Re-raise the exception with contextual information for API-side logging
        RAISE EXCEPTION
            'sp_create_ticket_from_json failed [order_type=%, customer_mobile=%, customer_email=%]: %',
            v_order_type, v_cust_mobile, v_cust_email, SQLERRM;

END;
$$;

-- =============================================================================
-- PERMISSIONS — Adjust role name to match your deployment
-- =============================================================================
-- GRANT EXECUTE
--     ON FUNCTION sp_create_ticket_from_json(JSONB, INTEGER, INTEGER, INTEGER)
--     TO floreant_app_role;

-- =============================================================================
-- EXAMPLE CALL
-- Uncomment the block below and run it against your database to smoke-test
-- the procedure. Expected result: a positive INTEGER (the new TICKET.ID).
-- =============================================================================
/*
SELECT sp_create_ticket_from_json(
    p_order_json => '{
        "order_type"      : "Delivery",
        "number_of_guests": 1,
        "delivery_charge" : 3.50,
        "notes"           : "Ring doorbell twice",
        "table_numbers"   : [],

        "customer": {
            "first_name"   : "Maria",
            "last_name"    : "Lopez",
            "mobile_no"    : "5551234567",
            "email"        : "maria.lopez@example.com",
            "home_phone_no": "5559876543",
            "address"      : "123 Main St",
            "city"         : "Miami",
            "state"        : "FL",
            "zip_code"     : "33101",
            "note"         : "Loyal customer"
        },

        "delivery_address": {
            "address"        : "456 Ocean Dr, Apt 3B",
            "phone_extension": "3B",
            "room_no"        : null,
            "distance"       : 2.5
        },

        "items": [
            {
                "menu_item_id"  : 1,
                "name"          : "Cheeseburger",
                "quantity"      : 2,
                "unit_price"    : 8.99,
                "group_name"    : "Burgers",
                "category_name" : "Food",
                "tax_rate"      : 0.08,
                "seat_number"   : null,

                "modifiers": [
                    {
                        "modifier_id"  : 10,
                        "name"         : "Extra Cheese",
                        "unit_price"   : 1.50,
                        "tax_rate"     : 0.08,
                        "modifier_type": 1,
                        "item_count"   : 1,
                        "is_addon"     : false
                    }
                ],

                "cooking_instructions": [
                    { "description": "Well done",         "printed_to_kitchen": false },
                    { "description": "No pickles please", "printed_to_kitchen": false }
                ]
            },
            {
                "menu_item_id"  : 5,
                "name"          : "Large Fries",
                "quantity"      : 1,
                "unit_price"    : 3.49,
                "group_name"    : "Sides",
                "category_name" : "Food",
                "tax_rate"      : 0.08,
                "seat_number"   : null,
                "modifiers"     : [],
                "cooking_instructions": []
            }
        ]
    }'::jsonb,
    p_owner_id    => 1,
    p_terminal_id => 1,
    p_shift_id    => 1
);
*/
