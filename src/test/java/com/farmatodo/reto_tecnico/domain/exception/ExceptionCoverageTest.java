package com.farmatodo.reto_tecnico.domain.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive unit tests for all domain exceptions.
 * Tests all constructors and getters to ensure full coverage.
 */
@DisplayName("Domain Exception Coverage Tests")
class ExceptionCoverageTest {

    @Nested
    @DisplayName("DomainException Tests")
    class DomainExceptionTests {

        @Test
        @DisplayName("Should create with message")
        void shouldCreateWithMessage() {
            // When
            DomainException exception = new DomainException("Test error message");

            // Then
            assertThat(exception.getMessage()).isEqualTo("Test error message");
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            // Given
            Throwable cause = new RuntimeException("Root cause");

            // When
            DomainException exception = new DomainException("Test error", cause);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Test error");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("InsufficientStockException Tests")
    class InsufficientStockExceptionTests {

        @Test
        @DisplayName("Should create with product details")
        void shouldCreateWithProductDetails() {
            // Given
            UUID productId = UUID.randomUUID();
            String productName = "Acetaminofén 500mg";
            int availableStock = 5;
            int requestedQuantity = 10;

            // When
            InsufficientStockException exception = new InsufficientStockException(
                    productId, productName, availableStock, requestedQuantity
            );

            // Then
            assertThat(exception.getProductId()).isEqualTo(productId);
            assertThat(exception.getProductName()).isEqualTo(productName);
            assertThat(exception.getAvailableStock()).isEqualTo(availableStock);
            assertThat(exception.getRequestedQuantity()).isEqualTo(requestedQuantity);
            assertThat(exception.getMessage())
                    .contains(productName)
                    .contains(productId.toString())
                    .contains("5")
                    .contains("10");
        }

        @Test
        @DisplayName("Should create with simple message")
        void shouldCreateWithSimpleMessage() {
            // When
            InsufficientStockException exception = new InsufficientStockException("Not enough stock");

            // Then
            assertThat(exception.getMessage()).isEqualTo("Not enough stock");
            assertThat(exception.getProductId()).isNull();
            assertThat(exception.getProductName()).isNull();
            assertThat(exception.getAvailableStock()).isZero();
            assertThat(exception.getRequestedQuantity()).isZero();
        }
    }

    @Nested
    @DisplayName("PaymentFailedException Tests")
    class PaymentFailedExceptionTests {

        @Test
        @DisplayName("Should create with order details")
        void shouldCreateWithOrderDetails() {
            // Given
            UUID orderId = UUID.randomUUID();
            String reason = "Card declined";
            int attemptNumber = 3;

            // When
            PaymentFailedException exception = new PaymentFailedException(orderId, reason, attemptNumber);

            // Then
            assertThat(exception.getOrderId()).isEqualTo(orderId);
            assertThat(exception.getReason()).isEqualTo(reason);
            assertThat(exception.getAttemptNumber()).isEqualTo(attemptNumber);
            assertThat(exception.getMessage())
                    .contains(orderId.toString())
                    .contains("attempt 3")
                    .contains("Card declined");
        }

        @Test
        @DisplayName("Should create with simple message")
        void shouldCreateWithSimpleMessage() {
            // When
            PaymentFailedException exception = new PaymentFailedException("Payment processing error");

            // Then
            assertThat(exception.getMessage()).isEqualTo("Payment processing error");
            assertThat(exception.getOrderId()).isNull();
            assertThat(exception.getReason()).isEqualTo("Payment processing error");
            assertThat(exception.getAttemptNumber()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            // Given
            Throwable cause = new RuntimeException("Network error");

            // When
            PaymentFailedException exception = new PaymentFailedException("Gateway timeout", cause);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Gateway timeout");
            assertThat(exception.getCause()).isEqualTo(cause);
            assertThat(exception.getOrderId()).isNull();
            assertThat(exception.getReason()).isEqualTo("Gateway timeout");
            assertThat(exception.getAttemptNumber()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("TokenizationFailedException Tests")
    class TokenizationFailedExceptionTests {

        @Test
        @DisplayName("Should create with message")
        void shouldCreateWithMessage() {
            // When
            TokenizationFailedException exception = new TokenizationFailedException("Invalid card number");

            // Then
            assertThat(exception.getMessage()).isEqualTo("Invalid card number");
        }

        @Test
        @DisplayName("Should create with message and cause")
        void shouldCreateWithMessageAndCause() {
            // Given
            Throwable cause = new RuntimeException("Encryption failed");

            // When
            TokenizationFailedException exception = new TokenizationFailedException("Tokenization error", cause);

            // Then
            assertThat(exception.getMessage()).isEqualTo("Tokenization error");
            assertThat(exception.getCause()).isEqualTo(cause);
        }
    }

    @Nested
    @DisplayName("CustomerNotFoundException Tests")
    class CustomerNotFoundExceptionTests {

        @Test
        @DisplayName("Should create with customer ID")
        void shouldCreateWithCustomerId() {
            // Given
            UUID customerId = UUID.randomUUID();

            // When
            CustomerNotFoundException exception = new CustomerNotFoundException(customerId);

            // Then
            assertThat(exception.getCustomerId()).isEqualTo(customerId);
            assertThat(exception.getMessage()).contains(customerId.toString());
            assertThat(exception.getMessage()).contains("Customer not found");
        }

        @Test
        @DisplayName("Should create with custom message")
        void shouldCreateWithCustomMessage() {
            // When
            CustomerNotFoundException exception = new CustomerNotFoundException("Customer does not exist");

            // Then
            assertThat(exception.getMessage()).isEqualTo("Customer does not exist");
            assertThat(exception.getCustomerId()).isNull();
        }
    }

    @Nested
    @DisplayName("OrderNotFoundException Tests")
    class OrderNotFoundExceptionTests {

        @Test
        @DisplayName("Should create with order ID")
        void shouldCreateWithOrderId() {
            // Given
            UUID orderId = UUID.randomUUID();

            // When
            OrderNotFoundException exception = new OrderNotFoundException(orderId);

            // Then
            assertThat(exception.getOrderId()).isEqualTo(orderId);
            assertThat(exception.getMessage()).contains(orderId.toString());
            assertThat(exception.getMessage()).contains("Order not found");
        }

        @Test
        @DisplayName("Should create with custom message")
        void shouldCreateWithCustomMessage() {
            // When
            OrderNotFoundException exception = new OrderNotFoundException("Order does not exist");

            // Then
            assertThat(exception.getMessage()).isEqualTo("Order does not exist");
            assertThat(exception.getOrderId()).isNull();
        }
    }

    @Nested
    @DisplayName("ProductNotFoundException Tests")
    class ProductNotFoundExceptionTests {

        @Test
        @DisplayName("Should create with product ID")
        void shouldCreateWithProductId() {
            // Given
            UUID productId = UUID.randomUUID();

            // When
            ProductNotFoundException exception = new ProductNotFoundException(productId);

            // Then
            assertThat(exception.getProductId()).isEqualTo(productId);
            assertThat(exception.getMessage()).contains(productId.toString());
            assertThat(exception.getMessage()).contains("Product not found");
        }

        @Test
        @DisplayName("Should create with custom message")
        void shouldCreateWithCustomMessage() {
            // When
            ProductNotFoundException exception = new ProductNotFoundException("Product does not exist");

            // Then
            assertThat(exception.getMessage()).isEqualTo("Product does not exist");
            assertThat(exception.getProductId()).isNull();
        }
    }

    @Nested
    @DisplayName("CustomerAlreadyExistsException Tests")
    class CustomerAlreadyExistsExceptionTests {

        @Test
        @DisplayName("Should create with email")
        void shouldCreateWithEmail() {
            // Given
            String email = "existing@example.com";

            // When
            CustomerAlreadyExistsException exception = new CustomerAlreadyExistsException(email);

            // Then
            assertThat(exception.getMessage()).contains(email);
            assertThat(exception.getMessage()).contains("already exists");
        }
    }

    @Nested
    @DisplayName("CartNotFoundException Tests")
    class CartNotFoundExceptionTests {

        @Test
        @DisplayName("Should create with customer ID")
        void shouldCreateWithCustomerId() {
            // Given
            UUID customerId = UUID.randomUUID();

            // When
            CartNotFoundException exception = new CartNotFoundException(customerId);

            // Then
            assertThat(exception.getMessage()).contains(customerId.toString());
            assertThat(exception.getMessage()).contains("Cart not found");
        }

        @Test
        @DisplayName("Should create with custom message")
        void shouldCreateWithCustomMessage() {
            // When
            CartNotFoundException exception = new CartNotFoundException("Cart does not exist");

            // Then
            assertThat(exception.getMessage()).isEqualTo("Cart does not exist");
        }
    }

    @Nested
    @DisplayName("EmptyCartException Tests")
    class EmptyCartExceptionTests {

        @Test
        @DisplayName("Should create with default message")
        void shouldCreateWithDefaultMessage() {
            // When
            EmptyCartException exception = new EmptyCartException();

            // Then
            assertThat(exception.getMessage()).isEqualTo("Cannot checkout an empty cart");
        }

        @Test
        @DisplayName("Should create with custom message")
        void shouldCreateWithCustomMessage() {
            // When
            EmptyCartException exception = new EmptyCartException("Cart is empty");

            // Then
            assertThat(exception.getMessage()).isEqualTo("Cart is empty");
        }
    }

    @Test
    @DisplayName("All exceptions should extend DomainException")
    void allExceptionsShouldExtendDomainException() {
        // Given & When
        InsufficientStockException insufficientStock = new InsufficientStockException("test");
        PaymentFailedException paymentFailed = new PaymentFailedException("test");
        TokenizationFailedException tokenizationFailed = new TokenizationFailedException("test");
        CustomerNotFoundException customerNotFound = new CustomerNotFoundException("test");
        OrderNotFoundException orderNotFound = new OrderNotFoundException("test");
        ProductNotFoundException productNotFound = new ProductNotFoundException("test");
        CustomerAlreadyExistsException customerExists = new CustomerAlreadyExistsException("test@test.com");
        CartNotFoundException cartNotFound = new CartNotFoundException("test");
        EmptyCartException emptyCart = new EmptyCartException();

        // Then
        assertThat(insufficientStock).isInstanceOf(DomainException.class);
        assertThat(paymentFailed).isInstanceOf(DomainException.class);
        assertThat(tokenizationFailed).isInstanceOf(DomainException.class);
        assertThat(customerNotFound).isInstanceOf(DomainException.class);
        assertThat(orderNotFound).isInstanceOf(DomainException.class);
        assertThat(productNotFound).isInstanceOf(DomainException.class);
        assertThat(customerExists).isInstanceOf(DomainException.class);
        assertThat(cartNotFound).isInstanceOf(DomainException.class);
        assertThat(emptyCart).isInstanceOf(DomainException.class);
    }

    @Test
    @DisplayName("All exceptions should be RuntimeExceptions")
    void allExceptionsShouldBeRuntimeExceptions() {
        // Given & When
        DomainException domain = new DomainException("test");

        // Then
        assertThat(domain).isInstanceOf(RuntimeException.class);
    }
}
