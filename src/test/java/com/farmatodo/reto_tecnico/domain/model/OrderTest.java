package com.farmatodo.reto_tecnico.domain.model;

import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for Order domain entity.
 * Tests state machine transitions and business logic.
 */
@DisplayName("Order Domain Model Tests")
class OrderTest {

    private Customer testCustomer;
    private Product product1;
    private Product product2;
    private OrderItem item1;
    private OrderItem item2;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Juan Pérez")
                .email(new Email("juan@test.com"))
                .phone(new Phone("3001234567"))
                .address("Calle 123, Bogotá")
                .build();

        product1 = Product.builder()
                .id(UUID.randomUUID())
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(new Money(new BigDecimal("10000.00")))
                .stock(100)
                .build();

        product2 = Product.builder()
                .id(UUID.randomUUID())
                .name("Ibuprofeno 400mg")
                .description("Antiinflamatorio")
                .price(new Money(new BigDecimal("15000.00")))
                .stock(50)
                .build();

        item1 = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(product1)
                .quantity(2)
                .unitPrice(product1.getPrice())
                .build();

        item2 = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(product2)
                .quantity(3)
                .unitPrice(product2.getPrice())
                .build();
    }

    @Test
    @DisplayName("Should calculate total correctly from items")
    void shouldCalculateTotalCorrectly() {
        // Given: Order with 2 items
        // Item 1: 2 units × $10,000 = $20,000
        // Item 2: 3 units × $15,000 = $45,000
        // Total: $65,000

        // Act
        Order order = Order.create(testCustomer, List.of(item1, item2));

        // Assert
        assertThat(order.getTotalAmount())
                .isEqualTo(new Money(new BigDecimal("65000.00")));
        assertThat(order.getTotalItemCount()).isEqualTo(5);
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        assertThat(order.getCreatedAt()).isNotNull();
        assertThat(order.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should fail payment from invalid state")
    void shouldFailPaymentFromInvalidState() {
        // Given: Order that has been completed
        Order order = Order.create(testCustomer, List.of(item1));
        order.assignPaymentToken("tok_test123");
        order.confirmPayment();
        order.complete();

        // Act & Assert: Cannot fail payment from COMPLETED state
        assertThatThrownBy(() -> order.failPayment())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot fail payment from state: COMPLETED")
                .hasMessageContaining("Payment can only fail from PENDING or PAYMENT_PROCESSING states");
    }

    @Test
    @DisplayName("Should cancel order successfully")
    void shouldCancelOrder() {
        // Given: Order in PENDING state
        Order order = Order.create(testCustomer, List.of(item1, item2));

        // Act
        order.cancel();

        // Assert
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.CANCELLED);
        assertThat(order.isCancellable()).isFalse();
        assertThat(order.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should not cancel completed order")
    void shouldNotCancelCompletedOrder() {
        // Given: Order that has been completed
        Order order = Order.create(testCustomer, List.of(item1));
        order.assignPaymentToken("tok_test123");
        order.confirmPayment();
        order.complete();

        // Act & Assert
        assertThatThrownBy(() -> order.cancel())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot cancel a completed order");
    }

    @Test
    @DisplayName("Should transition through payment states correctly")
    void shouldTransitionThroughPaymentStatesCorrectly() {
        // Given: New order
        Order order = Order.create(testCustomer, List.of(item1));
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PENDING);

        // When: Assign payment token
        order.assignPaymentToken("tok_test123");

        // Then: Status changes to PAYMENT_PROCESSING
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_PROCESSING);
        assertThat(order.getPaymentToken()).isEqualTo("tok_test123");

        // When: Confirm payment
        order.confirmPayment();

        // Then: Status changes to PAYMENT_CONFIRMED
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_CONFIRMED);

        // When: Complete order
        order.complete();

        // Then: Status changes to COMPLETED
        assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.COMPLETED);
    }

    @Test
    @DisplayName("Should fail payment correctly from valid states")
    void shouldFailPaymentCorrectlyFromValidStates() {
        // Test 1: Fail payment from PENDING
        Order order1 = Order.create(testCustomer, List.of(item1));
        assertThat(order1.getStatus()).isEqualTo(Order.OrderStatus.PENDING);

        order1.failPayment();
        assertThat(order1.getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_FAILED);

        // Test 2: Fail payment from PAYMENT_PROCESSING
        Order order2 = Order.create(testCustomer, List.of(item1));
        order2.assignPaymentToken("tok_test456");
        assertThat(order2.getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_PROCESSING);

        order2.failPayment();
        assertThat(order2.getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_FAILED);
    }

    @Test
    @DisplayName("Should not confirm payment from non-processing state")
    void shouldNotConfirmPaymentFromNonProcessingState() {
        // Given: Order in PENDING state
        Order order = Order.create(testCustomer, List.of(item1));

        // Act & Assert
        assertThatThrownBy(() -> order.confirmPayment())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot confirm payment for order not in processing state");
    }

    @Test
    @DisplayName("Should not complete order without payment confirmation")
    void shouldNotCompleteOrderWithoutPaymentConfirmation() {
        // Given: Order in PENDING state
        Order order = Order.create(testCustomer, List.of(item1));

        // Act & Assert
        assertThatThrownBy(() -> order.complete())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot complete order without payment confirmation");
    }

    @Test
    @DisplayName("Should recalculate total when items change")
    void shouldRecalculateTotalWhenItemsChange() {
        // Given: Order with 1 item
        Order order = Order.create(testCustomer, List.of(item1));
        Money initialTotal = order.getTotalAmount();

        // When: Add another item
        order.addItem(item2);

        // Then: Total is recalculated
        assertThat(order.getTotalAmount().isGreaterThan(initialTotal)).isTrue();
        assertThat(order.getTotalAmount()).isEqualTo(new Money(new BigDecimal("65000.00")));
        assertThat(order.getItems()).hasSize(2);
    }

    @Test
    @DisplayName("Should not add item to non-pending order")
    void shouldNotAddItemToNonPendingOrder() {
        // Given: Order with payment processing
        Order order = Order.create(testCustomer, List.of(item1));
        order.assignPaymentToken("tok_test123");

        // Act & Assert
        assertThatThrownBy(() -> order.addItem(item2))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot add items to a non-pending order");
    }

    @Test
    @DisplayName("Should remove item and recalculate total")
    void shouldRemoveItemAndRecalculateTotal() {
        // Given: Order with 2 items
        Order order = Order.create(testCustomer, List.of(item1, item2));
        Money initialTotal = order.getTotalAmount();

        // When: Remove one item
        order.removeItem(item2.getId());

        // Then: Total is recalculated
        assertThat(order.getTotalAmount().isLessThan(initialTotal)).isTrue();
        assertThat(order.getTotalAmount()).isEqualTo(new Money(new BigDecimal("20000.00")));
        assertThat(order.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Should not remove last item from order")
    void shouldNotRemoveLastItemFromOrder() {
        // Given: Order with 1 item
        Order order = Order.create(testCustomer, List.of(item1));

        // Act & Assert
        assertThatThrownBy(() -> order.removeItem(item1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Order must have at least one item");
    }

    @Test
    @DisplayName("Should check if order is cancellable")
    void shouldCheckIfOrderIsCancellable() {
        // Pending order is cancellable
        Order pendingOrder = Order.create(testCustomer, List.of(item1));
        assertThat(pendingOrder.isCancellable()).isTrue();

        // Completed order is not cancellable
        Order completedOrder = Order.create(testCustomer, List.of(item1));
        completedOrder.assignPaymentToken("tok_test123");
        completedOrder.confirmPayment();
        completedOrder.complete();
        assertThat(completedOrder.isCancellable()).isFalse();

        // Cancelled order is not cancellable
        Order cancelledOrder = Order.create(testCustomer, List.of(item1));
        cancelledOrder.cancel();
        assertThat(cancelledOrder.isCancellable()).isFalse();
    }

    @Test
    @DisplayName("Should validate customer is not null")
    void shouldValidateCustomerIsNotNull() {
        assertThatThrownBy(() -> Order.create(null, List.of(item1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Customer cannot be null");
    }

    @Test
    @DisplayName("Should validate items list is not empty")
    void shouldValidateItemsListIsNotEmpty() {
        assertThatThrownBy(() -> Order.create(testCustomer, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order must have at least one item");

        assertThatThrownBy(() -> Order.create(testCustomer, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Order must have at least one item");
    }
}
