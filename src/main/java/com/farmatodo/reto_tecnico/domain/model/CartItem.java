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
 * CartItem domain entity.
 * Represents a single item in a shopping cart.
 * Pure domain model without persistence annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    /**
     * Unique identifier for the cart item.
     */
    private UUID id;

    /**
     * Reference to the product in the cart.
     */
    @NotNull(message = "Product cannot be null")
    private Product product;

    /**
     * Quantity of the product in the cart.
     */
    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    /**
     * Unit price at the time the item was added to cart.
     * Captured to handle price changes between adding to cart and checkout.
     */
    @NotNull(message = "Unit price cannot be null")
    private Money unitPrice;

    /**
     * Calculates the subtotal for this cart item.
     * @return subtotal (unitPrice * quantity)
     */
    public Money calculateSubtotal() {
        return unitPrice.multiply(quantity);
    }

    /**
     * Increases the quantity by the specified amount.
     * @param additionalQuantity quantity to add
     */
    public void increaseQuantity(int additionalQuantity) {
        if (additionalQuantity <= 0) {
            throw new IllegalArgumentException("Additional quantity must be positive");
        }
        this.quantity += additionalQuantity;
    }

    /**
     * Updates the quantity to a specific value.
     * @param newQuantity new quantity value
     */
    public void updateQuantity(int newQuantity) {
        if (newQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.quantity = newQuantity;
    }
}
