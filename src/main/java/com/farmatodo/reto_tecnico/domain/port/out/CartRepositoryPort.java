package com.farmatodo.reto_tecnico.domain.port.out;

import com.farmatodo.reto_tecnico.domain.model.Cart;

import java.util.Optional;
import java.util.UUID;

/**
 * Output port for cart persistence operations.
 * Defines the contract for cart repository implementations.
 */
public interface CartRepositoryPort {

    /**
     * Saves a cart to the repository.
     *
     * @param cart the cart to save
     * @return the saved cart
     */
    Cart save(Cart cart);

    /**
     * Finds a cart by customer ID.
     *
     * @param customerId the customer ID
     * @return Optional containing the cart if found
     */
    Optional<Cart> findByCustomerId(UUID customerId);

    /**
     * Finds a cart by its ID.
     *
     * @param cartId the cart ID
     * @return Optional containing the cart if found
     */
    Optional<Cart> findById(UUID cartId);

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
