-- ============================================================================
-- STANDALONE FIX: credit_cards.customer_id Foreign Key
-- ============================================================================
-- Run this in DBeaver AFTER V2__add_foreign_keys.sql
-- Safe to run multiple times (idempotent)
-- ============================================================================

-- Step 1: Add customer_id column if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'credit_cards' AND column_name = 'customer_id'
    ) THEN
        ALTER TABLE credit_cards ADD COLUMN customer_id UUID;
        RAISE NOTICE 'Column added: credit_cards.customer_id';
    ELSE
        RAISE NOTICE 'Column already exists: credit_cards.customer_id (skipped)';
    END IF;
END $$;

-- Step 2: Update existing rows to use the first customer (avoid FK violation)
DO $$
DECLARE
    first_customer_id UUID;
    rows_updated INTEGER;
BEGIN
    -- Get the first customer ID
    SELECT id INTO first_customer_id FROM customers ORDER BY id LIMIT 1;

    IF first_customer_id IS NOT NULL THEN
        -- Update orphan credit cards
        UPDATE credit_cards
        SET customer_id = first_customer_id
        WHERE customer_id IS NULL;

        GET DIAGNOSTICS rows_updated = ROW_COUNT;
        RAISE NOTICE 'Updated % orphan credit_cards to customer: %', rows_updated, first_customer_id;
    ELSE
        RAISE NOTICE 'No customers found. If credit_cards has rows, FK will fail.';
    END IF;
END $$;

-- Step 3: Add NOT NULL constraint (only if all rows have customer_id)
DO $$
DECLARE
    null_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO null_count FROM credit_cards WHERE customer_id IS NULL;

    IF null_count = 0 THEN
        -- Safe to add NOT NULL
        ALTER TABLE credit_cards ALTER COLUMN customer_id SET NOT NULL;
        RAISE NOTICE 'Constraint added: credit_cards.customer_id NOT NULL';
    ELSE
        RAISE WARNING 'Cannot add NOT NULL: % rows still have NULL customer_id', null_count;
    END IF;
EXCEPTION
    WHEN others THEN
        RAISE NOTICE 'NOT NULL already set or error: %', SQLERRM;
END $$;

-- Step 4: Add Foreign Key constraint
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_credit_cards_customer_id'
        AND table_name = 'credit_cards'
    ) THEN
        ALTER TABLE credit_cards
        ADD CONSTRAINT fk_credit_cards_customer_id
        FOREIGN KEY (customer_id) REFERENCES customers(id)
        ON DELETE CASCADE;
        RAISE NOTICE 'FK created: credit_cards.customer_id -> customers.id';
    ELSE
        RAISE NOTICE 'FK already exists: fk_credit_cards_customer_id (skipped)';
    END IF;
END $$;

-- Step 5: Add index for performance
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes
        WHERE indexname = 'idx_credit_cards_customer_id'
    ) THEN
        CREATE INDEX idx_credit_cards_customer_id ON credit_cards(customer_id);
        RAISE NOTICE 'Index created: idx_credit_cards_customer_id';
    ELSE
        RAISE NOTICE 'Index already exists: idx_credit_cards_customer_id (skipped)';
    END IF;
END $$;

-- Verification
DO $$
DECLARE
    fk_exists BOOLEAN;
BEGIN
    SELECT EXISTS (
        SELECT 1 FROM information_schema.table_constraints
        WHERE constraint_name = 'fk_credit_cards_customer_id'
    ) INTO fk_exists;

    RAISE NOTICE '========================================';
    RAISE NOTICE 'CREDIT_CARDS FK FIX SUMMARY';
    RAISE NOTICE '========================================';
    IF fk_exists THEN
        RAISE NOTICE 'Status: SUCCESS';
        RAISE NOTICE 'FK fk_credit_cards_customer_id is active';
    ELSE
        RAISE WARNING 'Status: FAILED - FK not created';
    END IF;
    RAISE NOTICE '========================================';
END $$;
