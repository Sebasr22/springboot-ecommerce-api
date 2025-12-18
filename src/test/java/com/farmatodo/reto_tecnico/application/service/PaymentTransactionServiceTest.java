package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.domain.port.out.OrderRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentTransactionService.
 * Tests each transactional method independently.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PaymentTransactionService Unit Tests")
class PaymentTransactionServiceTest {

    @Mock
    private OrderRepositoryPort orderRepository;

    @InjectMocks
    private PaymentTransactionService paymentTransactionService;

    private Order testOrder;
    private Customer testCustomer;

    @BeforeEach
    void setUp() {
        testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Test Customer")
                .email(new Email("test@example.com"))
                .phone(new Phone("3001234567"))
                .address("Test Address")
                .build();

        Product testProduct = Product.builder()
                .id(UUID.randomUUID())
                .name("Test Product")
                .description("Test Description")
                .price(Money.of(new BigDecimal("10000.00")))
                .stock(100)
                .build();

        OrderItem item = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(testProduct)
                .quantity(2)
                .unitPrice(testProduct.getPrice())
                .build();

        testOrder = Order.create(testCustomer, List.of(item));
    }

    @Test
    @DisplayName("Should save order state successfully")
    void shouldSaveOrderStateSuccessfully() {
        // Given
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = paymentTransactionService.saveOrderState(testOrder);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testOrder.getId());
        verify(orderRepository).save(testOrder);
    }

    @Test
    @DisplayName("Should assign token and save order")
    void shouldAssignTokenAndSaveOrder() {
        // Given
        String token = "tok_test_token_12345";
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = paymentTransactionService.assignTokenAndSave(testOrder, token);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentToken()).isEqualTo(token);
        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_PROCESSING);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getPaymentToken()).isEqualTo(token);
    }

    @Test
    @DisplayName("Should confirm payment and save order")
    void shouldConfirmPaymentAndSaveOrder() {
        // Given
        testOrder.assignPaymentToken("tok_test_token");
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = paymentTransactionService.confirmPaymentAndSave(testOrder);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_CONFIRMED);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_CONFIRMED);
    }

    @Test
    @DisplayName("Should fail payment and save order")
    void shouldFailPaymentAndSaveOrder() {
        // Given
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = paymentTransactionService.failPaymentAndSave(testOrder);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_FAILED);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        assertThat(orderCaptor.getValue().getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_FAILED);
    }

    @Test
    @DisplayName("Should call repository save exactly once per operation")
    void shouldCallRepositorySaveExactlyOncePerOperation() {
        // Given
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        paymentTransactionService.saveOrderState(testOrder);

        // Then
        verify(orderRepository, times(1)).save(testOrder);
    }

    @Test
    @DisplayName("Should preserve order data when saving state")
    void shouldPreserveOrderDataWhenSavingState() {
        // Given
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = paymentTransactionService.saveOrderState(testOrder);

        // Then
        assertThat(result.getCustomer()).isEqualTo(testCustomer);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotalAmount()).isEqualTo(testOrder.getTotalAmount());
    }

    @Test
    @DisplayName("Should update order status to PAYMENT_PROCESSING when assigning token")
    void shouldUpdateOrderStatusToPaymentProcessingWhenAssigningToken() {
        // Given
        String token = "tok_processing_token";
        assertThat(testOrder.getStatus()).isEqualTo(Order.OrderStatus.PENDING);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Order result = paymentTransactionService.assignTokenAndSave(testOrder, token);

        // Then
        assertThat(result.getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_PROCESSING);
    }
}
