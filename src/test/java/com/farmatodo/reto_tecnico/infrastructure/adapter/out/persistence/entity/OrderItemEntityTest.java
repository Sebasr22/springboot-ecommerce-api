package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * POJO tests for OrderItemEntity.
 * Tests builder, getters, setters, and relationships.
 */
@DisplayName("OrderItemEntity POJO Tests")
class OrderItemEntityTest {

    @Test
    @DisplayName("Should create entity using builder")
    void shouldCreateEntityUsingBuilder() {
        // Given
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        // When
        OrderItemEntity entity = OrderItemEntity.builder()
                .id(id)
                .productId(productId)
                .quantity(5)
                .unitPrice(new BigDecimal("10000.00"))
                .build();

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getProductId()).isEqualTo(productId);
        assertThat(entity.getQuantity()).isEqualTo(5);
        assertThat(entity.getUnitPrice()).isEqualByComparingTo(new BigDecimal("10000.00"));
    }

    @Test
    @DisplayName("Should create entity using no-args constructor")
    void shouldCreateEntityUsingNoArgsConstructor() {
        // When
        OrderItemEntity entity = new OrderItemEntity();

        // Then
        assertThat(entity.getId()).isNull();
        assertThat(entity.getProductId()).isNull();
        assertThat(entity.getQuantity()).isNull();
    }

    @Test
    @DisplayName("Should create entity using all-args constructor")
    void shouldCreateEntityUsingAllArgsConstructor() {
        // Given
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        BigDecimal unitPrice = new BigDecimal("15000.00");
        OrderEntity order = new OrderEntity();

        // When
        OrderItemEntity entity = new OrderItemEntity(id, productId, 3, unitPrice, order);

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getProductId()).isEqualTo(productId);
        assertThat(entity.getQuantity()).isEqualTo(3);
        assertThat(entity.getUnitPrice()).isEqualByComparingTo(unitPrice);
        assertThat(entity.getOrder()).isEqualTo(order);
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        // Given
        OrderItemEntity entity = new OrderItemEntity();
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        OrderEntity order = new OrderEntity();

        // When
        entity.setId(id);
        entity.setProductId(productId);
        entity.setQuantity(10);
        entity.setUnitPrice(new BigDecimal("25000.00"));
        entity.setOrder(order);

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getProductId()).isEqualTo(productId);
        assertThat(entity.getQuantity()).isEqualTo(10);
        assertThat(entity.getUnitPrice()).isEqualByComparingTo(new BigDecimal("25000.00"));
        assertThat(entity.getOrder()).isEqualTo(order);
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderItemEntity entity1 = OrderItemEntity.builder()
                .id(id)
                .productId(productId)
                .quantity(2)
                .unitPrice(new BigDecimal("5000.00"))
                .build();

        OrderItemEntity entity2 = OrderItemEntity.builder()
                .id(id)
                .productId(productId)
                .quantity(2)
                .unitPrice(new BigDecimal("5000.00"))
                .build();

        // Then
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void shouldImplementToString() {
        // Given
        UUID productId = UUID.randomUUID();
        OrderItemEntity entity = OrderItemEntity.builder()
                .productId(productId)
                .quantity(5)
                .unitPrice(new BigDecimal("10000.00"))
                .build();

        // When
        String result = entity.toString();

        // Then
        assertThat(result).contains("10000.00");
        assertThat(result).contains("5");
    }

    @Test
    @DisplayName("Should handle relationship with OrderEntity")
    void shouldHandleRelationshipWithOrderEntity() {
        // Given
        OrderEntity order = OrderEntity.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .totalAmount(new BigDecimal("100000.00"))
                .status(OrderEntity.OrderStatus.PENDING)
                .build();

        OrderItemEntity item = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .quantity(2)
                .unitPrice(new BigDecimal("50000.00"))
                .build();

        // When
        item.setOrder(order);

        // Then
        assertThat(item.getOrder()).isEqualTo(order);
    }
}
