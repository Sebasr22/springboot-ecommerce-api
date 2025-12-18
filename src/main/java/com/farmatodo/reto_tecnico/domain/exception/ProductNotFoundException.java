package com.farmatodo.reto_tecnico.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a product is not found.
 */
public class ProductNotFoundException extends DomainException {

    private final UUID productId;

    /**
     * Creates a new ProductNotFoundException.
     * @param productId the product ID that was not found
     */
    public ProductNotFoundException(UUID productId) {
        super(String.format("Product not found with ID: %s", productId));
        this.productId = productId;
    }

    /**
     * Creates a new ProductNotFoundException with custom message.
     * @param message the error message
     */
    public ProductNotFoundException(String message) {
        super(message);
        this.productId = null;
    }

    public UUID getProductId() {
        return productId;
    }
}
