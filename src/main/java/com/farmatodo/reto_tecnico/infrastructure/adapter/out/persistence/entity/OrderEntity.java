package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity;

import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.converter.CryptoConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for Order persistence (Aggregate Root).
 * Maps to 'orders' table in PostgreSQL.
 * Manages the complete order lifecycle including items and payment.
 *
 * Design notes:
 * - Aggregate root for OrderItem entities (cascade operations)
 * - Payment token encrypted at rest using CryptoConverter
 * - Customer stored as FK reference
 * - Status tracked as enum string
 */
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_customer_id", columnList = "customer_id"),
    @Index(name = "idx_order_status", columnList = "status"),
    @Index(name = "idx_order_created_at", columnList = "created_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    /**
     * Reference to customer who placed the order.
     */
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    /**
     * Order items (one-to-many relationship).
     * Cascade ALL operations - order owns its items.
     * orphanRemoval ensures deleted items are removed from database.
     */
    @OneToMany(
        mappedBy = "order",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<OrderItemEntity> items = new ArrayList<>();

    /**
     * Total amount for the order.
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Current order status.
     * Stored as string enum value.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private OrderStatus status;

    /**
     * Timestamp when order was created.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when order was last updated.
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Encrypted payment token.
     * CRITICAL: Uses CryptoConverter for AES-GCM encryption at rest.
     */
    @Convert(converter = CryptoConverter.class)
    @Column(name = "payment_token", length = 500)
    private String paymentToken;

    /**
     * Order status enum matching domain model.
     */
    public enum OrderStatus {
        PENDING,
        PAYMENT_PROCESSING,
        PAYMENT_CONFIRMED,
        PAYMENT_FAILED,
        COMPLETED,
        CANCELLED
    }

    /**
     * Helper method to add order item and maintain bidirectional relationship.
     * @param item the order item to add
     */
    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.setOrder(this);
    }

    /**
     * Helper method to remove order item and maintain bidirectional relationship.
     * @param item the order item to remove
     */
    public void removeItem(OrderItemEntity item) {
        items.remove(item);
        item.setOrder(null);
    }

    /**
     * Lifecycle callback to set timestamps.
     */
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    /**
     * Lifecycle callback to update timestamp on modification.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
