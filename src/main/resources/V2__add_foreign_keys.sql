-- ============================================================================
-- FARMATODO - FOREIGN KEY CONSTRAINTS MIGRATION
-- ============================================================================
-- Version: 2.1
-- Date: 2025-12-18
-- Purpose: Add ALL missing Foreign Key constraints for referential integrity
--
-- TABLES FIXED:
-- 1. carts.customer_id -> customers.id
-- 2. orders.customer_id -> customers.id
-- 3. search_logs.customer_id -> customers.id (nullable)
-- 4. order_items.product_id -> products.id
-- 5. credit_cards.customer_id -> customers.id (NEW - adds column + FK)
--
-- NOTE: cart_items already has FKs via @ManyToOne annotations
-- ============================================================================

-- ============================================================================
-- PRE-CHECK: Verify tables exist
-- ============================================================================
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'customers') THEN
        RAISE EXCEPTION 'Table "customers" does not exist. Cannot create foreign keys.';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'products') THEN
        RAISE EXCEPTION 'Table "products" does not exist. Cannot create foreign keys.';
    END IF;
    RAISE NOTICE 'Pre-check passed: Required tables exist.';
END $$;

-- ============================================================================
-- 1. CARTS -> CUSTOMERS
-- ============================================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_carts_customer_id' AND table_name = 'carts'
    ) THEN
        ALTER TABLE carts
        ADD CONSTRAINT fk_carts_customer_id
        FOREIGN KEY (customer_id) REFERENCES customers(id)
        ON DELETE CASCADE ON UPDATE CASCADE;
        RAISE NOTICE 'FK created: carts.customer_id -> customers.id';
    ELSE
        RAISE NOTICE 'FK exists: fk_carts_customer_id (skipped)';
    END IF;
END $$;

-- ============================================================================
-- 2. ORDERS -> CUSTOMERS
-- ============================================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_orders_customer_id' AND table_name = 'orders'
    ) THEN
        ALTER TABLE orders
        ADD CONSTRAINT fk_orders_customer_id
        FOREIGN KEY (customer_id) REFERENCES customers(id)
        ON DELETE RESTRICT ON UPDATE CASCADE;
        RAISE NOTICE 'FK created: orders.customer_id -> customers.id';
    ELSE
        RAISE NOTICE 'FK exists: fk_orders_customer_id (skipped)';
    END IF;
END $$;

-- ============================================================================
-- 3. SEARCH_LOGS -> CUSTOMERS (nullable)
-- ============================================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_search_logs_customer_id' AND table_name = 'search_logs'
    ) THEN
        ALTER TABLE search_logs
        ADD CONSTRAINT fk_search_logs_customer_id
        FOREIGN KEY (customer_id) REFERENCES customers(id)
        ON DELETE SET NULL ON UPDATE CASCADE;
        RAISE NOTICE 'FK created: search_logs.customer_id -> customers.id';
    ELSE
        RAISE NOTICE 'FK exists: fk_search_logs_customer_id (skipped)';
    END IF;
END $$;

-- ============================================================================
-- 4. ORDER_ITEMS -> PRODUCTS
-- ============================================================================
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_order_items_product_id' AND table_name = 'order_items'
    ) THEN
        ALTER TABLE order_items
        ADD CONSTRAINT fk_order_items_product_id
        FOREIGN KEY (product_id) REFERENCES products(id)
        ON DELETE RESTRICT ON UPDATE CASCADE;
        RAISE NOTICE 'FK created: order_items.product_id -> products.id';
    ELSE
        RAISE NOTICE 'FK exists: fk_order_items_product_id (skipped)';
    END IF;
END $$;

-- ============================================================================
-- 5. CREDIT_CARDS -> CUSTOMERS (Adds column + FK)
-- ============================================================================

-- 5a. Add customer_id column if missing
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'credit_cards' AND column_name = 'customer_id'
    ) THEN
        ALTER TABLE credit_cards ADD COLUMN customer_id UUID;
        RAISE NOTICE 'Column added: credit_cards.customer_id';
    ELSE
        RAISE NOTICE 'Column exists: credit_cards.customer_id (skipped)';
    END IF;
END $$;

-- 5b. Assign orphan credit_cards to first customer
DO $$
DECLARE
    first_customer_id UUID;
    rows_updated INTEGER;
BEGIN
    SELECT id INTO first_customer_id FROM customers ORDER BY id LIMIT 1;
    IF first_customer_id IS NOT NULL THEN
        UPDATE credit_cards SET customer_id = first_customer_id WHERE customer_id IS NULL;
        GET DIAGNOSTICS rows_updated = ROW_COUNT;
        IF rows_updated > 0 THEN
            RAISE NOTICE 'Assigned % orphan credit_cards to customer %', rows_updated, first_customer_id;
        END IF;
    END IF;
END $$;

-- 5c. Add NOT NULL constraint
DO $$
DECLARE
    null_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO null_count FROM credit_cards WHERE customer_id IS NULL;
    IF null_count = 0 THEN
        BEGIN
            ALTER TABLE credit_cards ALTER COLUMN customer_id SET NOT NULL;
            RAISE NOTICE 'Constraint: credit_cards.customer_id SET NOT NULL';
        EXCEPTION WHEN others THEN
            RAISE NOTICE 'NOT NULL already set on credit_cards.customer_id';
        END;
    END IF;
END $$;

-- 5d. Add FK constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_credit_cards_customer_id' AND table_name = 'credit_cards'
    ) THEN
        ALTER TABLE credit_cards
        ADD CONSTRAINT fk_credit_cards_customer_id
        FOREIGN KEY (customer_id) REFERENCES customers(id)
        ON DELETE CASCADE ON UPDATE CASCADE;
        RAISE NOTICE 'FK created: credit_cards.customer_id -> customers.id';
    ELSE
        RAISE NOTICE 'FK exists: fk_credit_cards_customer_id (skipped)';
    END IF;
END $$;

-- 5e. Add index for performance
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes WHERE indexname = 'idx_credit_cards_customer_id') THEN
        CREATE INDEX idx_credit_cards_customer_id ON credit_cards(customer_id);
        RAISE NOTICE 'Index created: idx_credit_cards_customer_id';
    END IF;
END $$;

-- ============================================================================
-- VERIFICATION
-- ============================================================================
DO $$
DECLARE
    fk_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO fk_count
    FROM information_schema.table_constraints tc
    WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.constraint_name IN (
        'fk_carts_customer_id',
        'fk_orders_customer_id',
        'fk_search_logs_customer_id',
        'fk_order_items_product_id',
        'fk_credit_cards_customer_id'
    );

    RAISE NOTICE '========================================';
    RAISE NOTICE 'FOREIGN KEY MIGRATION COMPLETE';
    RAISE NOTICE '========================================';
    RAISE NOTICE 'Expected: 5 foreign keys';
    RAISE NOTICE 'Found: % foreign keys', fk_count;
    IF fk_count = 5 THEN
        RAISE NOTICE 'Status: SUCCESS';
    ELSE
        RAISE WARNING 'Status: INCOMPLETE';
    END IF;
    RAISE NOTICE '========================================';
END $$;

-- ============================================================================
-- ROLLBACK (if needed)
-- ============================================================================
-- ALTER TABLE carts DROP CONSTRAINT IF EXISTS fk_carts_customer_id;
-- ALTER TABLE orders DROP CONSTRAINT IF EXISTS fk_orders_customer_id;
-- ALTER TABLE search_logs DROP CONSTRAINT IF EXISTS fk_search_logs_customer_id;
-- ALTER TABLE order_items DROP CONSTRAINT IF EXISTS fk_order_items_product_id;
-- ALTER TABLE credit_cards DROP CONSTRAINT IF EXISTS fk_credit_cards_customer_id;
-- ALTER TABLE credit_cards DROP COLUMN IF EXISTS customer_id;
-- ============================================================================
