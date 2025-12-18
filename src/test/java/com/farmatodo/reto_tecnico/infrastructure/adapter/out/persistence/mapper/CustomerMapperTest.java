package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CustomerMapper.
 * Tests MapStruct-generated mapping between Customer domain model and CustomerEntity.
 */
@DisplayName("CustomerMapper Unit Tests")
class CustomerMapperTest {

    private CustomerMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new CustomerMapperImpl();
    }

    @Test
    @DisplayName("Should convert domain Customer to CustomerEntity")
    void shouldConvertDomainToEntity() {
        // Given
        UUID customerId = UUID.randomUUID();
        Customer customer = Customer.builder()
                .id(customerId)
                .name("Juan Pérez")
                .email(new Email("juan@example.com"))
                .phone(new Phone("3001234567"))
                .address("Calle 123, Bogotá")
                .build();

        // When
        CustomerEntity entity = mapper.toEntity(customer);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(customerId);
        assertThat(entity.getName()).isEqualTo("Juan Pérez");
        assertThat(entity.getEmail()).isEqualTo("juan@example.com");
        assertThat(entity.getPhone()).isEqualTo("3001234567");
        assertThat(entity.getAddress()).isEqualTo("Calle 123, Bogotá");
    }

    @Test
    @DisplayName("Should convert CustomerEntity to domain Customer")
    void shouldConvertEntityToDomain() {
        // Given
        UUID customerId = UUID.randomUUID();
        CustomerEntity entity = CustomerEntity.builder()
                .id(customerId)
                .name("María García")
                .email("maria@example.com")
                .phone("3109876543")
                .address("Carrera 45, Medellín")
                .build();

        // When
        Customer customer = mapper.toDomain(entity);

        // Then
        assertThat(customer).isNotNull();
        assertThat(customer.getId()).isEqualTo(customerId);
        assertThat(customer.getName()).isEqualTo("María García");
        assertThat(customer.getEmail().value()).isEqualTo("maria@example.com");
        assertThat(customer.getPhone().value()).isEqualTo("3109876543");
        assertThat(customer.getAddress()).isEqualTo("Carrera 45, Medellín");
    }

    @Test
    @DisplayName("Should handle null input for toEntity")
    void shouldHandleNullInputForToEntity() {
        // When
        CustomerEntity entity = mapper.toEntity(null);

        // Then
        assertThat(entity).isNull();
    }

    @Test
    @DisplayName("Should handle null input for toDomain")
    void shouldHandleNullInputForToDomain() {
        // When
        Customer customer = mapper.toDomain(null);

        // Then
        assertThat(customer).isNull();
    }

    @Test
    @DisplayName("Should roundtrip conversion preserve data")
    void shouldRoundtripConversionPreserveData() {
        // Given
        UUID customerId = UUID.randomUUID();
        Customer original = Customer.builder()
                .id(customerId)
                .name("Test Customer")
                .email(new Email("test@example.com"))
                .phone(new Phone("3151234567"))
                .address("Test Address 123")
                .build();

        // When
        CustomerEntity entity = mapper.toEntity(original);
        Customer result = mapper.toDomain(entity);

        // Then
        assertThat(result.getId()).isEqualTo(original.getId());
        assertThat(result.getName()).isEqualTo(original.getName());
        assertThat(result.getEmail().value()).isEqualTo(original.getEmail().value());
        assertThat(result.getPhone().value()).isEqualTo(original.getPhone().value());
        assertThat(result.getAddress()).isEqualTo(original.getAddress());
    }
}
