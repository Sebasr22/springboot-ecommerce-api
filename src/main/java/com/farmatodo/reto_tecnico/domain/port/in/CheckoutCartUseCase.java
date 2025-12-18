package com.farmatodo.reto_tecnico.domain.port.in;

import com.farmatodo.reto_tecnico.domain.model.Order;

import java.util.UUID;

/**
 * Use case for checking out a shopping cart.
 * Converts the cart items into an order and clears the cart.
 */
public interface CheckoutCartUseCase {

    /**
     * Checks out the cart for a customer.
     * Creates an order from the cart items and clears the cart.
     *
     * @param customerId the customer ID
     * @return the created order
     */
    Order checkout(UUID customerId);
}
