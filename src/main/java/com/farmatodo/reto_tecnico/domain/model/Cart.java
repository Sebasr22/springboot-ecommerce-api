package com.farmatodo.reto_tecnico.domain.model;

import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Cart domain entity (Aggregate Root).
 * Represents a shopping cart containing items before checkout.
 * Pure domain model without persistence annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Cart {

    /**
     * Unique identifier for the cart.
     */
    private UUID id;

    /**
     * Customer who owns this cart.
     */
    @NotNull(message = "Customer ID cannot be null")
    private UUID customerId;

    /**
     * List of items in the cart.
     */
    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    /**
     * Timestamp when the cart was created.
     */
    private LocalDateTime createdAt;

    /**
     * Timestamp when the cart was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Adds a product to the cart.
     * If the product already exists, increases the quantity.
     * If it's a new product, adds it as a new item.
     *
     * @param product  the product to add
     * @param quantity the quantity to add
     */
    public void addProduct(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Check if product already exists in cart
        Optional<CartItem> existingItem = findItemByProductId(product.getId());

        if (existingItem.isPresent()) {
            // Product exists, increase quantity
            existingItem.get().increaseQuantity(quantity);
        } else {
            // New product, add as new item
            CartItem newItem = CartItem.builder()
                    .id(UUID.randomUUID())
                    .product(product)
                    .quantity(quantity)
                    .unitPrice(product.getPrice())
                    .build();
            items.add(newItem);
        }

        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes a product from the cart.
     *
     * @param productId the product ID to remove
     * @return true if item was removed, false if not found
     */
    public boolean removeProduct(UUID productId) {
        boolean removed = items.removeIf(item -> item.getProduct().getId().equals(productId));
        if (removed) {
            this.updatedAt = LocalDateTime.now();
        }
        return removed;
    }

    /**
     * Updates the quantity of a specific product in the cart.
     *
     * @param productId   the product ID
     * @param newQuantity the new quantity
     * @return true if updated, false if product not found
     */
    public boolean updateProductQuantity(UUID productId, int newQuantity) {
        if (newQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        Optional<CartItem> item = findItemByProductId(productId);
        if (item.isPresent()) {
            item.get().updateQuantity(newQuantity);
            this.updatedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    /**
     * Clears all items from the cart.
     */
    public void clear() {
        items.clear();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Calculates the total amount of all items in the cart.
     *
     * @return total amount
     */
    public Money calculateTotal() {
        if (items.isEmpty()) {
            return new Money(BigDecimal.ZERO);
        }

        BigDecimal total = items.stream()
                .map(CartItem::calculateSubtotal)
                .map(Money::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new Money(total);
    }

    /**
     * Checks if the cart is empty.
     *
     * @return true if cart has no items
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Gets the total number of items in the cart (sum of quantities).
     *
     * @return total item count
     */
    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    /**
     * Finds a cart item by product ID.
     *
     * @param productId the product ID to search for
     * @return Optional containing the cart item if found
     */
    private Optional<CartItem> findItemByProductId(UUID productId) {
        return items.stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();
    }
}
