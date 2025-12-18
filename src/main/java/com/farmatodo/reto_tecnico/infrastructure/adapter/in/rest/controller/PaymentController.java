package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.domain.exception.OrderNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.CreditCard;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.port.in.ProcessPaymentUseCase;
import com.farmatodo.reto_tecnico.domain.port.out.OrderRepositoryPort;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request.ProcessPaymentRequest;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.PaymentResponse;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.CreditCardRestMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for payment processing.
 * Provides endpoint for processing payments on orders.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payments", description = "Payment processing API")
public class PaymentController {

    private final ProcessPaymentUseCase processPaymentUseCase;
    private final OrderRepositoryPort orderRepository;
    private final CreditCardRestMapper creditCardMapper;

    @PostMapping("/orders/{orderId}")
    @Operation(
        summary = "Process payment for order",
        description = "Processes payment for an existing order. Supports two flows: " +
                      "(1) Provide paymentToken for existing tokenized card, OR " +
                      "(2) Provide creditCard details for new card (will be tokenized automatically). " +
                      "Implements automatic retry logic (up to 3 attempts by default)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Payment processed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PaymentResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - validation errors or missing required fields",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing API key"
        ),
        @ApiResponse(
            responseCode = "402",
            description = "Payment Required - Payment failed after all retry attempts",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error - tokenization or payment processing failed"
        )
    })
    public ResponseEntity<PaymentResponse> processPayment(
            @Parameter(description = "Order UUID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID orderId,
            @Valid @RequestBody ProcessPaymentRequest request
    ) {
        log.info("Processing payment for order: {}", orderId);

        // Validate request: must have either paymentToken OR creditCard
        validatePaymentRequest(request);

        // Load order
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Process payment using appropriate flow
        ProcessPaymentUseCase.PaymentResult result;
        if (request.getPaymentToken() != null && !request.getPaymentToken().isBlank()) {
            // Flow 1: Use existing payment token (skip tokenization)
            log.info("Processing payment with existing token for order: {}", orderId);
            result = processPaymentUseCase.processPaymentWithToken(order, request.getPaymentToken());
        } else {
            // Flow 2: Use credit card (includes tokenization)
            log.info("Processing payment with credit card for order: {}", orderId);
            CreditCard creditCard = creditCardMapper.toDomain(request.getCreditCard());
            result = processPaymentUseCase.processPayment(order, creditCard);
        }

        // Build response
        PaymentResponse response = PaymentResponse.builder()
                .orderId(orderId)
                .success(result.success())
                .transactionId(result.transactionId())
                .attempts(result.attemptsMade())
                .message(result.success() ?
                        "Payment processed successfully on attempt " + result.attemptsMade() :
                        "Payment failed after " + result.attemptsMade() + " attempts")
                .orderStatus(order.getStatus().name())
                .build();

        log.info("Payment processing completed for order {}: success={}, attempts={}",
                orderId, result.success(), result.attemptsMade());

        return ResponseEntity.ok(response);
    }

    /**
     * Validates that the request contains either paymentToken OR creditCard (but not neither).
     * @param request the payment request
     * @throws IllegalArgumentException if validation fails
     */
    private void validatePaymentRequest(ProcessPaymentRequest request) {
        boolean hasToken = request.getPaymentToken() != null && !request.getPaymentToken().isBlank();
        boolean hasCreditCard = request.getCreditCard() != null;

        if (!hasToken && !hasCreditCard) {
            String message = "Payment request must provide either 'paymentToken' (for existing token) " +
                           "OR 'creditCard' (for new card). Both are missing.";
            log.warn("Payment validation failed: {}", message);
            throw new IllegalArgumentException(message);
        }

        // Log which flow is being used
        if (hasToken) {
            log.debug("Payment validation passed: using existing token");
        } else {
            log.debug("Payment validation passed: using credit card (will be tokenized)");
        }
    }
}
