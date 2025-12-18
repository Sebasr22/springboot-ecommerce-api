package com.farmatodo.reto_tecnico.domain.port.out;

import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for customer persistence.
 * Defines the contract for customer data access operations.
 * Implementation will be provided by the infrastructure layer.
 */
public interface CustomerRepositoryPort {

    /**
     * Saves a customer (create or update).
     * @param customer the customer to save
     * @return saved customer
     */
    Customer save(Customer customer);

    /**
     * Finds a customer by ID.
     * @param id the customer ID
     * @return Optional containing the customer if found
     */
    Optional<Customer> findById(UUID id);

    /**
     * Finds a customer by email.
     * @param email the customer email
     * @return Optional containing the customer if found
     */
    Optional<Customer> findByEmail(Email email);

    /**
     * Checks if a customer exists with the given email.
     * @param email the email to check
     * @return true if customer exists
     */
    boolean existsByEmail(Email email);

    /**
     * Checks if a customer exists with the given phone number.
     * @param phone the phone number to check
     * @return true if customer exists
     */
    boolean existsByPhone(Phone phone);

    /**
     * Retrieves all customers.
     * @return list of all customers
     */
    List<Customer> findAll();

    /**
     * Deletes a customer by ID.
     * @param id the customer ID
     * @return true if customer was deleted
     */
    boolean deleteById(UUID id);
}
