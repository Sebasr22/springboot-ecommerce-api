package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * JPA entity for Product persistence.
 * Maps to 'products' table in PostgreSQL.
 * Stores product catalog with pricing and stock information.
 *
 * IMPORTANT: Stock updates use optimistic locking (@Version) to prevent race conditions.
 * Additionally, atomic UPDATE queries ensure stock is never oversold.
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_name", columnList = "name")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock;

    /**
     * Optimistic locking version field.
     * Automatically incremented by JPA on each update.
     * Prevents lost updates in concurrent scenarios.
     */
    @Version
    @Column(name = "version")
    private Long version;
}
