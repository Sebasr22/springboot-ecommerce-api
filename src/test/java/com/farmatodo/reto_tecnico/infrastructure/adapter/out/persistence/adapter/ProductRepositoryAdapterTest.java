package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.adapter;

import com.farmatodo.reto_tecnico.application.config.FarmatodoProperties;
import com.farmatodo.reto_tecnico.domain.exception.InsufficientStockException;
import com.farmatodo.reto_tecnico.domain.exception.ProductNotFoundException;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.ProductEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.ProductMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductRepositoryAdapter.
 * Tests the adapter layer with mocked JPA repository and mapper.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductRepositoryAdapter Unit Tests")
class ProductRepositoryAdapterTest {

    @Mock
    private ProductJpaRepository jpaRepository;

    @Mock
    private ProductMapper mapper;

    @Mock
    private FarmatodoProperties properties;

    @InjectMocks
    private ProductRepositoryAdapter adapter;

    private UUID productId;
    private Product product;
    private ProductEntity productEntity;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        product = Product.builder()
                .id(productId)
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(new Money(new BigDecimal("15000.00")))
                .stock(100)
                .build();

        productEntity = ProductEntity.builder()
                .id(productId)
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(new BigDecimal("15000.00"))
                .stock(100)
                .version(1L)
                .build();

        // Configure FarmatodoProperties mock (lenient because not all tests use it)
        FarmatodoProperties.Product productConfig = new FarmatodoProperties.Product();
        productConfig.setMinStockThreshold(1);
        lenient().when(properties.getProduct()).thenReturn(productConfig);
    }

    @Test
    @DisplayName("Should save product and return mapped domain object")
    void shouldSaveProduct() {
        // Given
        when(mapper.toEntity(product)).thenReturn(productEntity);
        when(jpaRepository.save(productEntity)).thenReturn(productEntity);
        when(mapper.toDomain(productEntity)).thenReturn(product);

        // When
        Product result = adapter.save(product);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(productId);
        verify(mapper).toEntity(product);
        verify(jpaRepository).save(productEntity);
        verify(mapper).toDomain(productEntity);
    }

    @Test
    @DisplayName("Should find product by ID")
    void shouldFindProductById() {
        // Given
        when(jpaRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(mapper.toDomain(productEntity)).thenReturn(product);

        // When
        Optional<Product> result = adapter.findById(productId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(productId);
        verify(jpaRepository).findById(productId);
    }

    @Test
    @DisplayName("Should return empty when product not found by ID")
    void shouldReturnEmptyWhenProductNotFoundById() {
        // Given
        when(jpaRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        Optional<Product> result = adapter.findById(productId);

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findById(productId);
    }

    @Test
    @DisplayName("Should find products by name containing query")
    void shouldFindProductsByNameContaining() {
        // Given
        String query = "Acetaminofén";
        int minStock = 1;
        when(jpaRepository.findByNameContainingIgnoreCase(query, minStock)).thenReturn(List.of(productEntity));
        when(mapper.toDomainList(List.of(productEntity))).thenReturn(List.of(product));

        // When
        List<Product> result = adapter.findByNameContaining(query);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Acetaminofén 500mg");
        verify(jpaRepository).findByNameContainingIgnoreCase(query, minStock);
    }

    @Test
    @DisplayName("Should find all products in stock")
    void shouldFindAllProductsInStock() {
        // Given
        int minStock = 1;
        when(jpaRepository.findAllInStock(minStock)).thenReturn(List.of(productEntity));
        when(mapper.toDomainList(List.of(productEntity))).thenReturn(List.of(product));

        // When
        List<Product> result = adapter.findAllInStock();

        // Then
        assertThat(result).hasSize(1);
        verify(jpaRepository).findAllInStock(minStock);
    }

    @Test
    @DisplayName("Should find all products")
    void shouldFindAllProducts() {
        // Given
        when(jpaRepository.findAll()).thenReturn(List.of(productEntity));
        when(mapper.toDomainList(List.of(productEntity))).thenReturn(List.of(product));

        // When
        List<Product> result = adapter.findAll();

        // Then
        assertThat(result).hasSize(1);
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("Should find products with stock less than threshold")
    void shouldFindProductsWithStockLessThanThreshold() {
        // Given
        ProductEntity lowStockEntity = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Low Stock Product")
                .price(new BigDecimal("5000.00"))
                .stock(3)
                .build();

        Product lowStockProduct = Product.builder()
                .id(lowStockEntity.getId())
                .name("Low Stock Product")
                .price(new Money(new BigDecimal("5000.00")))
                .stock(3)
                .build();

        when(jpaRepository.findAll()).thenReturn(List.of(lowStockEntity, productEntity));
        when(mapper.toDomain(lowStockEntity)).thenReturn(lowStockProduct);

        // When
        List<Product> result = adapter.findByStockLessThan(10);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Low Stock Product");
    }

    @Test
    @DisplayName("Should reduce stock atomically")
    void shouldReduceStockAtomically() {
        // Given: Product has 100 stock, reduce by 20
        ProductEntity updatedEntity = ProductEntity.builder()
                .id(productId)
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(new BigDecimal("15000.00"))
                .stock(80)
                .version(2L)
                .build();

        Product updatedProduct = Product.builder()
                .id(productId)
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(new Money(new BigDecimal("15000.00")))
                .stock(80)
                .build();

        when(jpaRepository.findById(productId)).thenReturn(Optional.of(productEntity), Optional.of(updatedEntity));
        when(jpaRepository.reduceStockAtomic(productId, 20)).thenReturn(1);
        when(mapper.toDomain(updatedEntity)).thenReturn(updatedProduct);

        // When
        Product result = adapter.updateStock(productId, 80);

        // Then
        assertThat(result.getStock()).isEqualTo(80);
        verify(jpaRepository).reduceStockAtomic(productId, 20);
    }

    @Test
    @DisplayName("Should increase stock atomically")
    void shouldIncreaseStockAtomically() {
        // Given: Product has 100 stock, increase to 150
        ProductEntity updatedEntity = ProductEntity.builder()
                .id(productId)
                .name("Acetaminofén 500mg")
                .price(new BigDecimal("15000.00"))
                .stock(150)
                .version(2L)
                .build();

        Product updatedProduct = Product.builder()
                .id(productId)
                .name("Acetaminofén 500mg")
                .price(new Money(new BigDecimal("15000.00")))
                .stock(150)
                .build();

        when(jpaRepository.findById(productId)).thenReturn(Optional.of(productEntity), Optional.of(updatedEntity));
        when(jpaRepository.increaseStockAtomic(productId, 50)).thenReturn(1);
        when(mapper.toDomain(updatedEntity)).thenReturn(updatedProduct);

        // When
        Product result = adapter.updateStock(productId, 150);

        // Then
        assertThat(result.getStock()).isEqualTo(150);
        verify(jpaRepository).increaseStockAtomic(productId, 50);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when updating stock for non-existent product")
    void shouldThrowProductNotFoundWhenUpdatingStockForNonExistentProduct() {
        // Given
        when(jpaRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> adapter.updateStock(productId, 50))
                .isInstanceOf(ProductNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when atomic reduce fails")
    void shouldThrowInsufficientStockWhenAtomicReduceFails() {
        // Given: Try to reduce more stock than available
        when(jpaRepository.findById(productId)).thenReturn(Optional.of(productEntity));
        when(jpaRepository.reduceStockAtomic(productId, 100)).thenReturn(0); // Failed

        // When & Then
        assertThatThrownBy(() -> adapter.updateStock(productId, 0))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    @DisplayName("Should delete product by ID when exists")
    void shouldDeleteProductByIdWhenExists() {
        // Given
        when(jpaRepository.existsById(productId)).thenReturn(true);

        // When
        boolean result = adapter.deleteById(productId);

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).deleteById(productId);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent product")
    void shouldReturnFalseWhenDeletingNonExistentProduct() {
        // Given
        when(jpaRepository.existsById(productId)).thenReturn(false);

        // When
        boolean result = adapter.deleteById(productId);

        // Then
        assertThat(result).isFalse();
        verify(jpaRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Should check if product exists by ID")
    void shouldCheckIfProductExistsById() {
        // Given
        when(jpaRepository.existsById(productId)).thenReturn(true);

        // When
        boolean result = adapter.existsById(productId);

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsById(productId);
    }
}
