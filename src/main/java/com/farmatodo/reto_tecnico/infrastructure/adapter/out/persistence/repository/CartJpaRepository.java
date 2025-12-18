package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for cart persistence operations.
 */
@Repository
public interface CartJpaRepository extends JpaRepository<CartEntity, UUID> {

    /**
     * Finds a cart by customer ID.
     *
     * @param customerId the customer ID
     * @return Optional containing the cart entity if found
     */
    Optional<CartEntity> findByCustomerId(UUID customerId);

    /**
     * Deletes a cart by customer ID.
     *
     * @param customerId the customer ID
     */
    void deleteByCustomerId(UUID customerId);

    /**
     * Checks if a cart exists for a customer.
     *
     * @param customerId the customer ID
     * @return true if cart exists
     */
    boolean existsByCustomerId(UUID customerId);
}
