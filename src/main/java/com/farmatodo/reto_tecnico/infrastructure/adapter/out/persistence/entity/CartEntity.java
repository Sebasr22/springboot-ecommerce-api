package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JPA entity for shopping carts.
 * Represents the persistence layer model for a cart.
 */
@Entity
@Table(name = "carts", indexes = {
        @Index(name = "idx_cart_customer_id", columnList = "customer_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "customer_id", nullable = false, unique = true)
    private UUID customerId;

    @OneToMany(
            mappedBy = "cart",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    @Builder.Default
    private List<CartItemEntity> items = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Helper method to add a cart item and maintain bidirectional relationship.
     *
     * @param item the cart item to add
     */
    public void addItem(CartItemEntity item) {
        items.add(item);
        item.setCart(this);
    }

    /**
     * Helper method to remove a cart item and maintain bidirectional relationship.
     *
     * @param item the cart item to remove
     */
    public void removeItem(CartItemEntity item) {
        items.remove(item);
        item.setCart(null);
    }
}
