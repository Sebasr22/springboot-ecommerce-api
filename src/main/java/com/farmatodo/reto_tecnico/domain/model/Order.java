package com.farmatodo.reto_tecnico.domain.model;

import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Order domain entity (Aggregate Root).
 * Represents a customer order with items and payment information.
 * Pure domain model without persistence annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    /**
     * Unique identifier for the order.
     */
    private UUID id;

    /**
     * Reference to the customer who placed the order.
     */
    @NotNull(message = "Customer cannot be null")
    private Customer customer;

    /**
     * List of items in the order.
     */
    @NotEmpty(message = "Order must have at least one item")
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /**
     * Total amount of the order.
     */
    @NotNull(message = "Total amount cannot be null")
    private Money totalAmount;

    /**
     * Current status of the order.
     */
    @NotNull(message = "Status cannot be null")
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * Timestamp when the order was created.
     */
    @NotNull(message = "Created date cannot be null")
    private LocalDateTime createdAt;

    /**
     * Timestamp when the order was last updated.
     */
    private LocalDateTime updatedAt;

    /**
     * Payment token used for this order.
     */
    private String paymentToken;

    /**
     * Order status enum.
     */
    public enum OrderStatus {
        PENDING,
        PAYMENT_PROCESSING,
        PAYMENT_CONFIRMED,
        PAYMENT_FAILED,
        COMPLETED,
        CANCELLED
    }

    /**
     * Creates a new order from a customer and items.
     * Automatically calculates total and sets timestamps.
     * @param customer the customer
     * @param items list of order items
     * @return new Order instance
     */
    public static Order create(Customer customer, List<OrderItem> items) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }

        Money total = calculateTotalFromItems(items);
        LocalDateTime now = LocalDateTime.now();

        return Order.builder()
                .id(UUID.randomUUID())
                .customer(customer)
                .items(new ArrayList<>(items))
                .totalAmount(total)
                .status(OrderStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    /**
     * Calculates total amount from order items.
     * @param items list of order items
     * @return total Money amount
     */
    private static Money calculateTotalFromItems(List<OrderItem> items) {
        return items.stream()
                .map(OrderItem::calculateSubtotal)
                .reduce(Money.zero(), Money::add);
    }

    /**
     * Adds an item to the order.
     * Recalculates total.
     * @param item the order item to add
     */
    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Order item cannot be null");
        }
        if (!this.status.equals(OrderStatus.PENDING)) {
            throw new IllegalStateException("Cannot add items to a non-pending order");
        }

        this.items.add(item);
        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Removes an item from the order.
     * Recalculates total.
     * @param itemId the ID of the item to remove
     */
    public void removeItem(UUID itemId) {
        if (!this.status.equals(OrderStatus.PENDING)) {
            throw new IllegalStateException("Cannot remove items from a non-pending order");
        }

        boolean removed = this.items.removeIf(item -> item.getId().equals(itemId));
        if (!removed) {
            throw new IllegalArgumentException("Item not found in order");
        }

        if (this.items.isEmpty()) {
            throw new IllegalStateException("Order must have at least one item");
        }

        recalculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Recalculates the total amount from current items.
     */
    public void recalculateTotal() {
        this.totalAmount = calculateTotalFromItems(this.items);
    }

    /**
     * Assigns a payment token to the order and updates status.
     * @param token the payment token
     */
    public void assignPaymentToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("Payment token cannot be null or blank");
        }
        this.paymentToken = token;
        this.status = OrderStatus.PAYMENT_PROCESSING;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Confirms payment for the order.
     */
    public void confirmPayment() {
        if (!this.status.equals(OrderStatus.PAYMENT_PROCESSING)) {
            throw new IllegalStateException("Cannot confirm payment for order not in processing state");
        }
        this.status = OrderStatus.PAYMENT_CONFIRMED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks payment as failed.
     * Validates that payment failure can only occur from valid states.
     */
    public void failPayment() {
        if (!this.status.equals(OrderStatus.PAYMENT_PROCESSING) &&
            !this.status.equals(OrderStatus.PENDING)) {
            throw new IllegalStateException(
                "Cannot fail payment from state: " + this.status +
                ". Payment can only fail from PENDING or PAYMENT_PROCESSING states."
            );
        }
        this.status = OrderStatus.PAYMENT_FAILED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Completes the order.
     */
    public void complete() {
        if (!this.status.equals(OrderStatus.PAYMENT_CONFIRMED)) {
            throw new IllegalStateException("Cannot complete order without payment confirmation");
        }
        this.status = OrderStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Cancels the order.
     */
    public void cancel() {
        if (this.status.equals(OrderStatus.COMPLETED)) {
            throw new IllegalStateException("Cannot cancel a completed order");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if the order can be cancelled.
     * @return true if order can be cancelled
     */
    public boolean isCancellable() {
        return !this.status.equals(OrderStatus.COMPLETED) &&
               !this.status.equals(OrderStatus.CANCELLED);
    }

    /**
     * Gets the total number of items in the order.
     * @return total item count
     */
    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    /**
     * Gets the customer ID for this order.
     * @return customer UUID
     */
    public UUID getCustomerId() {
        return customer.getId();
    }
}
