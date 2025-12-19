-- ============================================================================
-- SCRIPT: Agregar columna delivery_address a tabla orders
-- ============================================================================
-- Ejecutar en: DBeaver / pgAdmin / cualquier cliente PostgreSQL
-- Base de datos: farmatodo_db
-- Fecha: 2025-12-19
-- ============================================================================

-- IMPORTANTE: Ejecutar todo el script de una sola vez (seleccionar todo y ejecutar)

-- Paso 1: Agregar columna delivery_address (nullable inicialmente)
ALTER TABLE orders
ADD COLUMN IF NOT EXISTS delivery_address VARCHAR(500);

-- Paso 2: Poblar columna con la dirección actual del cliente
UPDATE orders o
SET delivery_address = c.address
FROM customers c
WHERE o.customer_id = c.id
AND o.delivery_address IS NULL;

-- Paso 3: Agregar constraint NOT NULL
ALTER TABLE orders
ALTER COLUMN delivery_address SET NOT NULL;

-- Paso 4: Crear índice para performance (opcional pero recomendado)
CREATE INDEX IF NOT EXISTS idx_orders_delivery_address
ON orders(delivery_address);

-- Verificación: Mostrar algunos pedidos con su dirección
SELECT
    id,
    customer_id,
    delivery_address,
    status,
    created_at
FROM orders
ORDER BY created_at DESC
LIMIT 5;

-- ============================================================================
-- FIN DEL SCRIPT
-- ============================================================================
-- Si todo salió bien, deberías ver 5 pedidos con sus direcciones de entrega
-- ============================================================================
