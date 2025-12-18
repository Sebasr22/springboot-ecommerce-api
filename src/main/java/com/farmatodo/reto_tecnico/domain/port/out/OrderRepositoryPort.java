package com.farmatodo.reto_tecnico.domain.port.out;

import com.farmatodo.reto_tecnico.domain.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for order persistence.
 * Defines the contract for order data access operations.
 * Implementation will be provided by the infrastructure layer.
 */
public interface OrderRepositoryPort {

    /**
     * Saves an order (create or update).
     * @param order the order to save
     * @return saved order
     */
    Order save(Order order);

    /**
     * Finds an order by ID.
     * @param id the order ID
     * @return Optional containing the order if found
     */
    Optional<Order> findById(UUID id);

    /**
     * Finds all orders for a specific customer.
     * @param customerId the customer ID
     * @return list of customer orders
     */
    List<Order> findByCustomerId(UUID customerId);

    /**
     * Finds orders by status.
     * @param status the order status
     * @return list of orders with the given status
     */
    List<Order> findByStatus(Order.OrderStatus status);

    /**
     * Retrieves all orders.
     * @return list of all orders
     */
    List<Order> findAll();

    /**
     * Deletes an order by ID.
     * @param id the order ID
     * @return true if order was deleted
     */
    boolean deleteById(UUID id);

    /**
     * Checks if an order exists.
     * @param id the order ID
     * @return true if order exists
     */
    boolean existsById(UUID id);
}
