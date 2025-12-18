package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * POJO tests for ProductEntity.
 * Tests builder, getters, setters, equals, hashCode, and toString.
 */
@DisplayName("ProductEntity POJO Tests")
class ProductEntityTest {

    @Test
    @DisplayName("Should create entity using builder")
    void shouldCreateEntityUsingBuilder() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        ProductEntity entity = ProductEntity.builder()
                .id(id)
                .name("Acetaminofén 500mg")
                .description("Analgésico")
                .price(new BigDecimal("15000.00"))
                .stock(100)
                .version(1L)
                .build();

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("Acetaminofén 500mg");
        assertThat(entity.getDescription()).isEqualTo("Analgésico");
        assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(entity.getStock()).isEqualTo(100);
        assertThat(entity.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should create entity using no-args constructor")
    void shouldCreateEntityUsingNoArgsConstructor() {
        // When
        ProductEntity entity = new ProductEntity();

        // Then
        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isNull();
        assertThat(entity.getStock()).isNull();
    }

    @Test
    @DisplayName("Should create entity using all-args constructor")
    void shouldCreateEntityUsingAllArgsConstructor() {
        // Given
        UUID id = UUID.randomUUID();
        BigDecimal price = new BigDecimal("10000.00");

        // When
        ProductEntity entity = new ProductEntity(id, "Test Product", "Description", price, 50, 1L);

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("Test Product");
        assertThat(entity.getDescription()).isEqualTo("Description");
        assertThat(entity.getPrice()).isEqualByComparingTo(price);
        assertThat(entity.getStock()).isEqualTo(50);
        assertThat(entity.getVersion()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        // Given
        ProductEntity entity = new ProductEntity();
        UUID id = UUID.randomUUID();
        BigDecimal price = new BigDecimal("25000.00");

        // When
        entity.setId(id);
        entity.setName("New Product");
        entity.setDescription("New Description");
        entity.setPrice(price);
        entity.setStock(75);
        entity.setVersion(2L);

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("New Product");
        assertThat(entity.getDescription()).isEqualTo("New Description");
        assertThat(entity.getPrice()).isEqualByComparingTo(price);
        assertThat(entity.getStock()).isEqualTo(75);
        assertThat(entity.getVersion()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        BigDecimal price = new BigDecimal("15000.00");

        ProductEntity entity1 = ProductEntity.builder()
                .id(id)
                .name("Test")
                .description("Desc")
                .price(price)
                .stock(10)
                .version(1L)
                .build();

        ProductEntity entity2 = ProductEntity.builder()
                .id(id)
                .name("Test")
                .description("Desc")
                .price(price)
                .stock(10)
                .version(1L)
                .build();

        // Then
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void shouldImplementToString() {
        // Given
        ProductEntity entity = ProductEntity.builder()
                .name("Test Product")
                .price(new BigDecimal("10000.00"))
                .stock(50)
                .build();

        // When
        String result = entity.toString();

        // Then
        assertThat(result).contains("Test Product");
        assertThat(result).contains("10000.00");
        assertThat(result).contains("50");
    }

    @Test
    @DisplayName("Should handle null description")
    void shouldHandleNullDescription() {
        // Given
        ProductEntity entity = ProductEntity.builder()
                .id(UUID.randomUUID())
                .name("Product Without Description")
                .price(new BigDecimal("5000.00"))
                .stock(10)
                .build();

        // Then
        assertThat(entity.getDescription()).isNull();
    }
}
