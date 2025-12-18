package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.adapter;

import com.farmatodo.reto_tecnico.domain.exception.CustomerNotFoundException;
import com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.OrderEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.OrderItemEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.CustomerMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.OrderItemMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.OrderMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.ProductMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.CustomerJpaRepository;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.OrderJpaRepository;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderRepositoryAdapter.
 * Tests both happy paths and sad paths (error scenarios).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderRepositoryAdapter Unit Tests")
class OrderRepositoryAdapterTest {

    @Mock
    private OrderJpaRepository orderJpaRepository;

    @Mock
    private CustomerJpaRepository customerJpaRepository;

    @Mock
    private ProductJpaRepository productJpaRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private OrderItemMapper orderItemMapper;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private OrderRepositoryAdapter orderRepositoryAdapter;

    private Customer testCustomer;
    private Order testOrder;
    private OrderEntity testOrderEntity;
    private CustomerEntity testCustomerEntity;
    private ProductEntity testProductEntity;

    @BeforeEach
    void setUp() {
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        testCustomer = Customer.builder()
                .id(customerId)
                .name("Test Customer")
                .email(new Email("test@example.com"))
                .phone(new Phone("3001234567"))
                .address("Test Address")
                .build();

        testCustomerEntity = CustomerEntity.builder()
                .id(customerId)
                .name("Test Customer")
                .email("test@example.com")
                .phone("3001234567")
                .address("Test Address")
                .build();

        testProductEntity = ProductEntity.builder()
                .id(productId)
                .name("Test Product")
                .price(new BigDecimal("10000.00"))
                .stock(100)
                .build();

        Product product = Product.builder()
                .id(productId)
                .name("Test Product")
                .price(Money.of(new BigDecimal("10000.00")))
                .stock(100)
                .build();

        OrderItem item = OrderItem.builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .unitPrice(Money.of(new BigDecimal("10000.00")))
                .build();

        testOrder = Order.builder()
                .id(orderId)
                .customer(testCustomer)
                .items(new ArrayList<>(List.of(item)))
                .totalAmount(Money.of(new BigDecimal("20000.00")))
                .status(Order.OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        OrderItemEntity itemEntity = OrderItemEntity.builder()
                .id(UUID.randomUUID())
                .productId(productId)
                .quantity(2)
                .unitPrice(new BigDecimal("10000.00"))
                .build();

        testOrderEntity = OrderEntity.builder()
                .id(orderId)
                .customerId(customerId)
                .items(new ArrayList<>(List.of(itemEntity)))
                .totalAmount(new BigDecimal("20000.00"))
                .status(OrderEntity.OrderStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should return order when found")
        void shouldReturnOrderWhenFound() {
            // Given
            UUID orderId = testOrderEntity.getId();
            when(orderJpaRepository.findById(orderId)).thenReturn(Optional.of(testOrderEntity));
            when(customerJpaRepository.findById(testOrderEntity.getCustomerId())).thenReturn(Optional.of(testCustomerEntity));
            when(customerMapper.toDomain(testCustomerEntity)).thenReturn(testCustomer);
            when(orderMapper.toDomain(testOrderEntity, testCustomer)).thenReturn(testOrder);
            when(productJpaRepository.findById(any(UUID.class))).thenReturn(Optional.of(testProductEntity));
            when(productMapper.toDomain(testProductEntity)).thenReturn(testOrder.getItems().get(0).getProduct());
            when(orderItemMapper.toDomain(any(OrderItemEntity.class), any(Product.class))).thenReturn(testOrder.getItems().get(0));

            // When
            Optional<Order> result = orderRepositoryAdapter.findById(orderId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("Should return empty when order not found")
        void shouldReturnEmptyWhenOrderNotFound() {
            // Given
            UUID orderId = UUID.randomUUID();
            when(orderJpaRepository.findById(orderId)).thenReturn(Optional.empty());

            // When
            Optional<Order> result = orderRepositoryAdapter.findById(orderId);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should throw CustomerNotFoundException when customer not found")
        void shouldThrowCustomerNotFoundExceptionWhenCustomerNotFound() {
            // Given
            UUID orderId = testOrderEntity.getId();
            when(orderJpaRepository.findById(orderId)).thenReturn(Optional.of(testOrderEntity));
            when(customerJpaRepository.findById(testOrderEntity.getCustomerId())).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderRepositoryAdapter.findById(orderId))
                    .isInstanceOf(CustomerNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw ProductNotFoundException when product not found")
        void shouldThrowProductNotFoundExceptionWhenProductNotFound() {
            // Given
            UUID orderId = testOrderEntity.getId();
            when(orderJpaRepository.findById(orderId)).thenReturn(Optional.of(testOrderEntity));
            when(customerJpaRepository.findById(testOrderEntity.getCustomerId())).thenReturn(Optional.of(testCustomerEntity));
            when(customerMapper.toDomain(testCustomerEntity)).thenReturn(testCustomer);
            when(orderMapper.toDomain(testOrderEntity, testCustomer)).thenReturn(testOrder);
            when(productJpaRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> orderRepositoryAdapter.findById(orderId))
                    .isInstanceOf(ProductNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByCustomerId Tests")
    class FindByCustomerIdTests {

        @Test
        @DisplayName("Should return orders for customer")
        void shouldReturnOrdersForCustomer() {
            // Given
            UUID customerId = testCustomer.getId();
            when(orderJpaRepository.findByCustomerId(customerId)).thenReturn(List.of(testOrderEntity));
            when(customerJpaRepository.findById(testOrderEntity.getCustomerId())).thenReturn(Optional.of(testCustomerEntity));
            when(customerMapper.toDomain(testCustomerEntity)).thenReturn(testCustomer);
            when(orderMapper.toDomain(testOrderEntity, testCustomer)).thenReturn(testOrder);
            when(productJpaRepository.findById(any(UUID.class))).thenReturn(Optional.of(testProductEntity));
            when(productMapper.toDomain(testProductEntity)).thenReturn(testOrder.getItems().get(0).getProduct());
            when(orderItemMapper.toDomain(any(OrderItemEntity.class), any(Product.class))).thenReturn(testOrder.getItems().get(0));

            // When
            List<Order> result = orderRepositoryAdapter.findByCustomerId(customerId);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no orders found")
        void shouldReturnEmptyListWhenNoOrdersFound() {
            // Given
            UUID customerId = UUID.randomUUID();
            when(orderJpaRepository.findByCustomerId(customerId)).thenReturn(List.of());

            // When
            List<Order> result = orderRepositoryAdapter.findByCustomerId(customerId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatus Tests")
    class FindByStatusTests {

        @Test
        @DisplayName("Should return orders by status")
        void shouldReturnOrdersByStatus() {
            // Given
            when(orderJpaRepository.findByStatus(OrderEntity.OrderStatus.PENDING)).thenReturn(List.of(testOrderEntity));
            when(customerJpaRepository.findById(testOrderEntity.getCustomerId())).thenReturn(Optional.of(testCustomerEntity));
            when(customerMapper.toDomain(testCustomerEntity)).thenReturn(testCustomer);
            when(orderMapper.toDomain(testOrderEntity, testCustomer)).thenReturn(testOrder);
            when(productJpaRepository.findById(any(UUID.class))).thenReturn(Optional.of(testProductEntity));
            when(productMapper.toDomain(testProductEntity)).thenReturn(testOrder.getItems().get(0).getProduct());
            when(orderItemMapper.toDomain(any(OrderItemEntity.class), any(Product.class))).thenReturn(testOrder.getItems().get(0));

            // When
            List<Order> result = orderRepositoryAdapter.findByStatus(Order.OrderStatus.PENDING);

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no orders with status")
        void shouldReturnEmptyListWhenNoOrdersWithStatus() {
            // Given
            when(orderJpaRepository.findByStatus(OrderEntity.OrderStatus.COMPLETED)).thenReturn(List.of());

            // When
            List<Order> result = orderRepositoryAdapter.findByStatus(Order.OrderStatus.COMPLETED);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteById Tests")
    class DeleteByIdTests {

        @Test
        @DisplayName("Should delete order when exists")
        void shouldDeleteOrderWhenExists() {
            // Given
            UUID orderId = UUID.randomUUID();
            when(orderJpaRepository.existsById(orderId)).thenReturn(true);
            doNothing().when(orderJpaRepository).deleteById(orderId);

            // When
            boolean result = orderRepositoryAdapter.deleteById(orderId);

            // Then
            assertThat(result).isTrue();
            verify(orderJpaRepository).deleteById(orderId);
        }

        @Test
        @DisplayName("Should return false when order does not exist")
        void shouldReturnFalseWhenOrderDoesNotExist() {
            // Given
            UUID orderId = UUID.randomUUID();
            when(orderJpaRepository.existsById(orderId)).thenReturn(false);

            // When
            boolean result = orderRepositoryAdapter.deleteById(orderId);

            // Then
            assertThat(result).isFalse();
            verify(orderJpaRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("existsById Tests")
    class ExistsByIdTests {

        @Test
        @DisplayName("Should return true when exists")
        void shouldReturnTrueWhenExists() {
            // Given
            UUID orderId = UUID.randomUUID();
            when(orderJpaRepository.existsById(orderId)).thenReturn(true);

            // When
            boolean result = orderRepositoryAdapter.existsById(orderId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when does not exist")
        void shouldReturnFalseWhenDoesNotExist() {
            // Given
            UUID orderId = UUID.randomUUID();
            when(orderJpaRepository.existsById(orderId)).thenReturn(false);

            // When
            boolean result = orderRepositoryAdapter.existsById(orderId);

            // Then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("findAll Tests")
    class FindAllTests {

        @Test
        @DisplayName("Should return all orders")
        void shouldReturnAllOrders() {
            // Given
            when(orderJpaRepository.findAll()).thenReturn(List.of(testOrderEntity));
            when(customerJpaRepository.findById(testOrderEntity.getCustomerId())).thenReturn(Optional.of(testCustomerEntity));
            when(customerMapper.toDomain(testCustomerEntity)).thenReturn(testCustomer);
            when(orderMapper.toDomain(testOrderEntity, testCustomer)).thenReturn(testOrder);
            when(productJpaRepository.findById(any(UUID.class))).thenReturn(Optional.of(testProductEntity));
            when(productMapper.toDomain(testProductEntity)).thenReturn(testOrder.getItems().get(0).getProduct());
            when(orderItemMapper.toDomain(any(OrderItemEntity.class), any(Product.class))).thenReturn(testOrder.getItems().get(0));

            // When
            List<Order> result = orderRepositoryAdapter.findAll();

            // Then
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list when no orders")
        void shouldReturnEmptyListWhenNoOrders() {
            // Given
            when(orderJpaRepository.findAll()).thenReturn(List.of());

            // When
            List<Order> result = orderRepositoryAdapter.findAll();

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save order successfully")
        void shouldSaveOrderSuccessfully() {
            // Given
            when(orderMapper.toEntity(testOrder)).thenReturn(testOrderEntity);
            when(orderItemMapper.toEntity(any(OrderItem.class))).thenReturn(testOrderEntity.getItems().get(0));
            when(orderJpaRepository.save(any(OrderEntity.class))).thenReturn(testOrderEntity);
            when(customerJpaRepository.findById(testOrderEntity.getCustomerId())).thenReturn(Optional.of(testCustomerEntity));
            when(customerMapper.toDomain(testCustomerEntity)).thenReturn(testCustomer);
            when(orderMapper.toDomain(testOrderEntity, testCustomer)).thenReturn(testOrder);
            when(productJpaRepository.findById(any(UUID.class))).thenReturn(Optional.of(testProductEntity));
            when(productMapper.toDomain(testProductEntity)).thenReturn(testOrder.getItems().get(0).getProduct());
            when(orderItemMapper.toDomain(any(OrderItemEntity.class), any(Product.class))).thenReturn(testOrder.getItems().get(0));

            // When
            Order result = orderRepositoryAdapter.save(testOrder);

            // Then
            assertThat(result).isNotNull();
            verify(orderJpaRepository).save(any(OrderEntity.class));
        }
    }
}
