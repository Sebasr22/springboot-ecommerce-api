package com.farmatodo.reto_tecnico.domain.model;

import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for Customer domain model.
 * Tests builder, factory methods, and domain behavior.
 */
@DisplayName("Customer Domain Model Tests")
class CustomerTest {

    @Test
    @DisplayName("Should create customer using builder")
    void shouldCreateCustomerUsingBuilder() {
        // Given
        UUID id = UUID.randomUUID();
        Email email = new Email("test@example.com");
        Phone phone = new Phone("3001234567");

        // When
        Customer customer = Customer.builder()
                .id(id)
                .name("Juan Pérez")
                .email(email)
                .phone(phone)
                .address("Calle 123 #45-67")
                .build();

        // Then
        assertThat(customer.getId()).isEqualTo(id);
        assertThat(customer.getName()).isEqualTo("Juan Pérez");
        assertThat(customer.getEmail()).isEqualTo(email);
        assertThat(customer.getPhone()).isEqualTo(phone);
        assertThat(customer.getAddress()).isEqualTo("Calle 123 #45-67");
    }

    @Test
    @DisplayName("Should create customer using factory method")
    void shouldCreateCustomerUsingFactoryMethod() {
        // Given
        Email email = new Email("factory@example.com");
        Phone phone = new Phone("3109876543");

        // When
        Customer customer = Customer.create("María García", email, phone, "Carrera 50 #10-20");

        // Then
        assertThat(customer.getId()).isNotNull();
        assertThat(customer.getName()).isEqualTo("María García");
        assertThat(customer.getEmail()).isEqualTo(email);
        assertThat(customer.getPhone()).isEqualTo(phone);
        assertThat(customer.getAddress()).isEqualTo("Carrera 50 #10-20");
    }

    @Test
    @DisplayName("Should update contact info")
    void shouldUpdateContactInfo() {
        // Given
        Customer customer = Customer.builder()
                .id(UUID.randomUUID())
                .name("Test Customer")
                .email(new Email("old@example.com"))
                .phone(new Phone("3001111111"))
                .address("Old Address")
                .build();

        Email newEmail = new Email("new@example.com");
        Phone newPhone = new Phone("3009999999");
        String newAddress = "New Address 123";

        // When
        customer.updateContactInfo(newEmail, newPhone, newAddress);

        // Then
        assertThat(customer.getEmail()).isEqualTo(newEmail);
        assertThat(customer.getPhone()).isEqualTo(newPhone);
        assertThat(customer.getAddress()).isEqualTo(newAddress);
        assertThat(customer.getName()).isEqualTo("Test Customer"); // Name unchanged
    }

    @Test
    @DisplayName("Should create customer using no-args constructor")
    void shouldCreateCustomerUsingNoArgsConstructor() {
        // When
        Customer customer = new Customer();

        // Then
        assertThat(customer.getId()).isNull();
        assertThat(customer.getName()).isNull();
        assertThat(customer.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should create customer using all-args constructor")
    void shouldCreateCustomerUsingAllArgsConstructor() {
        // Given
        UUID id = UUID.randomUUID();
        Email email = new Email("allargs@example.com");
        Phone phone = new Phone("3005555555");

        // When
        Customer customer = new Customer(id, "AllArgs Customer", email, phone, "AllArgs Address");

        // Then
        assertThat(customer.getId()).isEqualTo(id);
        assertThat(customer.getName()).isEqualTo("AllArgs Customer");
        assertThat(customer.getEmail()).isEqualTo(email);
        assertThat(customer.getPhone()).isEqualTo(phone);
        assertThat(customer.getAddress()).isEqualTo("AllArgs Address");
    }

    @Test
    @DisplayName("Should implement equals correctly")
    void shouldImplementEqualsCorrectly() {
        // Given
        UUID id = UUID.randomUUID();
        Email email = new Email("equals@example.com");
        Phone phone = new Phone("3001234567");

        Customer customer1 = Customer.builder()
                .id(id)
                .name("Test")
                .email(email)
                .phone(phone)
                .address("Address")
                .build();

        Customer customer2 = Customer.builder()
                .id(id)
                .name("Test")
                .email(email)
                .phone(phone)
                .address("Address")
                .build();

        // Then
        assertThat(customer1).isEqualTo(customer2);
        assertThat(customer1.hashCode()).isEqualTo(customer2.hashCode());
    }

    @Test
    @DisplayName("Should implement toString")
    void shouldImplementToString() {
        // Given
        Customer customer = Customer.builder()
                .name("ToString Test")
                .email(new Email("tostring@example.com"))
                .build();

        // When
        String result = customer.toString();

        // Then
        assertThat(result).contains("ToString Test");
    }

    @Test
    @DisplayName("Should generate unique ID for each customer created via factory")
    void shouldGenerateUniqueIdForEachCustomer() {
        // Given
        Email email = new Email("unique@example.com");
        Phone phone = new Phone("3001234567");

        // When
        Customer customer1 = Customer.create("Customer 1", email, phone, "Address 1");
        Customer customer2 = Customer.create("Customer 2", email, phone, "Address 2");

        // Then
        assertThat(customer1.getId()).isNotEqualTo(customer2.getId());
    }

    @Test
    @DisplayName("Should set and get all properties using setters")
    void shouldSetAndGetAllPropertiesUsingSetters() {
        // Given
        Customer customer = new Customer();
        UUID id = UUID.randomUUID();
        Email email = new Email("setter@example.com");
        Phone phone = new Phone("3007777777");

        // When
        customer.setId(id);
        customer.setName("Setter Name");
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setAddress("Setter Address");

        // Then
        assertThat(customer.getId()).isEqualTo(id);
        assertThat(customer.getName()).isEqualTo("Setter Name");
        assertThat(customer.getEmail()).isEqualTo(email);
        assertThat(customer.getPhone()).isEqualTo(phone);
        assertThat(customer.getAddress()).isEqualTo("Setter Address");
    }
}
