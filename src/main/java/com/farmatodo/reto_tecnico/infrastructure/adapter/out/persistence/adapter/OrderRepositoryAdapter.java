package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.adapter;

import com.farmatodo.reto_tecnico.domain.exception.CustomerNotFoundException;
import com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.port.out.OrderRepositoryPort;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.OrderEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.OrderItemEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.CustomerMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.OrderItemMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.OrderMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.ProductMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.CustomerJpaRepository;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.OrderJpaRepository;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementation for Order persistence.
 * Implements hexagonal architecture output port using JPA repository.
 * Translates between domain Order and JPA OrderEntity.
 *
 * Handles complex aggregate mapping:
 * - Order -> OrderEntity (with customer FK)
 * - OrderItems -> OrderItemEntities (with product FK)
 * - Maintains bidirectional relationships
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class OrderRepositoryAdapter implements OrderRepositoryPort {

    private final OrderJpaRepository orderJpaRepository;
    private final CustomerJpaRepository customerJpaRepository;
    private final ProductJpaRepository productJpaRepository;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CustomerMapper customerMapper;
    private final ProductMapper productMapper;

    /**
     * Saves an order with all its items.
     * Handles the complex aggregate root persistence.
     *
     * @param order the order to save
     * @return saved order with generated IDs
     */
    @Override
    @Transactional
    public Order save(Order order) {
        log.debug("Saving order: {}", order.getId());

        // Convert order to entity (without items first)
        OrderEntity orderEntity = orderMapper.toEntity(order);

        // Convert and add order items
        List<OrderItemEntity> itemEntities = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            OrderItemEntity itemEntity = orderItemMapper.toEntity(item);
            itemEntity.setOrder(orderEntity); // Set bidirectional relationship
            itemEntities.add(itemEntity);
        }
        orderEntity.setItems(itemEntities);

        // Save order (cascade saves items)
        OrderEntity savedEntity = orderJpaRepository.save(orderEntity);

        // Convert back to domain
        return entityToDomain(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(UUID id) {
        log.debug("Finding order by ID: {}", id);
        return orderJpaRepository.findById(id)
                .map(this::entityToDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByCustomerId(UUID customerId) {
        log.debug("Finding orders for customer: {}", customerId);
        return orderJpaRepository.findByCustomerId(customerId).stream()
                .map(this::entityToDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByStatus(Order.OrderStatus status) {
        log.debug("Finding orders by status: {}", status);
        OrderEntity.OrderStatus entityStatus = OrderEntity.OrderStatus.valueOf(status.name());
        return orderJpaRepository.findByStatus(entityStatus).stream()
                .map(this::entityToDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        log.debug("Finding all orders");
        return orderJpaRepository.findAll().stream()
                .map(this::entityToDomain)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean deleteById(UUID id) {
        log.debug("Deleting order: {}", id);
        if (orderJpaRepository.existsById(id)) {
            orderJpaRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean existsById(UUID id) {
        return orderJpaRepository.existsById(id);
    }

    /**
     * Converts OrderEntity to domain Order.
     * Handles loading of related entities (Customer, Products).
     *
     * @param entity the order entity
     * @return domain order
     */
    private Order entityToDomain(OrderEntity entity) {
        // Load customer
        CustomerEntity customerEntity = customerJpaRepository.findById(entity.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(entity.getCustomerId()));
        Customer customer = customerMapper.toDomain(customerEntity);

        // Convert order (without items)
        Order order = orderMapper.toDomain(entity, customer);

        // Convert order items with their products
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemEntity itemEntity : entity.getItems()) {
            // Load product for this item
            ProductEntity productEntity = productJpaRepository.findById(itemEntity.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(itemEntity.getProductId()));
            Product product = productMapper.toDomain(productEntity);

            // Convert item
            OrderItem orderItem = orderItemMapper.toDomain(itemEntity, product);
            orderItems.add(orderItem);
        }

        // Set items on order
        order.setItems(orderItems);

        return order;
    }
}
