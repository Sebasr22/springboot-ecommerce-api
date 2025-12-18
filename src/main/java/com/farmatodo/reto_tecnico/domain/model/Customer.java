package com.farmatodo.reto_tecnico.domain.model;

import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Customer domain entity.
 * Represents a customer in the system with their contact information.
 * Pure domain model without persistence annotations.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    /**
     * Unique identifier for the customer.
     */
    private UUID id;

    /**
     * Customer's full name.
     */
    @NotBlank(message = "Name cannot be blank")
    private String name;

    /**
     * Customer's email address (Value Object).
     */
    @NotNull(message = "Email cannot be null")
    private Email email;

    /**
     * Customer's phone number (Value Object).
     */
    @NotNull(message = "Phone cannot be null")
    private Phone phone;

    /**
     * Customer's delivery address.
     */
    @NotBlank(message = "Address cannot be blank")
    private String address;

    /**
     * Creates a new customer with generated UUID.
     * @param name customer name
     * @param email customer email
     * @param phone customer phone
     * @param address customer address
     * @return new Customer instance
     */
    public static Customer create(String name, Email email, Phone phone, String address) {
        return Customer.builder()
                .id(UUID.randomUUID())
                .name(name)
                .email(email)
                .phone(phone)
                .address(address)
                .build();
    }

    /**
     * Updates customer contact information.
     * @param email new email
     * @param phone new phone
     * @param address new address
     */
    public void updateContactInfo(Email email, Phone phone, String address) {
        this.email = email;
        this.phone = phone;
        this.address = address;
    }
}
