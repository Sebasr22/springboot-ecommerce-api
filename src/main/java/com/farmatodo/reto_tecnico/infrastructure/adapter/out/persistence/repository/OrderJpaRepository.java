package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA repository for OrderEntity.
 * Provides CRUD operations and custom queries for order persistence.
 */
@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {

    /**
     * Finds all orders for a specific customer.
     * @param customerId the customer ID
     * @return list of customer orders
     */
    List<OrderEntity> findByCustomerId(UUID customerId);

    /**
     * Finds orders by status.
     * @param status the order status
     * @return list of orders with given status
     */
    List<OrderEntity> findByStatus(OrderEntity.OrderStatus status);

    /**
     * Finds orders created within a date range.
     * @param startDate start of date range
     * @param endDate end of date range
     * @return list of orders in date range
     */
    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<OrderEntity> findByCreatedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Finds orders for a customer with specific status.
     * @param customerId the customer ID
     * @param status the order status
     * @return list of matching orders
     */
    List<OrderEntity> findByCustomerIdAndStatus(UUID customerId, OrderEntity.OrderStatus status);
}
