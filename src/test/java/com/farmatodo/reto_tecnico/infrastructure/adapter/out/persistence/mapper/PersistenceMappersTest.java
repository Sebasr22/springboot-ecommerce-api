package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.reto_tecnico.domain.model.*;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for all persistence mappers.
 * Uses MapStruct-generated implementations directly (not mocked).
 * This ensures coverage of the generated code.
 */
@DisplayName("Persistence Mappers Comprehensive Tests")
class PersistenceMappersTest {

    // All mapper implementations
    private CustomerMapper customerMapper;
    private ProductMapper productMapper;
    private OrderMapper orderMapper;
    private OrderItemMapper orderItemMapper;
    private CartMapper cartMapper;
    private CartItemMapper cartItemMapper;
    private SearchLogMapper searchLogMapper;

    @BeforeEach
    void setUp() {
        // Instantiate MapStruct-generated implementations directly
        customerMapper = new CustomerMapperImpl();
        productMapper = new ProductMapperImpl();
        cartItemMapper = new CartItemMapperImpl();
        cartMapper = new CartMapperImpl();
        searchLogMapper = new SearchLogMapperImpl();

        // OrderItemMapper and OrderMapper require dependent mappers
        orderItemMapper = new OrderItemMapperImpl();
        orderMapper = new OrderMapperImpl();
    }

    @Nested
    @DisplayName("OrderItemMapper Tests")
    class OrderItemMapperTests {

        @Test
        @DisplayName("Should convert OrderItem to OrderItemEntity")
        void shouldConvertOrderItemToEntity() {
            // Given
            Product product = Product.builder()
                    .id(UUID.randomUUID())
                    .name("Test Product")
                    .price(Money.of(new BigDecimal("15000.00")))
                    .stock(50)
                    .build();

            OrderItem orderItem = OrderItem.builder()
                    .id(UUID.randomUUID())
                    .product(product)
                    .quantity(3)
                    .unitPrice(Money.of(new BigDecimal("15000.00")))
                    .build();

            // When
            OrderItemEntity entity = orderItemMapper.toEntity(orderItem);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(orderItem.getId());
            assertThat(entity.getProductId()).isEqualTo(product.getId());
            assertThat(entity.getQuantity()).isEqualTo(3);
            assertThat(entity.getUnitPrice()).isEqualByComparingTo(new BigDecimal("15000.00"));
        }

        @Test
        @DisplayName("Should convert OrderItemEntity to OrderItem with Product")
        void shouldConvertEntityToOrderItemWithProduct() {
            // Given
            UUID productId = UUID.randomUUID();
            Product product = Product.builder()
                    .id(productId)
                    .name("Test Product")
                    .price(Money.of(new BigDecimal("20000.00")))
                    .stock(100)
                    .build();

            OrderItemEntity entity = OrderItemEntity.builder()
                    .id(UUID.randomUUID())
                    .productId(productId)
                    .quantity(2)
                    .unitPrice(new BigDecimal("20000.00"))
                    .build();

            // When
            OrderItem orderItem = orderItemMapper.toDomain(entity, product);

            // Then
            assertThat(orderItem).isNotNull();
            assertThat(orderItem.getId()).isEqualTo(entity.getId());
            assertThat(orderItem.getProduct()).isEqualTo(product);
            assertThat(orderItem.getQuantity()).isEqualTo(2);
            assertThat(orderItem.getUnitPrice().amount()).isEqualByComparingTo(new BigDecimal("20000.00"));
        }

        @Test
        @DisplayName("Should handle null OrderItem")
        void shouldHandleNullOrderItem() {
            // When
            OrderItemEntity entity = orderItemMapper.toEntity(null);

            // Then
            assertThat(entity).isNull();
        }
    }

    @Nested
    @DisplayName("OrderMapper Tests")
    class OrderMapperTests {

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
        }

        @Test
        @DisplayName("Should convert Order to OrderEntity")
        void shouldConvertOrderToEntity() {
            // Given
            Product product = Product.builder()
                    .id(UUID.randomUUID())
                    .name("Product")
                    .price(Money.of(new BigDecimal("10000.00")))
                    .stock(10)
                    .build();

            OrderItem item = OrderItem.builder()
                    .id(UUID.randomUUID())
                    .product(product)
                    .quantity(2)
                    .unitPrice(product.getPrice())
                    .build();

            Order order = Order.builder()
                    .id(UUID.randomUUID())
                    .customer(testCustomer)
                    .items(new ArrayList<>(List.of(item)))
                    .totalAmount(Money.of(new BigDecimal("20000.00")))
                    .status(Order.OrderStatus.PENDING)
                    .paymentToken("tok_123")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // When
            OrderEntity entity = orderMapper.toEntity(order);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(order.getId());
            assertThat(entity.getCustomerId()).isEqualTo(testCustomer.getId());
            assertThat(entity.getTotalAmount()).isEqualByComparingTo(new BigDecimal("20000.00"));
            assertThat(entity.getStatus()).isEqualTo(OrderEntity.OrderStatus.PENDING);
            assertThat(entity.getPaymentToken()).isEqualTo("tok_123");
        }

        @Test
        @DisplayName("Should convert OrderEntity to Order with Customer")
        void shouldConvertEntityToOrderWithCustomer() {
            // Given
            OrderEntity entity = OrderEntity.builder()
                    .id(UUID.randomUUID())
                    .customerId(testCustomer.getId())
                    .items(new ArrayList<>())
                    .totalAmount(new BigDecimal("30000.00"))
                    .status(OrderEntity.OrderStatus.PAYMENT_CONFIRMED)
                    .paymentToken("tok_456")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // When
            Order order = orderMapper.toDomain(entity, testCustomer);

            // Then
            assertThat(order).isNotNull();
            assertThat(order.getId()).isEqualTo(entity.getId());
            assertThat(order.getCustomer()).isEqualTo(testCustomer);
            assertThat(order.getTotalAmount().amount()).isEqualByComparingTo(new BigDecimal("30000.00"));
            assertThat(order.getStatus()).isEqualTo(Order.OrderStatus.PAYMENT_CONFIRMED);
        }

        @Test
        @DisplayName("Should map all OrderStatus values")
        void shouldMapAllOrderStatusValues() {
            for (Order.OrderStatus domainStatus : Order.OrderStatus.values()) {
                OrderEntity.OrderStatus entityStatus = orderMapper.mapOrderStatus(domainStatus);
                assertThat(entityStatus.name()).isEqualTo(domainStatus.name());
            }

            for (OrderEntity.OrderStatus entityStatus : OrderEntity.OrderStatus.values()) {
                Order.OrderStatus domainStatus = orderMapper.mapEntityStatus(entityStatus);
                assertThat(domainStatus.name()).isEqualTo(entityStatus.name());
            }
        }

        @Test
        @DisplayName("Should handle null Order")
        void shouldHandleNullOrder() {
            OrderEntity entity = orderMapper.toEntity(null);
            assertThat(entity).isNull();
        }
    }

    @Nested
    @DisplayName("CartMapper Tests")
    class CartMapperTests {

        @Test
        @DisplayName("Should convert Cart to CartEntity")
        void shouldConvertCartToEntity() {
            // Given
            Cart cart = Cart.builder()
                    .id(UUID.randomUUID())
                    .customerId(UUID.randomUUID())
                    .items(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // When
            CartEntity entity = cartMapper.toEntity(cart);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(cart.getId());
            assertThat(entity.getCustomerId()).isEqualTo(cart.getCustomerId());
        }

        @Test
        @DisplayName("Should convert CartEntity to Cart")
        void shouldConvertEntityToCart() {
            // Given
            CartEntity entity = CartEntity.builder()
                    .id(UUID.randomUUID())
                    .customerId(UUID.randomUUID())
                    .items(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            // When
            Cart cart = cartMapper.toDomain(entity);

            // Then
            assertThat(cart).isNotNull();
            assertThat(cart.getId()).isEqualTo(entity.getId());
            assertThat(cart.getCustomerId()).isEqualTo(entity.getCustomerId());
        }

        @Test
        @DisplayName("Should handle null Cart")
        void shouldHandleNullCart() {
            CartEntity entity = cartMapper.toEntity(null);
            assertThat(entity).isNull();
        }

        @Test
        @DisplayName("Should handle null CartEntity")
        void shouldHandleNullCartEntity() {
            Cart cart = cartMapper.toDomain(null);
            assertThat(cart).isNull();
        }
    }

    @Nested
    @DisplayName("CartItemMapper Tests")
    class CartItemMapperTests {

        private Product testProduct;
        private CartItemMapper cartItemMapperWithDeps;

        @BeforeEach
        void setUp() throws Exception {
            testProduct = Product.builder()
                    .id(UUID.randomUUID())
                    .name("Cart Product")
                    .price(Money.of(new BigDecimal("12000.00")))
                    .stock(25)
                    .build();

            // CartItemMapperImpl requires ProductMapper injected via reflection
            cartItemMapperWithDeps = new CartItemMapperImpl();
            java.lang.reflect.Field productMapperField = CartItemMapperImpl.class.getDeclaredField("productMapper");
            productMapperField.setAccessible(true);
            productMapperField.set(cartItemMapperWithDeps, new ProductMapperImpl());
        }

        @Test
        @DisplayName("Should convert CartItem to CartItemEntity")
        void shouldConvertCartItemToEntity() {
            // Given
            CartItem cartItem = CartItem.builder()
                    .id(UUID.randomUUID())
                    .product(testProduct)
                    .quantity(4)
                    .unitPrice(Money.of(new BigDecimal("12000.00")))
                    .build();

            // When
            CartItemEntity entity = cartItemMapperWithDeps.toEntity(cartItem);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(cartItem.getId());
            assertThat(entity.getQuantity()).isEqualTo(4);
            assertThat(entity.getUnitPrice()).isEqualByComparingTo(new BigDecimal("12000.00"));
        }

        @Test
        @DisplayName("Should convert CartItemEntity to CartItem")
        void shouldConvertEntityToCartItem() {
            // Given
            ProductEntity productEntity = ProductEntity.builder()
                    .id(UUID.randomUUID())
                    .name("Entity Product")
                    .price(new BigDecimal("18000.00"))
                    .stock(30)
                    .build();

            CartItemEntity entity = CartItemEntity.builder()
                    .id(UUID.randomUUID())
                    .product(productEntity)
                    .quantity(5)
                    .unitPrice(new BigDecimal("18000.00"))
                    .build();

            // When
            CartItem cartItem = cartItemMapperWithDeps.toDomain(entity);

            // Then
            assertThat(cartItem).isNotNull();
            assertThat(cartItem.getId()).isEqualTo(entity.getId());
            assertThat(cartItem.getQuantity()).isEqualTo(5);
            assertThat(cartItem.getUnitPrice().amount()).isEqualByComparingTo(new BigDecimal("18000.00"));
        }

        @Test
        @DisplayName("Should handle null CartItem")
        void shouldHandleNullCartItem() {
            CartItemEntity entity = cartItemMapperWithDeps.toEntity(null);
            assertThat(entity).isNull();
        }

        @Test
        @DisplayName("Should handle null CartItemEntity")
        void shouldHandleNullCartItemEntity() {
            CartItem cartItem = cartItemMapperWithDeps.toDomain(null);
            assertThat(cartItem).isNull();
        }
    }

    @Nested
    @DisplayName("SearchLogMapper Tests")
    class SearchLogMapperTests {

        @Test
        @DisplayName("Should convert SearchLog to SearchLogEntity")
        void shouldConvertSearchLogToEntity() {
            // Given
            SearchLog searchLog = SearchLog.builder()
                    .id(UUID.randomUUID())
                    .query("acetaminofen")
                    .resultsCount(10)
                    .customerId(UUID.randomUUID())
                    .searchTimestamp(LocalDateTime.now())
                    .traceId("trace-123")
                    .build();

            // When
            SearchLogEntity entity = searchLogMapper.toEntity(searchLog);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(searchLog.getId());
            assertThat(entity.getQuery()).isEqualTo("acetaminofen");
            assertThat(entity.getResultsCount()).isEqualTo(10);
            assertThat(entity.getTraceId()).isEqualTo("trace-123");
        }

        @Test
        @DisplayName("Should convert SearchLogEntity to SearchLog")
        void shouldConvertEntityToSearchLog() {
            // Given
            SearchLogEntity entity = SearchLogEntity.builder()
                    .id(UUID.randomUUID())
                    .query("ibuprofeno")
                    .resultsCount(5)
                    .customerId(UUID.randomUUID())
                    .searchTimestamp(LocalDateTime.now())
                    .traceId("trace-456")
                    .build();

            // When
            SearchLog searchLog = searchLogMapper.toDomain(entity);

            // Then
            assertThat(searchLog).isNotNull();
            assertThat(searchLog.getId()).isEqualTo(entity.getId());
            assertThat(searchLog.getQuery()).isEqualTo("ibuprofeno");
            assertThat(searchLog.getResultsCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should handle null SearchLog")
        void shouldHandleNullSearchLog() {
            SearchLogEntity entity = searchLogMapper.toEntity(null);
            assertThat(entity).isNull();
        }

        @Test
        @DisplayName("Should handle null SearchLogEntity")
        void shouldHandleNullSearchLogEntity() {
            SearchLog searchLog = searchLogMapper.toDomain(null);
            assertThat(searchLog).isNull();
        }

        @Test
        @DisplayName("Should handle SearchLog with null optional fields")
        void shouldHandleSearchLogWithNullOptionalFields() {
            // Given
            SearchLog searchLog = SearchLog.builder()
                    .id(UUID.randomUUID())
                    .query("test")
                    .resultsCount(0)
                    .customerId(null)
                    .searchTimestamp(LocalDateTime.now())
                    .traceId(null)
                    .build();

            // When
            SearchLogEntity entity = searchLogMapper.toEntity(searchLog);

            // Then
            assertThat(entity).isNotNull();
            assertThat(entity.getCustomerId()).isNull();
            assertThat(entity.getTraceId()).isNull();
        }
    }
}
