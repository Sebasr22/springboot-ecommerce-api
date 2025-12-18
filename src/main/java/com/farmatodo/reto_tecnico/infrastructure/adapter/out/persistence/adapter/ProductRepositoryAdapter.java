package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.adapter;

import com.farmatodo.reto_tecnico.domain.exception.InsufficientStockException;
import com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.port.out.ProductRepositoryPort;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.ProductMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementation for Product persistence.
 * Implements hexagonal architecture output port using JPA repository.
 * Translates between domain Product and JPA ProductEntity.
 *
 * CRITICAL: Contains atomic stock update logic to prevent race conditions.
 * Uses database-level atomic query for stock reduction.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ProductRepositoryAdapter implements ProductRepositoryPort {

    private final ProductJpaRepository jpaRepository;
    private final ProductMapper mapper;

    @Override
    public Product save(Product product) {
        log.debug("Saving product: {}", product.getId());
        ProductEntity entity = mapper.toEntity(product);
        ProductEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Product> findById(UUID id) {
        log.debug("Finding product by ID: {}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Product> findByNameContaining(String query) {
        log.debug("Searching products by name: '{}'", query);
        List<ProductEntity> entities = jpaRepository.findByNameContainingIgnoreCase(query);
        return mapper.toDomainList(entities);
    }

    @Override
    public List<Product> findAllInStock() {
        log.debug("Finding all products in stock");
        List<ProductEntity> entities = jpaRepository.findAllInStock();
        return mapper.toDomainList(entities);
    }

    @Override
    public List<Product> findByStockLessThan(int threshold) {
        log.debug("Finding products with stock less than: {}", threshold);
        return jpaRepository.findAll().stream()
                .filter(p -> p.getStock() < threshold)
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<Product> findAll() {
        log.debug("Finding all products");
        return mapper.toDomainList(jpaRepository.findAll());
    }

    /**
     * Updates stock using ATOMIC database query.
     * This is the CRITICAL FIX for the race condition (Red Flag #1).
     *
     * How it works:
     * 1. Uses ProductJpaRepository.reduceStockAtomic() - database-level atomic UPDATE
     * 2. Query only updates if stock >= quantity (prevents overselling)
     * 3. Returns 0 if insufficient stock, 1 if successful
     * 4. Throws InsufficientStockException if atomic update fails
     *
     * IMPORTANT: Must be called within a transaction.
     *
     * @param productId the product ID
     * @param newStock the new stock value
     * @return updated product
     * @throws ProductNotFoundException if product not found
     * @throws InsufficientStockException if stock cannot be updated atomically
     */
    @Override
    @Transactional
    public Product updateStock(UUID productId, int newStock) {
        log.debug("Updating stock for product {} to {}", productId, newStock);

        // First, verify product exists
        ProductEntity product = jpaRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Calculate quantity to reduce
        int currentStock = product.getStock();
        int quantityToReduce = currentStock - newStock;

        if (quantityToReduce < 0) {
            // Increasing stock - use simple update
            int rowsUpdated = jpaRepository.increaseStockAtomic(productId, Math.abs(quantityToReduce));
            if (rowsUpdated == 0) {
                throw new RuntimeException("Failed to increase stock for product: " + productId);
            }
        } else if (quantityToReduce > 0) {
            // Reducing stock - use atomic query to prevent race condition
            int rowsUpdated = jpaRepository.reduceStockAtomic(productId, quantityToReduce);

            if (rowsUpdated == 0) {
                // Atomic update failed - either product not found or insufficient stock
                log.warn("Atomic stock reduction failed for product {} - insufficient stock or product not found",
                        productId);
                throw new InsufficientStockException(
                        productId,
                        product.getName(),
                        quantityToReduce,
                        currentStock
                );
            }

            log.info("Successfully reduced stock atomically for product {} by {} units",
                    productId, quantityToReduce);
        }
        // else quantityToReduce == 0, no update needed

        // Fetch updated product and return
        ProductEntity updated = jpaRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return mapper.toDomain(updated);
    }

    @Override
    public boolean deleteById(UUID id) {
        log.debug("Deleting product: {}", id);
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
