package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * POJO tests for OrderEntity.
 * Tests builder, getters, setters, relationships, and lifecycle callbacks.
 */
@DisplayName("OrderEntity POJO Tests")
class OrderEntityTest {

    @Test
    @DisplayName("Should create entity using builder")
    void shouldCreateEntityUsingBuilder() {
        // Given
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        OrderEntity entity = OrderEntity.builder()
                .id(id)
                .customerId(customerId)
                .totalAmount(new BigDecimal("150000.00"))
                .status(OrderEntity.OrderStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .paymentToken("encrypted_token")
                .build();

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getCustomerId()).isEqualTo(customerId);
        assertThat(entity.getTotalAmount()).isEqualByComparingTo(new BigDecimal("150000.00"));
        assertThat(entity.getStatus()).isEqualTo(OrderEntity.OrderStatus.PENDING);
        assertThat(entity.getPaymentToken()).isEqualTo("encrypted_token");
    }

    @Test
    @DisplayName("Should create entity using no-args constructor")
    void shouldCreateEntityUsingNoArgsConstructor() {
        // When
        OrderEntity entity = new OrderEntity();

        // Then
        assertThat(entity.getId()).isNull();
        assertThat(entity.getItems()).isNotNull();
    }

    @Test
    @DisplayName("Should add item and maintain bidirectional relationship")
    void shouldAddItemAndMaintainBidirectionalRelationship() {
        // Given
        OrderEntity order = OrderEntity.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .totalAmount(new BigDecimal("10000.00"))
                .status(OrderEntity.OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        OrderItemEntity item = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .quantity(2)
                .unitPrice(new BigDecimal("5000.00"))
                .build();

        // When
        order.addItem(item);

        // Then
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0)).isEqualTo(item);
        assertThat(item.getOrder()).isEqualTo(order);
    }

    @Test
    @DisplayName("Should remove item and maintain bidirectional relationship")
    void shouldRemoveItemAndMaintainBidirectionalRelationship() {
        // Given
        OrderEntity order = OrderEntity.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .totalAmount(new BigDecimal("10000.00"))
                .status(OrderEntity.OrderStatus.PENDING)
                .items(new ArrayList<>())
                .build();

        OrderItemEntity item = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(UUID.randomUUID())
                .quantity(2)
                .unitPrice(new BigDecimal("5000.00"))
                .build();

        order.addItem(item);

        // When
        order.removeItem(item);

        // Then
        assertThat(order.getItems()).isEmpty();
        assertThat(item.getOrder()).isNull();
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        // Given
        OrderEntity entity = new OrderEntity();
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        // When
        entity.setId(id);
        entity.setCustomerId(customerId);
        entity.setTotalAmount(new BigDecimal("200000.00"));
        entity.setStatus(OrderEntity.OrderStatus.PAYMENT_CONFIRMED);
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setPaymentToken("new_token");

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getCustomerId()).isEqualTo(customerId);
        assertThat(entity.getTotalAmount()).isEqualByComparingTo(new BigDecimal("200000.00"));
        assertThat(entity.getStatus()).isEqualTo(OrderEntity.OrderStatus.PAYMENT_CONFIRMED);
        assertThat(entity.getPaymentToken()).isEqualTo("new_token");
    }

    @Test
    @DisplayName("Should handle all order status values")
    void shouldHandleAllOrderStatusValues() {
        // Given
        OrderEntity entity = new OrderEntity();

        // When & Then
        for (OrderEntity.OrderStatus status : OrderEntity.OrderStatus.values()) {
            entity.setStatus(status);
            assertThat(entity.getStatus()).isEqualTo(status);
        }
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        OrderEntity entity1 = OrderEntity.builder()
                .id(id)
                .customerId(customerId)
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderEntity.OrderStatus.PENDING)
                .build();

        OrderEntity entity2 = OrderEntity.builder()
                .id(id)
                .customerId(customerId)
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderEntity.OrderStatus.PENDING)
                .build();

        // Then
        assertThat(entity1).isEqualTo(entity2);
    }

    @Test
    @DisplayName("Should implement toString")
    void shouldImplementToString() {
        // Given
        OrderEntity entity = OrderEntity.builder()
                .id(UUID.randomUUID())
                .totalAmount(new BigDecimal("150000.00"))
                .status(OrderEntity.OrderStatus.COMPLETED)
                .build();

        // When
        String result = entity.toString();

        // Then
        assertThat(result).contains("150000.00");
        assertThat(result).contains("COMPLETED");
    }

    @Test
    @DisplayName("Should trigger onCreate lifecycle callback")
    void shouldTriggerOnCreateLifecycleCallback() {
        // Given
        OrderEntity entity = OrderEntity.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderEntity.OrderStatus.PENDING)
                .build();

        // When
        entity.onCreate();

        // Then
        assertThat(entity.getCreatedAt()).isNotNull();
        assertThat(entity.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should trigger onUpdate lifecycle callback")
    void shouldTriggerOnUpdateLifecycleCallback() {
        // Given
        OrderEntity entity = OrderEntity.builder()
                .id(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .totalAmount(new BigDecimal("100.00"))
                .status(OrderEntity.OrderStatus.PENDING)
                .build();
        entity.onCreate();
        LocalDateTime originalUpdatedAt = entity.getUpdatedAt();

        // Wait a bit to ensure different timestamp
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        // When
        entity.onUpdate();

        // Then
        assertThat(entity.getUpdatedAt()).isAfterOrEqualTo(originalUpdatedAt);
    }
}
