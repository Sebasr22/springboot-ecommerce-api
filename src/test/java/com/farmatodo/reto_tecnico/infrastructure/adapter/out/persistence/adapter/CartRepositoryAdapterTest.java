package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.adapter;

import com.farmatodo.reto_tecnico.domain.model.Cart;
import com.farmatodo.reto_tecnico.domain.model.CartItem;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CartEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CartItemEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.CartItemMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.CartMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.CartJpaRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CartRepositoryAdapter.
 * Tests both happy paths and sad paths (error scenarios).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CartRepositoryAdapter Unit Tests")
class CartRepositoryAdapterTest {

    @Mock
    private CartJpaRepository jpaRepository;

    @Mock
    private CartMapper cartMapper;

    @Mock
    private CartItemMapper cartItemMapper;

    @InjectMocks
    private CartRepositoryAdapter cartRepositoryAdapter;

    private Cart testCart;
    private CartEntity testCartEntity;
    private UUID customerId;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        Product product = Product.builder()
                .id(UUID.randomUUID())
                .name("Test Product")
                .price(Money.of(new BigDecimal("15000.00")))
                .stock(50)
                .build();

        CartItem cartItem = CartItem.builder()
                .id(UUID.randomUUID())
                .product(product)
                .quantity(2)
                .unitPrice(Money.of(new BigDecimal("15000.00")))
                .build();

        testCart = Cart.builder()
                .id(cartId)
                .customerId(customerId)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testCart.getItems().add(cartItem);

        ProductEntity productEntity = ProductEntity.builder()
                .id(product.getId())
                .name("Test Product")
                .price(new BigDecimal("15000.00"))
                .stock(50)
                .build();

        CartItemEntity cartItemEntity = CartItemEntity.builder()
                .id(cartItem.getId())
                .product(productEntity)
                .quantity(2)
                .unitPrice(new BigDecimal("15000.00"))
                .build();

        testCartEntity = CartEntity.builder()
                .id(cartId)
                .customerId(customerId)
                .items(new ArrayList<>())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        testCartEntity.getItems().add(cartItemEntity);
    }

    @Nested
    @DisplayName("save Tests")
    class SaveTests {

        @Test
        @DisplayName("Should save cart successfully")
        void shouldSaveCartSuccessfully() {
            // Given
            when(cartMapper.toEntity(testCart)).thenReturn(testCartEntity);
            when(cartItemMapper.toEntity(any(CartItem.class))).thenReturn(testCartEntity.getItems().get(0));
            when(jpaRepository.save(any(CartEntity.class))).thenReturn(testCartEntity);
            when(cartMapper.toDomain(testCartEntity)).thenReturn(testCart);

            // When
            Cart result = cartRepositoryAdapter.save(testCart);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testCart.getId());
            verify(jpaRepository).save(any(CartEntity.class));
        }

        @Test
        @DisplayName("Should save empty cart successfully")
        void shouldSaveEmptyCartSuccessfully() {
            // Given
            Cart emptyCart = Cart.builder()
                    .id(UUID.randomUUID())
                    .customerId(customerId)
                    .items(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .build();

            CartEntity emptyCartEntity = CartEntity.builder()
                    .id(emptyCart.getId())
                    .customerId(customerId)
                    .items(new ArrayList<>())
                    .createdAt(LocalDateTime.now())
                    .build();

            when(cartMapper.toEntity(emptyCart)).thenReturn(emptyCartEntity);
            when(jpaRepository.save(any(CartEntity.class))).thenReturn(emptyCartEntity);
            when(cartMapper.toDomain(emptyCartEntity)).thenReturn(emptyCart);

            // When
            Cart result = cartRepositoryAdapter.save(emptyCart);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCustomerId Tests")
    class FindByCustomerIdTests {

        @Test
        @DisplayName("Should find cart by customer ID")
        void shouldFindCartByCustomerId() {
            // Given
            when(jpaRepository.findByCustomerId(customerId)).thenReturn(Optional.of(testCartEntity));
            when(cartMapper.toDomain(testCartEntity)).thenReturn(testCart);

            // When
            Optional<Cart> result = cartRepositoryAdapter.findByCustomerId(customerId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getCustomerId()).isEqualTo(customerId);
        }

        @Test
        @DisplayName("Should return empty when cart not found for customer")
        void shouldReturnEmptyWhenCartNotFoundForCustomer() {
            // Given
            UUID nonExistentCustomerId = UUID.randomUUID();
            when(jpaRepository.findByCustomerId(nonExistentCustomerId)).thenReturn(Optional.empty());

            // When
            Optional<Cart> result = cartRepositoryAdapter.findByCustomerId(nonExistentCustomerId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById Tests")
    class FindByIdTests {

        @Test
        @DisplayName("Should find cart by ID")
        void shouldFindCartById() {
            // Given
            UUID cartId = testCartEntity.getId();
            when(jpaRepository.findById(cartId)).thenReturn(Optional.of(testCartEntity));
            when(cartMapper.toDomain(testCartEntity)).thenReturn(testCart);

            // When
            Optional<Cart> result = cartRepositoryAdapter.findById(cartId);

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(cartId);
        }

        @Test
        @DisplayName("Should return empty when cart not found by ID")
        void shouldReturnEmptyWhenCartNotFoundById() {
            // Given
            UUID nonExistentCartId = UUID.randomUUID();
            when(jpaRepository.findById(nonExistentCartId)).thenReturn(Optional.empty());

            // When
            Optional<Cart> result = cartRepositoryAdapter.findById(nonExistentCartId);

            // Then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("deleteByCustomerId Tests")
    class DeleteByCustomerIdTests {

        @Test
        @DisplayName("Should delete cart by customer ID")
        void shouldDeleteCartByCustomerId() {
            // Given
            doNothing().when(jpaRepository).deleteByCustomerId(customerId);

            // When
            cartRepositoryAdapter.deleteByCustomerId(customerId);

            // Then
            verify(jpaRepository).deleteByCustomerId(customerId);
        }
    }

    @Nested
    @DisplayName("existsByCustomerId Tests")
    class ExistsByCustomerIdTests {

        @Test
        @DisplayName("Should return true when cart exists for customer")
        void shouldReturnTrueWhenCartExistsForCustomer() {
            // Given
            when(jpaRepository.existsByCustomerId(customerId)).thenReturn(true);

            // When
            boolean result = cartRepositoryAdapter.existsByCustomerId(customerId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should return false when cart does not exist for customer")
        void shouldReturnFalseWhenCartDoesNotExistForCustomer() {
            // Given
            UUID nonExistentCustomerId = UUID.randomUUID();
            when(jpaRepository.existsByCustomerId(nonExistentCustomerId)).thenReturn(false);

            // When
            boolean result = cartRepositoryAdapter.existsByCustomerId(nonExistentCustomerId);

            // Then
            assertThat(result).isFalse();
        }
    }
}
