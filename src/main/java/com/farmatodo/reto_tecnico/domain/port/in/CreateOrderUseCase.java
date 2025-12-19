package com.farmatodo.reto_tecnico.domain.port.in;

import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.OrderItem;

import java.util.List;
import java.util.UUID;

/**
 * Input port for order creation use case.
 * Defines the contract for creating and managing orders.
 * Implementation will handle stock validation, order persistence, and business rules.
 */
public interface CreateOrderUseCase {

    /**
     * Creates a new order from customer and items.
     * Validates stock availability before creating the order.
     * Uses customer's default address as delivery address.
     * @param customer the customer placing the order
     * @param items list of order items
     * @return created order
     * @throws com.farmatodo.reto_tecnico.domain.exception.InsufficientStockException if stock is not available
     * @throws IllegalArgumentException if input is invalid
     */
    Order createOrder(Customer customer, List<OrderItem> items);

    /**
     * Creates a new order from customer and items with explicit delivery address.
     * Validates stock availability before creating the order.
     * @param customer the customer placing the order
     * @param items list of order items
     * @param explicitDeliveryAddress optional delivery address (if null, uses customer's address)
     * @return created order
     * @throws com.farmatodo.reto_tecnico.domain.exception.InsufficientStockException if stock is not available
     * @throws IllegalArgumentException if input is invalid
     */
    Order createOrder(Customer customer, List<OrderItem> items, String explicitDeliveryAddress);

    /**
     * Creates a new order from customer ID and items.
     * Uses customer's default address as delivery address.
     * @param customerId the customer ID
     * @param items list of order items
     * @return created order
     * @throws com.farmatodo.reto_tecnico.domain.exception.CustomerNotFoundException if customer not found
     * @throws com.farmatodo.reto_tecnico.domain.exception.InsufficientStockException if stock is not available
     */
    Order createOrder(UUID customerId, List<OrderItem> items);

    /**
     * Creates a new order from customer ID and items with explicit delivery address.
     * @param customerId the customer ID
     * @param items list of order items
     * @param explicitDeliveryAddress optional delivery address (if null, uses customer's address)
     * @return created order
     * @throws com.farmatodo.reto_tecnico.domain.exception.CustomerNotFoundException if customer not found
     * @throws com.farmatodo.reto_tecnico.domain.exception.InsufficientStockException if stock is not available
     */
    Order createOrder(UUID customerId, List<OrderItem> items, String explicitDeliveryAddress);

    /**
     * Retrieves an order by ID.
     * @param orderId the order ID
     * @return the order
     * @throws com.farmatodo.reto_tecnico.domain.exception.OrderNotFoundException if order not found
     */
    Order getOrder(UUID orderId);

    /**
     * Cancels an existing order.
     * @param orderId the order ID to cancel
     * @return cancelled order
     * @throws com.farmatodo.reto_tecnico.domain.exception.OrderNotFoundException if order not found
     * @throws IllegalStateException if order cannot be cancelled
     */
    Order cancelOrder(UUID orderId);
}
