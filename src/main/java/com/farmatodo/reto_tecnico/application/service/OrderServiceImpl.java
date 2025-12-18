package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.exception.CustomerNotFoundException;
import com.farmatodo.reto_tecnico.domain.exception.InsufficientStockException;
import com.farmatodo.reto_tecnico.domain.exception.OrderNotFoundException;
import com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.port.in.CreateOrderUseCase;
import com.farmatodo.reto_tecnico.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.reto_tecnico.domain.port.out.OrderRepositoryPort;
import com.farmatodo.reto_tecnico.domain.port.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service implementation for order creation and management.
 * Coordinates order creation with stock validation and persistence.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements CreateOrderUseCase {

    private final OrderRepositoryPort orderRepository;
    private final CustomerRepositoryPort customerRepository;
    private final ProductRepositoryPort productRepository;

    @Override
    @Transactional
    public Order createOrder(Customer customer, List<OrderItem> items) {
        log.info("Creating order for customer: {} with {} items", customer.getEmail(), items.size());

        // Validate customer exists in repository
        if (!customerRepository.findById(customer.getId()).isPresent()) {
            log.warn("Customer not found in repository: {}", customer.getId());
            // Save customer if not exists
            customer = customerRepository.save(customer);
            log.info("Customer created: {}", customer.getId());
        }

        // Validate stock availability for all items
        validateStockAvailability(items);

        // Create order
        Order order = Order.create(customer, items);

        // Reduce stock for all items
        reduceStockForItems(items);

        // Save order
        Order savedOrder = orderRepository.save(order);

        log.info("Order created successfully: {} for customer: {}",
                savedOrder.getId(), customer.getEmail());

        return savedOrder;
    }

    @Override
    @Transactional
    public Order createOrder(UUID customerId, List<OrderItem> items) {
        log.info("Creating order for customer ID: {} with {} items", customerId, items.size());

        // Find customer
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        // Delegate to main createOrder method
        return createOrder(customer, items);
    }

    @Override
    public Order getOrder(UUID orderId) {
        log.debug("Retrieving order: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
    }

    @Override
    @Transactional
    public Order cancelOrder(UUID orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Validate order can be cancelled
        if (!order.isCancellable()) {
            throw new IllegalStateException(
                    String.format("Order %s cannot be cancelled (status: %s)",
                            orderId, order.getStatus())
            );
        }

        // Cancel order
        order.cancel();

        // Restore stock for all items
        restoreStockForItems(order.getItems());

        // Save updated order
        Order cancelledOrder = orderRepository.save(order);

        log.info("Order cancelled successfully: {}", orderId);

        return cancelledOrder;
    }

    /**
     * Validates stock availability for all order items.
     * @param items list of order items
     * @throws InsufficientStockException if any item has insufficient stock
     */
    private void validateStockAvailability(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            if (!product.hasSufficientStock(item.getQuantity())) {
                log.warn("Insufficient stock for product: {} (available: {}, requested: {})",
                        product.getName(), product.getStock(), item.getQuantity());
                throw new InsufficientStockException(
                        product.getId(),
                        product.getName(),
                        product.getStock(),
                        item.getQuantity()
                );
            }
        }
    }

    /**
     * Reduces stock for all order items.
     * @param items list of order items
     */
    private void reduceStockForItems(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            int newStock = product.getStock() - item.getQuantity();
            productRepository.updateStock(product.getId(), newStock);

            log.debug("Reduced stock for product {} from {} to {}",
                    product.getName(), product.getStock(), newStock);
        }
    }

    /**
     * Restores stock for all order items (used when cancelling an order).
     * @param items list of order items
     */
    private void restoreStockForItems(List<OrderItem> items) {
        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));

            int newStock = product.getStock() + item.getQuantity();
            productRepository.updateStock(product.getId(), newStock);

            log.debug("Restored stock for product {} from {} to {}",
                    product.getName(), product.getStock(), newStock);
        }
    }
}
