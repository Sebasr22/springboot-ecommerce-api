package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for ProductEntity.
 * Provides CRUD operations and custom queries for product persistence.
 *
 * CRITICAL: Contains atomic stock update query to prevent race conditions.
 */
@Repository
public interface ProductJpaRepository extends JpaRepository<ProductEntity, UUID> {

    /**
     * Finds products by name (case-insensitive partial match).
     * @param name the product name to search
     * @return list of matching products
     */
    @Query("SELECT p FROM ProductEntity p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<ProductEntity> findByNameContainingIgnoreCase(@Param("name") String name);

    /**
     * Finds all products with stock > 0.
     * @return list of in-stock products
     */
    @Query("SELECT p FROM ProductEntity p WHERE p.stock > 0")
    List<ProductEntity> findAllInStock();

    /**
     * ATOMIC STOCK UPDATE - CRITICAL FOR RACE CONDITION FIX.
     *
     * Updates stock using database-level atomic operation.
     * This query GUARANTEES:
     * 1. Stock is never oversold (WHERE clause checks availability)
     * 2. No race condition between check and update (atomic operation)
     * 3. Returns 0 if insufficient stock, 1 if successful
     *
     * The WHERE clause with "p.stock >= :quantity" ensures the update
     * only happens if sufficient stock exists, preventing negative stock.
     *
     * IMPORTANT: Must be called within a transaction.
     *
     * @param id product ID
     * @param quantity quantity to reduce
     * @return number of rows updated (0 = failed/insufficient stock, 1 = success)
     */
    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock - :quantity " +
           "WHERE p.id = :id AND p.stock >= :quantity")
    int reduceStockAtomic(@Param("id") UUID id, @Param("quantity") int quantity);

    /**
     * Atomically increases stock.
     * Used for restocking or order cancellations.
     *
     * @param id product ID
     * @param quantity quantity to add
     * @return number of rows updated
     */
    @Modifying
    @Query("UPDATE ProductEntity p SET p.stock = p.stock + :quantity WHERE p.id = :id")
    int increaseStockAtomic(@Param("id") UUID id, @Param("quantity") int quantity);
}
