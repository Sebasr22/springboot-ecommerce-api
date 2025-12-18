package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * POJO tests for CustomerEntity.
 * Tests builder, getters, setters, equals, hashCode, and toString.
 */
@DisplayName("CustomerEntity POJO Tests")
class CustomerEntityTest {

    @Test
    @DisplayName("Should create entity using builder")
    void shouldCreateEntityUsingBuilder() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        CustomerEntity entity = CustomerEntity.builder()
                .id(id)
                .name("Juan Pérez")
                .email("juan@example.com")
                .phone("3001234567")
                .address("Calle 123, Bogotá")
                .build();

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("Juan Pérez");
        assertThat(entity.getEmail()).isEqualTo("juan@example.com");
        assertThat(entity.getPhone()).isEqualTo("3001234567");
        assertThat(entity.getAddress()).isEqualTo("Calle 123, Bogotá");
    }

    @Test
    @DisplayName("Should create entity using no-args constructor")
    void shouldCreateEntityUsingNoArgsConstructor() {
        // When
        CustomerEntity entity = new CustomerEntity();

        // Then
        assertThat(entity.getId()).isNull();
        assertThat(entity.getName()).isNull();
    }

    @Test
    @DisplayName("Should create entity using all-args constructor")
    void shouldCreateEntityUsingAllArgsConstructor() {
        // Given
        UUID id = UUID.randomUUID();

        // When
        CustomerEntity entity = new CustomerEntity(id, "Test Name", "test@email.com", "1234567890", "Test Address");

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("Test Name");
    }

    @Test
    @DisplayName("Should set and get all properties")
    void shouldSetAndGetAllProperties() {
        // Given
        CustomerEntity entity = new CustomerEntity();
        UUID id = UUID.randomUUID();

        // When
        entity.setId(id);
        entity.setName("New Name");
        entity.setEmail("new@email.com");
        entity.setPhone("9876543210");
        entity.setAddress("New Address");

        // Then
        assertThat(entity.getId()).isEqualTo(id);
        assertThat(entity.getName()).isEqualTo("New Name");
        assertThat(entity.getEmail()).isEqualTo("new@email.com");
        assertThat(entity.getPhone()).isEqualTo("9876543210");
        assertThat(entity.getAddress()).isEqualTo("New Address");
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        CustomerEntity entity1 = CustomerEntity.builder()
                .id(id)
                .name("Test")
                .email("test@test.com")
                .phone("1234567890")
                .address("Address")
                .build();

        CustomerEntity entity2 = CustomerEntity.builder()
                .id(id)
                .name("Test")
                .email("test@test.com")
                .phone("1234567890")
                .address("Address")
                .build();

        // Then
        assertThat(entity1).isEqualTo(entity2);
        assertThat(entity1.hashCode()).isEqualTo(entity2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void shouldImplementToString() {
        // Given
        CustomerEntity entity = CustomerEntity.builder()
                .name("Test Name")
                .email("test@test.com")
                .build();

        // When
        String result = entity.toString();

        // Then
        assertThat(result).contains("Test Name");
        assertThat(result).contains("test@test.com");
    }
}
