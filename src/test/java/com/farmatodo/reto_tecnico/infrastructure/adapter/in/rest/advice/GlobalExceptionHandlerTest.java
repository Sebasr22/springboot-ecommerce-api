package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.advice;

import com.farmatodo.reto_tecnico.domain.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GlobalExceptionHandler.
 * Tests all exception handlers without Spring context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Unit Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    // ===========================================
    // 400 BAD REQUEST Tests
    // ===========================================

    @Nested
    @DisplayName("400 Bad Request - Validation Errors")
    class BadRequestTests {

        @Test
        @DisplayName("Should handle MethodArgumentNotValidException with field errors")
        void shouldHandleMethodArgumentNotValidException() {
            // Given
            BindingResult bindingResult = mock(BindingResult.class);
            FieldError fieldError1 = new FieldError("object", "email", "must be a valid email");
            FieldError fieldError2 = new FieldError("object", "name", "must not be blank");
            when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError1, fieldError2));

            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleValidationErrors(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(400);
            assertThat(response.getBody().getError()).isEqualTo("VALIDATION_ERROR");
            assertThat(response.getBody().getMessage()).contains("Validation failed");
            assertThat(response.getBody().getPath()).isEqualTo("/api/v1/test");
            assertThat(response.getBody().getValidationErrors()).containsKey("email");
            assertThat(response.getBody().getValidationErrors()).containsKey("name");
        }

        @Test
        @DisplayName("Should handle ConstraintViolationException")
        void shouldHandleConstraintViolationException() {
            // Given
            ConstraintViolation<?> violation = mock(ConstraintViolation.class);
            Path path = mock(Path.class);
            when(path.toString()).thenReturn("findById.id");
            when(violation.getPropertyPath()).thenReturn(path);
            when(violation.getMessage()).thenReturn("must not be null");

            ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

            // When
            ResponseEntity<ErrorResponse> response = handler.handleConstraintViolation(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("CONSTRAINT_VIOLATION");
            assertThat(response.getBody().getValidationErrors()).containsKey("id");
        }

        @Test
        @DisplayName("Should handle HttpMessageNotReadableException with null cause")
        void shouldHandleHttpMessageNotReadableExceptionNullCause() {
            // Given
            HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Could not read", (Throwable) null, null);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("MALFORMED_JSON");
            assertThat(response.getBody().getMessage()).isEqualTo("Malformed JSON request");
        }

        @Test
        @DisplayName("Should handle HttpMessageNotReadableException with deserialize error")
        void shouldHandleHttpMessageNotReadableExceptionDeserialize() {
            // Given
            RuntimeException cause = new RuntimeException("Cannot deserialize value");
            HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Could not read", cause, null);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("Invalid value in JSON request");
        }

        @Test
        @DisplayName("Should handle HttpMessageNotReadableException with syntax error")
        void shouldHandleHttpMessageNotReadableExceptionSyntax() {
            // Given
            RuntimeException cause = new RuntimeException("Unexpected character at position 5");
            HttpMessageNotReadableException ex = new HttpMessageNotReadableException("Could not read", cause, null);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleHttpMessageNotReadable(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("Invalid JSON syntax");
        }

        @Test
        @DisplayName("Should handle MissingServletRequestParameterException")
        void shouldHandleMissingServletRequestParameterException() {
            // Given
            MissingServletRequestParameterException ex = new MissingServletRequestParameterException("page", "int");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleMissingParameter(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("MISSING_PARAMETER");
            assertThat(response.getBody().getMessage()).contains("page");
            assertThat(response.getBody().getMessage()).contains("int");
        }

        @Test
        @DisplayName("Should handle MethodArgumentTypeMismatchException with required type")
        void shouldHandleMethodArgumentTypeMismatchException() {
            // Given
            MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
            when(ex.getName()).thenReturn("id");
            when(ex.getValue()).thenReturn("not-a-uuid");
            when(ex.getRequiredType()).thenReturn((Class) UUID.class);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("TYPE_MISMATCH");
            assertThat(response.getBody().getMessage()).contains("id");
            assertThat(response.getBody().getMessage()).contains("UUID");
            assertThat(response.getBody().getMessage()).contains("not-a-uuid");
        }

        @Test
        @DisplayName("Should handle MethodArgumentTypeMismatchException with null required type")
        void shouldHandleMethodArgumentTypeMismatchExceptionNullType() {
            // Given
            MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
            when(ex.getName()).thenReturn("param");
            when(ex.getValue()).thenReturn("invalid");
            when(ex.getRequiredType()).thenReturn(null);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleTypeMismatch(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("unknown");
        }

        @Test
        @DisplayName("Should handle IllegalArgumentException")
        void shouldHandleIllegalArgumentException() {
            // Given
            IllegalArgumentException ex = new IllegalArgumentException("Invalid quantity: must be positive");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("INVALID_ARGUMENT");
            assertThat(response.getBody().getMessage()).isEqualTo("Invalid quantity: must be positive");
        }

        @Test
        @DisplayName("Should handle EmptyCartException")
        void shouldHandleEmptyCartException() {
            // Given
            EmptyCartException ex = new EmptyCartException();

            // When
            ResponseEntity<ErrorResponse> response = handler.handleEmptyCart(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("EMPTY_CART");
            assertThat(response.getBody().getMessage()).contains("empty cart");
        }

        @Test
        @DisplayName("Should handle TokenizationFailedException")
        void shouldHandleTokenizationFailedException() {
            // Given
            TokenizationFailedException ex = new TokenizationFailedException("Card expired");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleTokenizationFailed(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("TOKENIZATION_FAILED");
            assertThat(response.getBody().getMessage()).contains("Card expired");
        }
    }

    // ===========================================
    // 402 PAYMENT REQUIRED Tests
    // ===========================================

    @Nested
    @DisplayName("402 Payment Required")
    class PaymentRequiredTests {

        @Test
        @DisplayName("Should handle PaymentFailedException")
        void shouldHandlePaymentFailedException() {
            // Given
            UUID orderId = UUID.randomUUID();
            PaymentFailedException ex = new PaymentFailedException(orderId, "Card declined", 3);

            // When
            ResponseEntity<ErrorResponse> response = handler.handlePaymentFailed(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.PAYMENT_REQUIRED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(402);
            assertThat(response.getBody().getError()).isEqualTo("PAYMENT_FAILED");
            assertThat(response.getBody().getMessage()).contains("Card declined");
        }
    }

    // ===========================================
    // 404 NOT FOUND Tests
    // ===========================================

    @Nested
    @DisplayName("404 Not Found")
    class NotFoundTests {

        @Test
        @DisplayName("Should handle ProductNotFoundException")
        void shouldHandleProductNotFoundException() {
            // Given
            UUID productId = UUID.randomUUID();
            ProductNotFoundException ex = new ProductNotFoundException(productId);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("PRODUCT_NOT_FOUND");
        }

        @Test
        @DisplayName("Should handle OrderNotFoundException")
        void shouldHandleOrderNotFoundException() {
            // Given
            UUID orderId = UUID.randomUUID();
            OrderNotFoundException ex = new OrderNotFoundException(orderId);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("ORDER_NOT_FOUND");
        }

        @Test
        @DisplayName("Should handle CustomerNotFoundException")
        void shouldHandleCustomerNotFoundException() {
            // Given
            UUID customerId = UUID.randomUUID();
            CustomerNotFoundException ex = new CustomerNotFoundException(customerId);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("CUSTOMER_NOT_FOUND");
        }

        @Test
        @DisplayName("Should handle CartNotFoundException")
        void shouldHandleCartNotFoundException() {
            // Given
            UUID customerId = UUID.randomUUID();
            CartNotFoundException ex = new CartNotFoundException(customerId);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("CART_NOT_FOUND");
        }

        @Test
        @DisplayName("Should handle CreditCardNotFoundException")
        void shouldHandleCreditCardNotFoundException() {
            // Given
            UUID cardId = UUID.randomUUID();
            CreditCardNotFoundException ex = new CreditCardNotFoundException(cardId);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("CREDIT_CARD_NOT_FOUND");
        }

        @Test
        @DisplayName("Should handle NoResourceFoundException")
        void shouldHandleNoResourceFoundException() throws NoResourceFoundException {
            // Given
            NoResourceFoundException ex = new NoResourceFoundException(HttpMethod.GET, "/api/v1/nonexistent");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleNoResourceFound(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("ENDPOINT_NOT_FOUND");
            assertThat(response.getBody().getMessage()).contains("/api/v1/test");
        }
    }

    // ===========================================
    // 405 METHOD NOT ALLOWED Tests
    // ===========================================

    @Nested
    @DisplayName("405 Method Not Allowed")
    class MethodNotAllowedTests {

        @Test
        @DisplayName("Should handle HttpRequestMethodNotSupportedException with supported methods")
        void shouldHandleHttpRequestMethodNotSupportedException() {
            // Given
            HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException(
                    "DELETE", List.of("GET", "POST")
            );

            // When
            ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupported(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("METHOD_NOT_ALLOWED");
            assertThat(response.getBody().getMessage()).contains("DELETE");
            assertThat(response.getBody().getMessage()).contains("GET");
            assertThat(response.getBody().getMessage()).contains("POST");
        }

        @Test
        @DisplayName("Should handle HttpRequestMethodNotSupportedException without supported methods")
        void shouldHandleHttpRequestMethodNotSupportedExceptionNoSupported() {
            // Given
            HttpRequestMethodNotSupportedException ex = new HttpRequestMethodNotSupportedException("PATCH");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleMethodNotSupported(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("PATCH");
        }
    }

    // ===========================================
    // 409 CONFLICT Tests
    // ===========================================

    @Nested
    @DisplayName("409 Conflict - Business Rule Violations")
    class ConflictTests {

        @Test
        @DisplayName("Should handle InsufficientStockException")
        void shouldHandleInsufficientStockException() {
            // Given
            UUID productId = UUID.randomUUID();
            InsufficientStockException ex = new InsufficientStockException(productId, "Acetaminofén", 5, 10);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleInsufficientStock(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("INSUFFICIENT_STOCK");
            assertThat(response.getBody().getMessage()).contains("Acetaminofén");
        }

        @Test
        @DisplayName("Should handle CustomerAlreadyExistsException")
        void shouldHandleCustomerAlreadyExistsException() {
            // Given
            CustomerAlreadyExistsException ex = new CustomerAlreadyExistsException("test@example.com");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleCustomerAlreadyExists(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("CUSTOMER_ALREADY_EXISTS");
            assertThat(response.getBody().getMessage()).contains("test@example.com");
        }

        @Test
        @DisplayName("Should handle InvalidCardException")
        void shouldHandleInvalidCardException() {
            // Given
            InvalidCardException ex = new InvalidCardException("1234", "Card is blocked");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleInvalidCard(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("INVALID_CARD");
            assertThat(response.getBody().getMessage()).contains("blocked");
        }

        @Test
        @DisplayName("Should handle IllegalStateException")
        void shouldHandleIllegalStateException() {
            // Given
            IllegalStateException ex = new IllegalStateException("Cannot cancel a completed order");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleIllegalState(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("INVALID_STATE");
            assertThat(response.getBody().getMessage()).contains("Cannot cancel");
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException - duplicate email")
        void shouldHandleDataIntegrityViolationDuplicateEmail() {
            // Given
            RuntimeException rootCause = new RuntimeException("duplicate key value violates unique constraint on email");
            DataIntegrityViolationException ex = new DataIntegrityViolationException("", rootCause);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("DATA_INTEGRITY_VIOLATION");
            assertThat(response.getBody().getMessage()).contains("email already exists");
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException - duplicate phone")
        void shouldHandleDataIntegrityViolationDuplicatePhone() {
            // Given
            RuntimeException rootCause = new RuntimeException("duplicate key value violates unique constraint on phone");
            DataIntegrityViolationException ex = new DataIntegrityViolationException("", rootCause);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("phone number already exists");
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException - foreign key customer")
        void shouldHandleDataIntegrityViolationForeignKeyCustomer() {
            // Given
            RuntimeException rootCause = new RuntimeException("violates foreign key constraint on customer_id");
            DataIntegrityViolationException ex = new DataIntegrityViolationException("", rootCause);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("customer does not exist");
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException - foreign key product")
        void shouldHandleDataIntegrityViolationForeignKeyProduct() {
            // Given
            RuntimeException rootCause = new RuntimeException("violates foreign key constraint on product_id");
            DataIntegrityViolationException ex = new DataIntegrityViolationException("", rootCause);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("product does not exist");
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException - foreign key order")
        void shouldHandleDataIntegrityViolationForeignKeyOrder() {
            // Given
            RuntimeException rootCause = new RuntimeException("violates foreign key constraint on order_id");
            DataIntegrityViolationException ex = new DataIntegrityViolationException("", rootCause);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("order does not exist");
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException - not null")
        void shouldHandleDataIntegrityViolationNotNull() {
            // Given
            RuntimeException rootCause = new RuntimeException("null value in column \"name\" violates not-null constraint");
            DataIntegrityViolationException ex = new DataIntegrityViolationException("", rootCause);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("required field is missing");
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException - check constraint")
        void shouldHandleDataIntegrityViolationCheckConstraint() {
            // Given
            RuntimeException rootCause = new RuntimeException("violates check constraint on stock_positive");
            DataIntegrityViolationException ex = new DataIntegrityViolationException("", rootCause);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("values are within allowed ranges");
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException - generic duplicate")
        void shouldHandleDataIntegrityViolationGenericDuplicate() {
            // Given
            RuntimeException rootCause = new RuntimeException("duplicate key value violates unique constraint");
            DataIntegrityViolationException ex = new DataIntegrityViolationException("", rootCause);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("duplicate entry");
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException - generic foreign key")
        void shouldHandleDataIntegrityViolationGenericForeignKey() {
            // Given
            RuntimeException rootCause = new RuntimeException("violates foreign key constraint on some_unknown_fk");
            DataIntegrityViolationException ex = new DataIntegrityViolationException("", rootCause);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("referenced record does not exist");
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException - unknown error")
        void shouldHandleDataIntegrityViolationUnknown() {
            // Given
            RuntimeException rootCause = new RuntimeException("some unknown database error");
            DataIntegrityViolationException ex = new DataIntegrityViolationException("", rootCause);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("data constraint was violated");
        }

        @Test
        @DisplayName("Should handle DataIntegrityViolationException - null message")
        void shouldHandleDataIntegrityViolationNullMessage() {
            // Given - Use a mock to simulate null message from getMostSpecificCause
            DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
            Throwable nullCause = mock(Throwable.class);
            when(ex.getMostSpecificCause()).thenReturn(nullCause);
            when(nullCause.getMessage()).thenReturn(null);

            // When
            ResponseEntity<ErrorResponse> response = handler.handleDataIntegrityViolation(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getMessage()).contains("data integrity constraint was violated");
        }
    }

    // ===========================================
    // 500 INTERNAL SERVER ERROR Tests
    // ===========================================

    @Nested
    @DisplayName("500 Internal Server Error - Catch All")
    class InternalServerErrorTests {

        @Test
        @DisplayName("Should handle DomainException (fallback)")
        void shouldHandleDomainExceptionFallback() {
            // Given
            DomainException ex = new DomainException("Some domain error");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleDomainException(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("DOMAIN_ERROR");
            assertThat(response.getBody().getMessage()).isEqualTo("Some domain error");
        }

        @Test
        @DisplayName("Should handle generic Exception with secure message")
        void shouldHandleGenericException() {
            // Given
            Exception ex = new RuntimeException("NullPointerException at line 42");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleGenericException(ex, request);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(500);
            assertThat(response.getBody().getError()).isEqualTo("INTERNAL_SERVER_ERROR");
            // Should NOT expose internal details
            assertThat(response.getBody().getMessage()).doesNotContain("NullPointerException");
            assertThat(response.getBody().getMessage()).contains("unexpected error");
        }
    }

    // ===========================================
    // Helper Methods Tests
    // ===========================================

    @Nested
    @DisplayName("Helper Methods - extractErrorCode")
    class HelperMethodsTests {

        @Test
        @DisplayName("Should return RESOURCE_NOT_FOUND for unknown DomainException subtype")
        void shouldReturnResourceNotFoundForUnknownDomainException() {
            // Given - a custom domain exception not explicitly handled
            class CustomDomainException extends DomainException {
                CustomDomainException(String message) {
                    super(message);
                }
            }
            CustomDomainException ex = new CustomDomainException("Custom error");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleResourceNotFound(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getError()).isEqualTo("RESOURCE_NOT_FOUND");
        }
    }

    // ===========================================
    // Error Response Structure Tests
    // ===========================================

    @Nested
    @DisplayName("Error Response Structure")
    class ErrorResponseStructureTests {

        @Test
        @DisplayName("Should include timestamp in all responses")
        void shouldIncludeTimestampInAllResponses() {
            // Given
            IllegalArgumentException ex = new IllegalArgumentException("test");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("Should include path in all responses")
        void shouldIncludePathInAllResponses() {
            // Given
            when(request.getRequestURI()).thenReturn("/api/v1/custom/path");
            IllegalArgumentException ex = new IllegalArgumentException("test");

            // When
            ResponseEntity<ErrorResponse> response = handler.handleIllegalArgument(ex, request);

            // Then
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getPath()).isEqualTo("/api/v1/custom/path");
        }
    }
}
