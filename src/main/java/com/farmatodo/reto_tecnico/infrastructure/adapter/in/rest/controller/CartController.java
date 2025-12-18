package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.controller;

import com.farmatodo.reto_tecnico.domain.model.Cart;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.port.in.AddToCartUseCase;
import com.farmatodo.reto_tecnico.domain.port.in.CheckoutCartUseCase;
import com.farmatodo.reto_tecnico.domain.port.in.GetCartUseCase;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request.AddCartItemRequest;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.CartResponse;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.CheckoutResponse;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper.CartRestMapper;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.UUID;

/**
 * REST controller for shopping cart operations.
 * Handles adding items, viewing cart, and checkout.
 */
@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cart", description = "Shopping cart management endpoints")
public class CartController {

    private final AddToCartUseCase addToCartUseCase;
    private final GetCartUseCase getCartUseCase;
    private final CheckoutCartUseCase checkoutCartUseCase;
    private final CartRestMapper cartMapper;

    @PostMapping("/items")
    @Operation(
            summary = "Add product to cart",
            description = "Adds a product to the customer's shopping cart. If the product already exists, increases the quantity."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Product added to cart successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Insufficient stock")
    })
    public ResponseEntity<CartResponse> addToCart(@Valid @RequestBody AddCartItemRequest request) {
        log.info("Adding product {} (quantity: {}) to cart for customer: {}",
                request.getProductId(), request.getQuantity(), request.getCustomerId());

        Cart cart = addToCartUseCase.addToCart(
                request.getCustomerId(),
                request.getProductId(),
                request.getQuantity()
        );

        CartResponse response = cartMapper.toResponse(cart);

        log.info("Product added successfully. Cart now has {} items", cart.getTotalItemCount());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{customerId}")
    @Operation(
            summary = "Get shopping cart",
            description = "Retrieves the current shopping cart for a customer. Creates an empty cart if none exists."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cart retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CartResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid customer ID")
    })
    public ResponseEntity<CartResponse> getCart(@PathVariable UUID customerId) {
        log.info("Retrieving cart for customer: {}", customerId);

        Cart cart = getCartUseCase.getCart(customerId);
        CartResponse response = cartMapper.toResponse(cart);

        log.info("Cart retrieved with {} items", cart.getTotalItemCount());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/checkout/{customerId}")
    @Operation(
            summary = "Checkout cart",
            description = "Creates an order from the cart items and clears the cart. Returns the created order ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Checkout successful, order created",
                    content = @Content(schema = @Schema(implementation = CheckoutResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Cart is empty or invalid"),
            @ApiResponse(responseCode = "404", description = "Cart or customer not found"),
            @ApiResponse(responseCode = "409", description = "Insufficient stock for one or more items")
    })
    public ResponseEntity<CheckoutResponse> checkout(@PathVariable UUID customerId) {
        log.info("Processing checkout for customer: {}", customerId);

        Order order = checkoutCartUseCase.checkout(customerId);

        CheckoutResponse response = CheckoutResponse.builder()
                .orderId(order.getId())
                .message("Checkout successful. Order created.")
                .build();

        log.info("Checkout successful. Order created: {}", order.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
