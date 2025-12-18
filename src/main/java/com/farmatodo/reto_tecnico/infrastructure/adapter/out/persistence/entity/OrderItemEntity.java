package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity for OrderItem persistence.
 * Maps to 'order_items' table in PostgreSQL.
 * Represents individual line items within an order.
 *
 * Design notes:
 * - Stores product_id as FK rather than full product object
 * - Captures unit_price at order time for historical accuracy
 * - Part of Order aggregate (cascade operations from Order)
 */
@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_item_order_id", columnList = "order_id"),
    @Index(name = "idx_order_item_product_id", columnList = "product_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Reference to the product ordered.
     * Stored as FK to maintain referential integrity.
     */
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    /**
     * Quantity of the product in this order item.
     */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * Unit price at time of order.
     * Captured to preserve historical pricing even if product price changes.
     */
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    /**
     * Reference to parent order.
     * Managed by OrderEntity (mappedBy).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;
}
