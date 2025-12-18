-- ============================================================================
-- FARMATODO - SEED DATA FOR TESTING
-- ============================================================================
-- This script populates the database with realistic pharmacy products and
-- a test customer to facilitate evaluation and testing.
--
-- Features:
-- - 10 realistic pharmacy products with varied stock levels
-- - Some products with low stock to test InsufficientStockException
-- - 1 test customer for order creation
-- - Idempotent script using ON CONFLICT DO NOTHING (PostgreSQL)
-- ============================================================================

-- ============================================================================
-- PRODUCTS (10 realistic pharmacy items)
-- ============================================================================

-- Product 1: Acetaminofén 500mg (High stock)
INSERT INTO products (id, name, description, price, stock, version)
VALUES (
    '550e8400-e29b-41d4-a716-446655440001',
    'Acetaminofén 500mg',
    'Analgésico y antipirético de venta libre. Caja con 20 tabletas.',
    12500.00,
    150,
    0
) ON CONFLICT (id) DO NOTHING;

-- Product 2: Ibuprofeno 400mg (High stock)
INSERT INTO products (id, name, description, price, stock, version)
VALUES (
    '550e8400-e29b-41d4-a716-446655440002',
    'Ibuprofeno 400mg',
    'Antiinflamatorio no esteroideo (AINE). Caja con 30 cápsulas.',
    18900.00,
    200,
    0
) ON CONFLICT (id) DO NOTHING;

-- Product 3: Loratadina 10mg (Medium stock)
INSERT INTO products (id, name, description, price, stock, version)
VALUES (
    '550e8400-e29b-41d4-a716-446655440003',
    'Loratadina 10mg',
    'Antihistamínico para alergias. Caja con 10 tabletas.',
    8500.00,
    75,
    0
) ON CONFLICT (id) DO NOTHING;

-- Product 4: Omeprazol 20mg (Medium stock)
INSERT INTO products (id, name, description, price, stock, version)
VALUES (
    '550e8400-e29b-41d4-a716-446655440004',
    'Omeprazol 20mg',
    'Inhibidor de la bomba de protones. Protector gástrico. Caja con 14 cápsulas.',
    22000.00,
    100,
    0
) ON CONFLICT (id) DO NOTHING;

-- Product 5: Vitamina C 1000mg (High stock)
INSERT INTO products (id, name, description, price, stock, version)
VALUES (
    '550e8400-e29b-41d4-a716-446655440005',
    'Vitamina C 1000mg',
    'Suplemento vitamínico. Fortalece el sistema inmunológico. Frasco con 60 tabletas efervescentes.',
    35000.00,
    120,
    0
) ON CONFLICT (id) DO NOTHING;

-- Product 6: Protector Solar FPS 50+ (Low stock - will trigger error if ordering > 8)
INSERT INTO products (id, name, description, price, stock, version)
VALUES (
    '550e8400-e29b-41d4-a716-446655440006',
    'Protector Solar FPS 50+',
    'Protección de amplio espectro contra rayos UVA/UVB. Resistente al agua. 120ml.',
    45000.00,
    8,
    0
) ON CONFLICT (id) DO NOTHING;

-- Product 7: Alcohol Antiséptico 70% (Very low stock - will trigger error if ordering > 3)
INSERT INTO products (id, name, description, price, stock, version)
VALUES (
    '550e8400-e29b-41d4-a716-446655440007',
    'Alcohol Antiséptico 70%',
    'Desinfectante de uso externo. Ideal para higiene de manos. Frasco 350ml.',
    6500.00,
    3,
    0
) ON CONFLICT (id) DO NOTHING;

-- Product 8: Suero Oral (Medium stock)
INSERT INTO products (id, name, description, price, stock, version)
VALUES (
    '550e8400-e29b-41d4-a716-446655440008',
    'Suero Oral Sabor Naranja',
    'Solución de rehidratación oral. Recomendado para diarrea y deshidratación. Caja con 6 sobres.',
    14000.00,
    50,
    0
) ON CONFLICT (id) DO NOTHING;

-- Product 9: Termómetro Digital (Low stock - will trigger error if ordering > 5)
INSERT INTO products (id, name, description, price, stock, version)
VALUES (
    '550e8400-e29b-41d4-a716-446655440009',
    'Termómetro Digital',
    'Termómetro digital de lectura rápida (10 segundos). Incluye estuche protector.',
    28000.00,
    5,
    0
) ON CONFLICT (id) DO NOTHING;

-- Product 10: Mascarilla N95 (Critical stock - will trigger error if ordering > 2)
INSERT INTO products (id, name, description, price, stock, version)
VALUES (
    '550e8400-e29b-41d4-a716-446655440010',
    'Mascarilla N95 Pack x10',
    'Respirador de partículas N95. Filtración de 95% de partículas. Caja con 10 unidades.',
    55000.00,
    2,
    0
) ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- TEST CUSTOMER
-- ============================================================================

INSERT INTO customers (id, name, email, phone, address)
VALUES (
    '660e8400-e29b-41d4-a716-446655440001',
    'Juan Pérez García',
    'juan@test.com',
    '573001234567',
    'Calle 123 #45-67, Bogotá, Colombia'
) ON CONFLICT (id) DO NOTHING;

-- ============================================================================
-- SUMMARY
-- ============================================================================
-- Products inserted: 10
--   - High stock (>100): 4 products (Acetaminofén, Ibuprofeno, Vitamina C, Omeprazol)
--   - Medium stock (50-100): 3 products (Loratadina, Suero Oral, Protector Solar)
--   - Low stock (3-8): 2 products (Alcohol, Termómetro)
--   - Critical stock (≤2): 1 product (Mascarilla N95)
--
-- Customers inserted: 1 (juan@test.com)
--
-- TEST SCENARIOS:
-- 1. Normal order: Use products with high stock (IDs ending in 001-005)
-- 2. Insufficient stock error: Try ordering >8 of Protector Solar (006)
-- 3. Critical stock error: Try ordering >2 of Mascarilla N95 (010)
-- 4. Payment testing: Create order, then process payment with test card
-- ============================================================================
