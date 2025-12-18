package com.farmatodo.reto_tecnico.domain.model;

import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * OrderItem domain entity.
 * Represents a single line item in an order.
 * Pure domain model without persistence annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    /**
     * Unique identifier for the order item.
     */
    private UUID id;

    /**
     * Reference to the product.
     */
    @NotNull(message = "Product cannot be null")
    private Product product;

    /**
     * Quantity of the product ordered.
     */
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    /**
     * Unit price at the time of order (snapshot of product price).
     * Stored separately to maintain historical accuracy even if product price changes.
     */
    @NotNull(message = "Unit price cannot be null")
    private Money unitPrice;

    /**
     * Creates a new order item from a product and quantity.
     * Captures current product price as unit price.
     * @param product the product
     * @param quantity the quantity
     * @return new OrderItem instance
     */
    public static OrderItem create(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        return OrderItem.builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(quantity)
                .unitPrice(product.getPrice())
                .build();
    }

    /**
     * Calculates the subtotal for this order item.
     * @return Money representing quantity * unitPrice
     */
    public Money calculateSubtotal() {
        return unitPrice.multiply(quantity);
    }

    /**
     * Increases the quantity of this order item.
     * @param additionalQuantity the quantity to add
     */
    public void increaseQuantity(int additionalQuantity) {
        if (additionalQuantity < 1) {
            throw new IllegalArgumentException("Additional quantity must be at least 1");
        }
        this.quantity += additionalQuantity;
    }

    /**
     * Updates the quantity of this order item.
     * @param newQuantity the new quantity
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.quantity = newQuantity;
    }

    /**
     * Gets the product ID for this order item.
     * @return product UUID
     */
    public UUID getProductId() {
        return product.getId();
    }

    /**
     * Gets the product name for display purposes.
     * @return product name
     */
    public String getProductName() {
        return product.getName();
    }
}
