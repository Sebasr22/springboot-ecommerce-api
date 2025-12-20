package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.advice;

import com.farmatodo.reto_tecnico.application.service.AuditLogService;
import com.farmatodo.reto_tecnico.domain.exception.*;
import com.farmatodo.reto_tecnico.domain.model.EventType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * Provides centralized error handling and consistent error responses.
 *
 * HTTP Status Code Mapping:
 * - 400 Bad Request: Validation errors, malformed JSON, invalid parameters
 * - 402 Payment Required: Payment processing failed
 * - 404 Not Found: Resource not found (Product, Order, Customer, Card, Endpoint)
 * - 405 Method Not Allowed: Wrong HTTP method used
 * - 409 Conflict: Business rule violation (Insufficient Stock, Duplicate Entry, Invalid State)
 * - 500 Internal Server Error: Unexpected errors (generic message for security)
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final AuditLogService auditLogService;

    // ===========================================
    // 400 BAD REQUEST - Input Validation Errors
    // ===========================================

    /**
     * Handles @Valid annotation validation failures on request bodies.
     * Returns field-level error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        log.warn("Validation error on {}: {}", request.getRequestURI(), errors);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("VALIDATION_ERROR")
                .message("Validation failed for one or more fields")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles @Validated annotation validation failures on method parameters.
     * Typically for @PathVariable or @RequestParam constraints.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> extractParameterName(violation),
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));

        log.warn("Constraint violation on {}: {}", request.getRequestURI(), errors);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("CONSTRAINT_VIOLATION")
                .message("Parameter validation failed")
                .path(request.getRequestURI())
                .validationErrors(errors)
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles invalid JSON format or unreadable request body.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        String message = "Malformed JSON request";

        // Extract more specific message if available
        Throwable cause = ex.getCause();
        if (cause != null) {
            String causeMessage = cause.getMessage();
            if (causeMessage != null && causeMessage.contains("Cannot deserialize")) {
                message = "Invalid value in JSON request. Please check field types.";
            } else if (causeMessage != null && causeMessage.contains("Unexpected character")) {
                message = "Invalid JSON syntax. Please verify the request body format.";
            }
        }

        log.warn("Malformed JSON on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("MALFORMED_JSON")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles missing required request parameters.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        String message = String.format("Required parameter '%s' of type '%s' is missing",
                ex.getParameterName(), ex.getParameterType());

        log.warn("Missing parameter on {}: {}", request.getRequestURI(), message);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("MISSING_PARAMETER")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles type mismatch errors (e.g., invalid UUID format in path variable).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String expectedType = ex.getRequiredType() != null
                ? ex.getRequiredType().getSimpleName()
                : "unknown";

        String message = String.format("Parameter '%s' must be a valid %s. Received: '%s'",
                ex.getName(), expectedType, ex.getValue());

        log.warn("Type mismatch on {}: {}", request.getRequestURI(), message);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("TYPE_MISMATCH")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles IllegalArgumentException from business validation.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid argument on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("INVALID_ARGUMENT")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles empty cart checkout attempts.
     */
    @ExceptionHandler(EmptyCartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleEmptyCart(
            EmptyCartException ex,
            HttpServletRequest request
    ) {
        log.warn("Empty cart on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("EMPTY_CART")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles tokenization failures (invalid card, expired card, etc).
     */
    @ExceptionHandler(TokenizationFailedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleTokenizationFailed(
            TokenizationFailedException ex,
            HttpServletRequest request
    ) {
        log.warn("Tokenization failed on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("TOKENIZATION_FAILED")
                .message("Credit card tokenization failed: " + ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    // ===========================================
    // 402 PAYMENT REQUIRED - Payment Failures
    // ===========================================

    /**
     * Handles payment processing failures.
     */
    @ExceptionHandler(PaymentFailedException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public ResponseEntity<ErrorResponse> handlePaymentFailed(
            PaymentFailedException ex,
            HttpServletRequest request
    ) {
        log.error("Payment failed on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.PAYMENT_REQUIRED.value())
                .error("PAYMENT_FAILED")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
    }

    // ===========================================
    // 404 NOT FOUND - Resource Not Found
    // ===========================================

    /**
     * Handles all domain "not found" exceptions.
     */
    @ExceptionHandler({
            ProductNotFoundException.class,
            OrderNotFoundException.class,
            CustomerNotFoundException.class,
            CartNotFoundException.class,
            CreditCardNotFoundException.class
    })
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            DomainException ex,
            HttpServletRequest request
    ) {
        log.warn("Resource not found on {}: {}", request.getRequestURI(), ex.getMessage());

        // Extract specific error code based on exception type
        String errorCode = extractErrorCode(ex);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error(errorCode)
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles endpoint not found (Spring 6.2+ NoResourceFoundException).
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException ex,
            HttpServletRequest request
    ) {
        log.warn("Endpoint not found: {}", request.getRequestURI());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("ENDPOINT_NOT_FOUND")
                .message("The requested endpoint does not exist: " + request.getRequestURI())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    // ===========================================
    // 405 METHOD NOT ALLOWED
    // ===========================================

    /**
     * Handles wrong HTTP method errors.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        String supportedMethods = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().toString()
                : "unknown";

        String message = String.format("Method '%s' not supported. Supported methods: %s",
                ex.getMethod(), supportedMethods);

        log.warn("Method not allowed on {}: {}", request.getRequestURI(), message);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .error("METHOD_NOT_ALLOWED")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // ===========================================
    // 409 CONFLICT - Business Rule Violations
    // ===========================================

    /**
     * Handles insufficient stock errors.
     */
    @ExceptionHandler(InsufficientStockException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(
            InsufficientStockException ex,
            HttpServletRequest request
    ) {
        log.warn("Insufficient stock on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("INSUFFICIENT_STOCK")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles customer already exists errors (duplicate email/phone).
     */
    @ExceptionHandler(CustomerAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleCustomerAlreadyExists(
            CustomerAlreadyExistsException ex,
            HttpServletRequest request
    ) {
        log.warn("Customer already exists on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("CUSTOMER_ALREADY_EXISTS")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles invalid card errors (validation failures beyond tokenization).
     */
    @ExceptionHandler(InvalidCardException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleInvalidCard(
            InvalidCardException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid card on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("INVALID_CARD")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles illegal state transitions (e.g., cancelling an already completed order).
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException ex,
            HttpServletRequest request
    ) {
        log.warn("Invalid state on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("INVALID_STATE")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Handles database integrity constraint violations.
     * Maps to 409 Conflict instead of 500 Internal Server Error.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest request
    ) {
        String message = extractDataIntegrityMessage(ex);

        log.warn("Data integrity violation on {}: {}", request.getRequestURI(), message);

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("DATA_INTEGRITY_VIOLATION")
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    // ===========================================
    // 500 INTERNAL SERVER ERROR - Catch-All
    // ===========================================

    /**
     * Handles any DomainException not caught by specific handlers.
     * Acts as a fallback for future domain exceptions.
     */
    @ExceptionHandler(DomainException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleDomainException(
            DomainException ex,
            HttpServletRequest request
    ) {
        log.error("Domain exception on {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("DOMAIN_ERROR")
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Catch-all handler for any unexpected exceptions.
     * Returns a generic message for security (hides internal details).
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        // Log full stack trace for debugging
        log.error("Unexpected error on {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        // RF8: Log error to audit trail (async, non-blocking)
        String errorMessage = String.format(
                "Unexpected error on %s: %s - %s",
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );
        auditLogService.logEventWithError(
                EventType.ERROR_OCCURRED,
                "System",
                null,
                "FAILURE",
                errorMessage
        );

        // Return generic message (never expose internal details to client)
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("INTERNAL_SERVER_ERROR")
                .message("An unexpected error occurred. Please try again later.")
                .path(request.getRequestURI())
                .build();

        return ResponseEntity.internalServerError().body(response);
    }

    // ===========================================
    // Helper Methods
    // ===========================================

    /**
     * Extracts parameter name from constraint violation path.
     */
    private String extractParameterName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        // Extract the last segment of the path (e.g., "findById.id" -> "id")
        int lastDot = path.lastIndexOf('.');
        return lastDot >= 0 ? path.substring(lastDot + 1) : path;
    }

    /**
     * Extracts a user-friendly message from DataIntegrityViolationException.
     */
    private String extractDataIntegrityMessage(DataIntegrityViolationException ex) {
        String rootMessage = ex.getMostSpecificCause().getMessage();

        if (rootMessage == null) {
            return "A data integrity constraint was violated. Please verify your request.";
        }

        // Common PostgreSQL constraint violation patterns
        if (rootMessage.contains("duplicate key") || rootMessage.contains("unique constraint")) {
            if (rootMessage.contains("email")) {
                return "A record with this email already exists.";
            }
            if (rootMessage.contains("phone")) {
                return "A record with this phone number already exists.";
            }
            return "A record with this value already exists (duplicate entry).";
        }

        if (rootMessage.contains("foreign key") || rootMessage.contains("violates foreign key constraint")) {
            if (rootMessage.contains("customer")) {
                return "Referenced customer does not exist.";
            }
            if (rootMessage.contains("product")) {
                return "Referenced product does not exist.";
            }
            if (rootMessage.contains("order")) {
                return "Referenced order does not exist.";
            }
            return "A referenced record does not exist. Please verify all IDs are valid.";
        }

        if (rootMessage.contains("not-null") || rootMessage.contains("null value")) {
            return "A required field is missing. Please provide all mandatory data.";
        }

        if (rootMessage.contains("check constraint")) {
            return "Data validation failed. Please verify the values are within allowed ranges.";
        }

        // Fallback generic message (never expose raw SQL errors)
        return "A data constraint was violated. Please verify your request data.";
    }

    /**
     * Extracts specific error code based on exception type.
     */
    private String extractErrorCode(DomainException ex) {
        if (ex instanceof ProductNotFoundException) {
            return "PRODUCT_NOT_FOUND";
        }
        if (ex instanceof OrderNotFoundException) {
            return "ORDER_NOT_FOUND";
        }
        if (ex instanceof CustomerNotFoundException) {
            return "CUSTOMER_NOT_FOUND";
        }
        if (ex instanceof CartNotFoundException) {
            return "CART_NOT_FOUND";
        }
        if (ex instanceof CreditCardNotFoundException) {
            return "CREDIT_CARD_NOT_FOUND";
        }
        return "RESOURCE_NOT_FOUND";
    }
}
