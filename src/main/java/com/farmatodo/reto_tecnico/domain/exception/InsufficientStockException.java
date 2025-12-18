package com.farmatodo.reto_tecnico.domain.exception;

import java.util.UUID;

/**
 * Exception thrown when a product does not have sufficient stock
 * to fulfill an order.
 */
public class InsufficientStockException extends DomainException {

    private final UUID productId;
    private final String productName;
    private final int availableStock;
    private final int requestedQuantity;

    /**
     * Creates a new InsufficientStockException.
     * @param productId the product ID
     * @param productName the product name
     * @param availableStock the available stock
     * @param requestedQuantity the requested quantity
     */
    public InsufficientStockException(UUID productId, String productName, int availableStock, int requestedQuantity) {
        super(String.format(
                "Insufficient stock for product '%s' (ID: %s). Available: %d, Requested: %d",
                productName, productId, availableStock, requestedQuantity
        ));
        this.productId = productId;
        this.productName = productName;
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }

    /**
     * Creates a new InsufficientStockException with simple message.
     * @param message the error message
     */
    public InsufficientStockException(String message) {
        super(message);
        this.productId = null;
        this.productName = null;
        this.availableStock = 0;
        this.requestedQuantity = 0;
    }

    public UUID getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }
}
