package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.adapter;

import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.CustomerMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.CustomerJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerRepositoryAdapter.
 * Tests the adapter layer with mocked JPA repository and mapper.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerRepositoryAdapter Unit Tests")
class CustomerRepositoryAdapterTest {

    @Mock
    private CustomerJpaRepository jpaRepository;

    @Mock
    private CustomerMapper mapper;

    @InjectMocks
    private CustomerRepositoryAdapter adapter;

    private UUID customerId;
    private Customer customer;
    private CustomerEntity customerEntity;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();

        customer = Customer.builder()
                .id(customerId)
                .name("Juan Pérez")
                .email(new Email("juan@example.com"))
                .phone(new Phone("3001234567"))
                .address("Calle 123, Bogotá")
                .build();

        customerEntity = CustomerEntity.builder()
                .id(customerId)
                .name("Juan Pérez")
                .email("juan@example.com")
                .phone("3001234567")
                .address("Calle 123, Bogotá")
                .build();
    }

    @Test
    @DisplayName("Should save customer and return mapped domain object")
    void shouldSaveCustomer() {
        // Given
        when(mapper.toEntity(customer)).thenReturn(customerEntity);
        when(jpaRepository.save(customerEntity)).thenReturn(customerEntity);
        when(mapper.toDomain(customerEntity)).thenReturn(customer);

        // When
        Customer result = adapter.save(customer);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(customerId);
        verify(mapper).toEntity(customer);
        verify(jpaRepository).save(customerEntity);
        verify(mapper).toDomain(customerEntity);
    }

    @Test
    @DisplayName("Should find customer by ID")
    void shouldFindCustomerById() {
        // Given
        when(jpaRepository.findById(customerId)).thenReturn(Optional.of(customerEntity));
        when(mapper.toDomain(customerEntity)).thenReturn(customer);

        // When
        Optional<Customer> result = adapter.findById(customerId);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(customerId);
        verify(jpaRepository).findById(customerId);
        verify(mapper).toDomain(customerEntity);
    }

    @Test
    @DisplayName("Should return empty when customer not found by ID")
    void shouldReturnEmptyWhenCustomerNotFoundById() {
        // Given
        when(jpaRepository.findById(customerId)).thenReturn(Optional.empty());

        // When
        Optional<Customer> result = adapter.findById(customerId);

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findById(customerId);
        verify(mapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("Should find customer by email")
    void shouldFindCustomerByEmail() {
        // Given
        Email email = new Email("juan@example.com");
        when(jpaRepository.findByEmail("juan@example.com")).thenReturn(Optional.of(customerEntity));
        when(mapper.toDomain(customerEntity)).thenReturn(customer);

        // When
        Optional<Customer> result = adapter.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail().value()).isEqualTo("juan@example.com");
        verify(jpaRepository).findByEmail("juan@example.com");
    }

    @Test
    @DisplayName("Should return empty when customer not found by email")
    void shouldReturnEmptyWhenCustomerNotFoundByEmail() {
        // Given
        Email email = new Email("notfound@example.com");
        when(jpaRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // When
        Optional<Customer> result = adapter.findByEmail(email);

        // Then
        assertThat(result).isEmpty();
        verify(jpaRepository).findByEmail("notfound@example.com");
    }

    @Test
    @DisplayName("Should check if customer exists by email")
    void shouldCheckIfCustomerExistsByEmail() {
        // Given
        Email email = new Email("exists@example.com");
        when(jpaRepository.existsByEmail("exists@example.com")).thenReturn(true);

        // When
        boolean result = adapter.existsByEmail(email);

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByEmail("exists@example.com");
    }

    @Test
    @DisplayName("Should return false when customer does not exist by email")
    void shouldReturnFalseWhenCustomerDoesNotExistByEmail() {
        // Given
        Email email = new Email("notexists@example.com");
        when(jpaRepository.existsByEmail("notexists@example.com")).thenReturn(false);

        // When
        boolean result = adapter.existsByEmail(email);

        // Then
        assertThat(result).isFalse();
        verify(jpaRepository).existsByEmail("notexists@example.com");
    }

    @Test
    @DisplayName("Should check if customer exists by phone")
    void shouldCheckIfCustomerExistsByPhone() {
        // Given
        Phone phone = new Phone("3001234567");
        when(jpaRepository.existsByPhone("3001234567")).thenReturn(true);

        // When
        boolean result = adapter.existsByPhone(phone);

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsByPhone("3001234567");
    }

    @Test
    @DisplayName("Should find all customers")
    void shouldFindAllCustomers() {
        // Given
        CustomerEntity entity2 = CustomerEntity.builder()
                .id(UUID.randomUUID())
                .name("María García")
                .email("maria@example.com")
                .phone("3109876543")
                .address("Carrera 45")
                .build();

        Customer customer2 = Customer.builder()
                .id(entity2.getId())
                .name("María García")
                .email(new Email("maria@example.com"))
                .phone(new Phone("3109876543"))
                .address("Carrera 45")
                .build();

        when(jpaRepository.findAll()).thenReturn(List.of(customerEntity, entity2));
        when(mapper.toDomain(customerEntity)).thenReturn(customer);
        when(mapper.toDomain(entity2)).thenReturn(customer2);

        // When
        List<Customer> result = adapter.findAll();

        // Then
        assertThat(result).hasSize(2);
        verify(jpaRepository).findAll();
    }

    @Test
    @DisplayName("Should delete customer by ID when exists")
    void shouldDeleteCustomerByIdWhenExists() {
        // Given
        when(jpaRepository.existsById(customerId)).thenReturn(true);

        // When
        boolean result = adapter.deleteById(customerId);

        // Then
        assertThat(result).isTrue();
        verify(jpaRepository).existsById(customerId);
        verify(jpaRepository).deleteById(customerId);
    }

    @Test
    @DisplayName("Should return false when deleting non-existent customer")
    void shouldReturnFalseWhenDeletingNonExistentCustomer() {
        // Given
        when(jpaRepository.existsById(customerId)).thenReturn(false);

        // When
        boolean result = adapter.deleteById(customerId);

        // Then
        assertThat(result).isFalse();
        verify(jpaRepository).existsById(customerId);
        verify(jpaRepository, never()).deleteById(any());
    }
}
