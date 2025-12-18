package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.domain.exception.OrderNotFoundException;
import com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.port.in.CreateOrderUseCase;
import com.farmatodo.reto_tecnico.domain.port.in.SearchProductUseCase;
import com.farmatodo.reto_tecnico.domain.port.out.OrderRepositoryPort;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.advice.ErrorResponse;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request.CreateOrderRequest;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request.OrderItemRequest;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.OrderResponse;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.OrderRestMapper;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for order management.
 * Provides endpoints for creating and retrieving orders.
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "Order management API")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final SearchProductUseCase searchProductUseCase;
    private final OrderRepositoryPort orderRepository;
    private final OrderRestMapper mapper;

    @PostMapping
    @Operation(
        summary = "Create new order",
        description = "Creates a new order with customer information and items. " +
                      "Supports two flows: (1) Provide customerId for existing customer, OR " +
                      "(2) Provide complete customer data (name, email, phone, address) for new customer. " +
                      "Validates product availability and stock levels. " +
                      "Returns order in PENDING status awaiting payment."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Order created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request - validation errors or missing required fields",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing API key",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product or customer not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Insufficient stock for requested products",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request
    ) {
        // Validate request logic: either customerId OR customer data must be provided
        validateOrderRequest(request);

        // Build order items with products
        List<OrderItem> orderItems = buildOrderItems(request.getItems());

        // Create order using appropriate flow
        Order order;
        if (request.getCustomerId() != null) {
            // Flow 1: Use existing customer by ID
            log.info("Creating order for existing customer ID: {}", request.getCustomerId());
            order = createOrderUseCase.createOrder(request.getCustomerId(), orderItems);
        } else {
            // Flow 2: Create order with new/provided customer data
            log.info("Creating order for customer: {}", request.getCustomerEmail());
            Customer customer = mapper.toCustomer(request);
            order = createOrderUseCase.createOrder(customer, orderItems);
        }

        OrderResponse response = mapper.toResponse(order);
        log.info("Order created successfully: {}", order.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Validates that the request contains either customerId OR complete customer data.
     * @param request the order creation request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateOrderRequest(CreateOrderRequest request) {
        if (request.getCustomerId() == null) {
            // Customer ID not provided, validate that all customer data is present
            List<String> missingFields = new ArrayList<>();

            if (request.getCustomerName() == null || request.getCustomerName().isBlank()) {
                missingFields.add("customerName");
            }
            if (request.getCustomerEmail() == null || request.getCustomerEmail().isBlank()) {
                missingFields.add("customerEmail");
            }
            if (request.getCustomerPhone() == null || request.getCustomerPhone().isBlank()) {
                missingFields.add("customerPhone");
            }
            if (request.getCustomerAddress() == null || request.getCustomerAddress().isBlank()) {
                missingFields.add("customerAddress");
            }

            if (!missingFields.isEmpty()) {
                String message = String.format(
                    "When customerId is not provided, all customer fields are required. Missing fields: %s",
                    String.join(", ", missingFields)
                );
                log.warn("Order creation validation failed: {}", message);
                throw new IllegalArgumentException(message);
            }
        }
    }

    /**
     * Builds domain OrderItem objects from request DTOs.
     * @param itemRequests list of item request DTOs
     * @return list of domain OrderItem objects
     * @throws ProductNotFoundException if any product is not found
     */
    private List<OrderItem> buildOrderItems(List<OrderItemRequest> itemRequests) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequest itemRequest : itemRequests) {
            Product product = searchProductUseCase.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException(itemRequest.getProductId()));

            OrderItem orderItem = OrderItem.create(product, itemRequest.getQuantity());
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get order by ID",
        description = "Retrieves order details by UUID. Returns order with all items and current status."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Order found successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = OrderResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid or missing API key",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Order not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<OrderResponse> getOrderById(
            @Parameter(description = "Order UUID", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id
    ) {
        log.info("Retrieving order: {}", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        OrderResponse response = mapper.toResponse(order);
        log.info("Order retrieved successfully: {}", id);

        return ResponseEntity.ok(response);
    }
}
