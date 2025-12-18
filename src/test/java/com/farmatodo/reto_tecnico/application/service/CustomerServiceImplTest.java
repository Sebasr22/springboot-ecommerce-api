package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.exception.CustomerAlreadyExistsException;
import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.domain.port.out.CustomerRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CustomerServiceImpl.
 * Tests customer registration with email uniqueness validation.
 *
 * Uses pure unit testing with Mockito (NO Spring context).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerServiceImpl Unit Tests")
class CustomerServiceImplTest {

    @Mock
    private CustomerRepositoryPort customerRepository;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private Customer testCustomer;
    private Email testEmail;

    @BeforeEach
    void setUp() {
        testEmail = new Email("juan.perez@test.com");

        testCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Juan Pérez")
                .email(testEmail)
                .phone(new Phone("3001234567"))
                .address("Calle 123 #45-67, Bogotá")
                .build();
    }

    @Test
    @DisplayName("Should throw CustomerAlreadyExistsException when email already exists")
    void shouldThrowCustomerAlreadyExistsExceptionWhenEmailExists() {
        // Given: Email already exists in database
        when(customerRepository.existsByEmail(testEmail)).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> customerService.register(testCustomer))
                .isInstanceOf(CustomerAlreadyExistsException.class)
                .hasMessageContaining("juan.perez@test.com")
                .hasMessageContaining("already exists");

        // Verify save was never called
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should register customer successfully when email is unique")
    void shouldRegisterCustomerSuccessfullyWhenEmailIsUnique() {
        // Given: Email does not exist in database
        when(customerRepository.existsByEmail(testEmail)).thenReturn(false);

        // Mock save operation
        Customer savedCustomer = Customer.builder()
                .id(testCustomer.getId())
                .name(testCustomer.getName())
                .email(testCustomer.getEmail())
                .phone(testCustomer.getPhone())
                .address(testCustomer.getAddress())
                .build();
        when(customerRepository.save(testCustomer)).thenReturn(savedCustomer);

        // Act
        Customer result = customerService.register(testCustomer);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testCustomer.getId());
        assertThat(result.getName()).isEqualTo("Juan Pérez");
        assertThat(result.getEmail().value()).isEqualTo("juan.perez@test.com");
        assertThat(result.getPhone().value()).isEqualTo("3001234567");
        assertThat(result.getAddress()).isEqualTo("Calle 123 #45-67, Bogotá");

        // Verify email uniqueness check was performed
        verify(customerRepository, times(1)).existsByEmail(testEmail);

        // Verify save was called once
        verify(customerRepository, times(1)).save(testCustomer);
    }

    @Test
    @DisplayName("Should validate email uniqueness before saving")
    void shouldValidateEmailUniquenessBeforeSaving() {
        // Given: Email already exists
        when(customerRepository.existsByEmail(testEmail)).thenReturn(true);

        // Act & Assert: Exception thrown before save
        assertThatThrownBy(() -> customerService.register(testCustomer))
                .isInstanceOf(CustomerAlreadyExistsException.class);

        // Verify email check was performed BEFORE save attempt
        verify(customerRepository, times(1)).existsByEmail(testEmail);
        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("Should register multiple customers with different emails")
    void shouldRegisterMultipleCustomersWithDifferentEmails() {
        // Given: Two customers with different emails
        Customer customer1 = Customer.builder()
                .id(UUID.randomUUID())
                .name("Customer 1")
                .email(new Email("customer1@test.com"))
                .phone(new Phone("3001111111"))
                .address("Address 1")
                .build();

        Customer customer2 = Customer.builder()
                .id(UUID.randomUUID())
                .name("Customer 2")
                .email(new Email("customer2@test.com"))
                .phone(new Phone("3002222222"))
                .address("Address 2")
                .build();

        // Mock both emails are unique
        when(customerRepository.existsByEmail(customer1.getEmail())).thenReturn(false);
        when(customerRepository.existsByEmail(customer2.getEmail())).thenReturn(false);

        // Mock save operations
        when(customerRepository.save(customer1)).thenReturn(customer1);
        when(customerRepository.save(customer2)).thenReturn(customer2);

        // Act
        Customer result1 = customerService.register(customer1);
        Customer result2 = customerService.register(customer2);

        // Assert
        assertThat(result1).isNotNull();
        assertThat(result2).isNotNull();
        assertThat(result1.getEmail().value()).isEqualTo("customer1@test.com");
        assertThat(result2.getEmail().value()).isEqualTo("customer2@test.com");

        // Verify both customers were saved
        verify(customerRepository, times(1)).save(customer1);
        verify(customerRepository, times(1)).save(customer2);
    }

    @Test
    @DisplayName("Should handle case-sensitive email validation")
    void shouldHandleCaseSensitiveEmailValidation() {
        // Given: Email with different case
        Email upperCaseEmail = new Email("JUAN.PEREZ@TEST.COM");
        Customer upperCaseCustomer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Juan Pérez")
                .email(upperCaseEmail)
                .phone(new Phone("3001234567"))
                .address("Calle 123 #45-67, Bogotá")
                .build();

        // Mock repository behavior
        when(customerRepository.existsByEmail(upperCaseEmail)).thenReturn(false);
        when(customerRepository.save(upperCaseCustomer)).thenReturn(upperCaseCustomer);

        // Act
        Customer result = customerService.register(upperCaseCustomer);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail().value()).isEqualTo("juan.perez@test.com"); // Email normalized to lowercase

        // Verify save was called
        verify(customerRepository, times(1)).save(upperCaseCustomer);
    }
}
