package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.ProductEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ProductMapper.
 * Tests MapStruct-generated mapping between Product domain model and ProductEntity.
 */
@DisplayName("ProductMapper Unit Tests")
class ProductMapperTest {

    private ProductMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProductMapperImpl();
    }

    @Test
    @DisplayName("Should convert domain Product to ProductEntity")
    void shouldConvertDomainToEntity() {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = Product.builder()
                .id(productId)
                .name("Acetaminofén 500mg")
                .description("Analgésico y antipirético")
                .price(new Money(new BigDecimal("15000.00")))
                .stock(100)
                .build();

        // When
        ProductEntity entity = mapper.toEntity(product);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(productId);
        assertThat(entity.getName()).isEqualTo("Acetaminofén 500mg");
        assertThat(entity.getDescription()).isEqualTo("Analgésico y antipirético");
        assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(entity.getStock()).isEqualTo(100);
    }

    @Test
    @DisplayName("Should convert ProductEntity to domain Product")
    void shouldConvertEntityToDomain() {
        // Given
        UUID productId = UUID.randomUUID();
        ProductEntity entity = ProductEntity.builder()
                .id(productId)
                .name("Ibuprofeno 400mg")
                .description("Antiinflamatorio")
                .price(new BigDecimal("20000.00"))
                .stock(50)
                .version(1L)
                .build();

        // When
        Product product = mapper.toDomain(entity);

        // Then
        assertThat(product).isNotNull();
        assertThat(product.getId()).isEqualTo(productId);
        assertThat(product.getName()).isEqualTo("Ibuprofeno 400mg");
        assertThat(product.getDescription()).isEqualTo("Antiinflamatorio");
        assertThat(product.getPrice().amount()).isEqualByComparingTo(new BigDecimal("20000.00"));
        assertThat(product.getStock()).isEqualTo(50);
    }

    @Test
    @DisplayName("Should convert list of entities to list of domain objects")
    void shouldConvertEntityListToDomainList() {
        // Given
        ProductEntity entity1 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Product 1")
                .description("Description 1")
                .price(new BigDecimal("10000.00"))
                .stock(10)
                .build();

        ProductEntity entity2 = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Product 2")
                .description("Description 2")
                .price(new BigDecimal("25000.00"))
                .stock(20)
                .build();

        List<ProductEntity> entities = List.of(entity1, entity2);

        // When
        List<Product> products = mapper.toDomainList(entities);

        // Then
        assertThat(products).hasSize(2);
        assertThat(products.get(0).getName()).isEqualTo("Product 1");
        assertThat(products.get(1).getName()).isEqualTo("Product 2");
    }

    @Test
    @DisplayName("Should handle null input for toEntity")
    void shouldHandleNullInputForToEntity() {
        // When
        ProductEntity entity = mapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should handle null input for toDomain")
    void shouldHandleNullInputForToDomain() {
        // When
        Product product = mapper.toDomain(null);

        // Then
        assertThat(product).isNull();
    }

    @Test
    @DisplayName("Should handle empty list for toDomainList")
    void shouldHandleEmptyListForToDomainList() {
        // When
        List<Product> products = mapper.toDomainList(List.of());

        // Then
        assertThat(products).isEmpty();
    }

    @Test
    @DisplayName("Should handle null list for toDomainList")
    void shouldHandleNullListForToDomainList() {
        // When
        List<Product> products = mapper.toDomainList(null);

        // Then
        assertThat(products).isNull();
    }

    @Test
    @DisplayName("Should roundtrip conversion preserve data")
    void shouldRoundtripConversionPreserveData() {
        // Given
        UUID productId = UUID.randomUUID();
        Product original = Product.builder()
                .id(productId)
                .name("Test Product")
                .description("Test Description")
                .price(new Money(new BigDecimal("12345.67")))
                .stock(77)
                .build();

        // When
        ProductEntity entity = mapper.toEntity(original);
        Product result = mapper.toDomain(entity);

        // Then
        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getName()).isEqualTo(original.getName());
        assertThat(result.getDescription()).isEqualTo(original.getDescription());
        assertThat(result.getPrice().amount()).isEqualByComparingTo(original.getPrice().amount());
        assertThat(result.getStock()).isEqualTo(original.getStock());
    }
}
