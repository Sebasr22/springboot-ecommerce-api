package com.farmatodo.reto_tecnico.domain.port.in;

import com.farmatodo.reto_tecnico.domain.model.Cart;

import java.util.UUID;

/**
 * Use case for retrieving a shopping cart.
 */
public interface GetCartUseCase {

    /**
     * Retrieves the cart for a specific customer.
     * If the cart doesn't exist, creates an empty one.
     *
     * @param customerId the customer ID
     * @return the customer's cart
     */
    Cart getCart(UUID customerId);
}
