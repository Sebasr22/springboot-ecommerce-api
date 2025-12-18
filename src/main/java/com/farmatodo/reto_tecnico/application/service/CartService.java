package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.exception.CartNotFoundException;
import com.farmatodo.reto_tecnico.domain.exception.EmptyCartException;
import com.farmatodo.reto_tecnico.domain.exception.InsufficientStockException;
import com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.Cart;
import com.farmatodo.reto_tecnico.domain.model.CartItem;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.port.in.AddToCartUseCase;
import com.farmatodo.reto_tecnico.domain.port.in.CheckoutCartUseCase;
import com.farmatodo.reto_tecnico.domain.port.in.CreateOrderUseCase;
import com.farmatodo.reto_tecnico.domain.port.in.GetCartUseCase;
import com.farmatodo.reto_tecnico.domain.port.out.CartRepositoryPort;
import com.farmatodo.reto_tecnico.domain.port.out.ProductRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service implementation for shopping cart operations.
 * Handles adding items to cart, retrieving cart, and checkout process.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService implements AddToCartUseCase, GetCartUseCase, CheckoutCartUseCase {

    private final CartRepositoryPort cartRepository;
    private final ProductRepositoryPort productRepository;
    private final CreateOrderUseCase orderService;

    @Override
    @Transactional
    public Cart addToCart(UUID customerId, UUID productId, int quantity) {
        log.info("Adding product {} (quantity: {}) to cart for customer: {}", productId, quantity, customerId);

        // Validate quantity
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        // Find or create cart
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createNewCart(customerId));

        // Find product
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        // Calculate total quantity needed (existing in cart + new quantity)
        int existingQuantity = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .map(CartItem::getQuantity)
                .findFirst()
                .orElse(0);

        int finalQuantity = existingQuantity + quantity;

        if (!product.hasSufficientStock(finalQuantity)) {
            log.warn("Insufficient stock for product: {}. Requested: {}, Available: {}",
                    productId, finalQuantity, product.getStock());
            throw new InsufficientStockException(
                    product.getId(),
                    product.getName(),
                    product.getStock(),
                    finalQuantity
            );
        }

        // Add product to cart
        cart.addProduct(product, quantity);

        // Save cart
        Cart savedCart = cartRepository.save(cart);

        log.info("Product {} added to cart for customer: {}. Total items: {}",
                productId, customerId, savedCart.getTotalItemCount());

        return savedCart;
    }

    @Override
    @Transactional(readOnly = true)
    public Cart getCart(UUID customerId) {
        log.info("Retrieving cart for customer: {}", customerId);

        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> createNewCart(customerId));
    }

    @Override
    @Transactional
    public Order checkout(UUID customerId) {
        log.info("Starting checkout for customer: {}", customerId);

        // Find cart
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new CartNotFoundException(customerId));

        // Validate cart is not empty
        if (cart.isEmpty()) {
            log.warn("Cannot checkout empty cart for customer: {}", customerId);
            throw new EmptyCartException();
        }

        // Convert cart items to order items
        List<OrderItem> orderItems = cart.getItems().stream()
                .map(this::convertToOrderItem)
                .toList();

        log.info("Creating order with {} items for customer: {}", orderItems.size(), customerId);

        // Create order using OrderService
        Order order = orderService.createOrder(customerId, orderItems);

        // Clear cart after successful order creation
        log.info("Order created successfully: {}. Clearing cart for customer: {}",
                order.getId(), customerId);
        cartRepository.deleteByCustomerId(customerId);

        return order;
    }

    /**
     * Creates a new empty cart for a customer.
     *
     * @param customerId the customer ID
     * @return new cart
     */
    private Cart createNewCart(UUID customerId) {
        log.info("Creating new cart for customer: {}", customerId);

        Cart newCart = Cart.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return cartRepository.save(newCart);
    }

    /**
     * Converts a CartItem to an OrderItem.
     *
     * @param cartItem the cart item
     * @return order item
     */
    private OrderItem convertToOrderItem(CartItem cartItem) {
        return OrderItem.builder()
                .id(UUID.randomUUID())
                .product(cartItem.getProduct())
                .quantity(cartItem.getQuantity())
                .unitPrice(cartItem.getUnitPrice())
                .build();
    }
}
