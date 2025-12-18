package com.farmatodo.reto_tecnico.domain.port.in;

import com.farmatodo.reto_tecnico.domain.model.Cart;

import java.util.UUID;

/**
 * Use case for adding products to a shopping cart.
 */
public interface AddToCartUseCase {

    /**
     * Adds a product to the customer's cart.
     * If the cart doesn't exist, creates a new one.
     * If the product already exists in the cart, increases the quantity.
     *
     * @param customerId the customer ID
     * @param productId  the product ID to add
     * @param quantity   the quantity to add
     * @return the updated cart
     */
    Cart addToCart(UUID customerId, UUID productId, int quantity);
}
