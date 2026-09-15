-- =============================================================================
-- Stored Procedure : sp_delete_ticket
-- Database         : FloreantPOS (PostgreSQL 9.5)
-- File             : database/postgres9.5/sp_delete_ticket.sql
-- Description      : Handles ticket cancellation/deletion atomically.
--                    - If unpaid: restores inventory stock for tracked items,
--                      deletes all kitchen tickets, items, modifiers, associations,
--                      releases tables in SHOP_TABLE_STATUS, and removes TICKET
--                      without evidence.
--                    - If paid: marks ticket VOIDED = TRUE, sets VOID_REASON,
--                      sets SETTLED = TRUE, and preserves financial audit.
--                    - Updates DATA_UPDATE_INFO so connected terminals refresh.
-- =============================================================================

DROP FUNCTION IF EXISTS sp_delete_ticket(INTEGER, TEXT);

CREATE OR REPLACE FUNCTION sp_delete_ticket(
    p_ticket_id   INTEGER,
    p_void_reason TEXT DEFAULT NULL
)
RETURNS BOOLEAN
LANGUAGE plpgsql
AS $$
DECLARE
    v_is_paid      BOOLEAN;
    v_paid_amount  DOUBLE PRECISION;
    v_is_settled   BOOLEAN;
    v_status       TEXT;
    v_now          TIMESTAMP WITHOUT TIME ZONE;
    v_void_user_id INTEGER;
BEGIN
    v_now := NOW();

    -- 1. Check ticket existence and state
    SELECT 
        COALESCE(PAID, FALSE), 
        COALESCE(PAID_AMOUNT, 0.0),
        COALESCE(SETTLED, FALSE),
        COALESCE(STATUS, '')
    INTO v_is_paid, v_paid_amount, v_is_settled, v_status
    FROM TICKET
    WHERE ID = p_ticket_id;

    IF NOT FOUND THEN
        -- Ticket does not exist or was already deleted
        RETURN TRUE;
    END IF;

    -- If ticket is already settled or completed, cannot cancel
    IF v_is_settled = TRUE OR v_status ILIKE '%Completed%' THEN
        RETURN FALSE;
    END IF;

    -- 2. If ticket is paid, mark as VOIDED (Floreant paid ticket void behavior)
    IF v_is_paid = TRUE OR v_paid_amount > 0.0 THEN
        SELECT AUTO_ID INTO v_void_user_id
        FROM USERS
        WHERE USER_NAME = 'ONLINE_USER'
        LIMIT 1;

        IF v_void_user_id IS NULL THEN
            v_void_user_id := 1;
        END IF;

        UPDATE TICKET 
        SET VOIDED = TRUE,
            VOID_REASON = COALESCE(p_void_reason, 'Cancelled by customer'),
            VOID_BY_USER = v_void_user_id,
            SETTLED = TRUE,
            CLOSING_DATE = v_now,
            UPDATED_AT = v_now
        WHERE ID = p_ticket_id AND COALESCE(VOIDED, FALSE) = FALSE;

        -- Release table status if any
        DELETE FROM TABLE_TICKET_NUM WHERE TICKET_ID = p_ticket_id;

        UPDATE SHOP_TABLE_STATUS
        SET TABLE_STATUS = 5
        WHERE ID IN (
            SELECT TABLE_ID FROM TICKET_TABLE_NUM WHERE ticket_id = p_ticket_id
        )
        AND ID NOT IN (
            SELECT DISTINCT SHOP_TABLE_STATUS_ID FROM TABLE_TICKET_NUM
        );

        UPDATE DATA_UPDATE_INFO 
        SET LAST_UPDATE_TIME = v_now 
        WHERE ID = (SELECT MIN(ID) FROM DATA_UPDATE_INFO);

        RETURN TRUE;
    END IF;

    -- 3. If ticket is unpaid, delete without evidence
    -- 3a. Restore stock in MENU_ITEM for items with inventory tracking enabled
    UPDATE MENU_ITEM mi
    SET STOCK_AMOUNT = mi.STOCK_AMOUNT + agg.total_qty,
        UPDATED_AT = v_now
    FROM (
        SELECT ITEM_ID, SUM(COALESCE(ITEM_QUANTITY, 1.0)) AS total_qty
        FROM TICKET_ITEM
        WHERE TICKET_ID = p_ticket_id AND ITEM_ID IS NOT NULL
        GROUP BY ITEM_ID
    ) agg
    WHERE mi.ID = agg.ITEM_ID
      AND (COALESCE(mi.DISABLE_WHEN_STOCK_AMOUNT_IS_ZERO, FALSE) = TRUE OR COALESCE(mi.STOCK_AMOUNT, 0.0) > 0.0);

    -- 3b. Delete kitchen tickets and items
    DELETE FROM KITCHEN_TICKET_ITEM 
    WHERE KITHEN_TICKET_ID IN (SELECT ID FROM KITCHEN_TICKET WHERE TICKET_ID = p_ticket_id);

    DELETE FROM KIT_TICKET_TABLE_NUM 
    WHERE kit_ticket_id IN (SELECT ID FROM KITCHEN_TICKET WHERE TICKET_ID = p_ticket_id);

    DELETE FROM KITCHEN_TICKET 
    WHERE TICKET_ID = p_ticket_id;

    -- 3c. Delete ticket item modifier relations and child tables
    DELETE FROM TICKET_ITEM_MODIFIER_RELATION 
    WHERE TICKET_ITEM_ID IN (SELECT ID FROM TICKET_ITEM WHERE TICKET_ID = p_ticket_id);

    DELETE FROM TICKET_ITEM_ADDON_RELATION 
    WHERE TICKET_ITEM_ID IN (SELECT ID FROM TICKET_ITEM WHERE TICKET_ID = p_ticket_id);

    DELETE FROM TICKET_ITEM_COOKING_INSTRUCTION 
    WHERE TICKET_ITEM_ID IN (SELECT ID FROM TICKET_ITEM WHERE TICKET_ID = p_ticket_id);

    DELETE FROM TICKET_ITEM_DISCOUNT 
    WHERE TICKET_ITEMID IN (SELECT ID FROM TICKET_ITEM WHERE TICKET_ID = p_ticket_id);

    DELETE FROM TICKET_ITEM_MODIFIER 
    WHERE TICKET_ITEM_ID IN (SELECT ID FROM TICKET_ITEM WHERE TICKET_ID = p_ticket_id)
       OR ID IN (SELECT SIZE_MODIFIER_ID FROM TICKET_ITEM WHERE TICKET_ID = p_ticket_id AND SIZE_MODIFIER_ID IS NOT NULL);

    -- 3d. Delete ticket items
    DELETE FROM TICKET_ITEM WHERE TICKET_ID = p_ticket_id;

    -- 3e. Release table status and delete table associations
    DELETE FROM TABLE_TICKET_NUM WHERE TICKET_ID = p_ticket_id;

    UPDATE SHOP_TABLE_STATUS
    SET TABLE_STATUS = 5
    WHERE ID IN (
        SELECT TABLE_ID FROM TICKET_TABLE_NUM WHERE ticket_id = p_ticket_id
    )
    AND ID NOT IN (
        SELECT DISTINCT SHOP_TABLE_STATUS_ID FROM TABLE_TICKET_NUM
    );

    DELETE FROM TICKET_TABLE_NUM WHERE ticket_id = p_ticket_id;

    -- 3f. Delete remaining ticket associations
    DELETE FROM TICKET_PROPERTIES WHERE id = p_ticket_id;
    DELETE FROM TICKET_DISCOUNT WHERE TICKET_ID = p_ticket_id;
    DELETE FROM GUEST_CHECK_PRINT WHERE TICKET_ID = p_ticket_id;
    DELETE FROM TRANSACTIONS WHERE TICKET_ID = p_ticket_id;

    -- Clear gratuity FK if exists, then delete gratuity
    UPDATE TICKET SET GRATUITY_ID = NULL WHERE ID = p_ticket_id;
    DELETE FROM GRATUITY WHERE TICKET_ID = p_ticket_id;

    -- 3g. Delete ticket header
    DELETE FROM TICKET WHERE ID = p_ticket_id;

    -- 3h. Touch DATA_UPDATE_INFO for immediate terminal synchronization
    UPDATE DATA_UPDATE_INFO 
    SET LAST_UPDATE_TIME = v_now 
    WHERE ID = (SELECT MIN(ID) FROM DATA_UPDATE_INFO);

    RETURN TRUE;
END;
$$;
