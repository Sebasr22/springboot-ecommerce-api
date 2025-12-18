package com.farmatodo.reto_tecnico.domain.port.in;

import com.farmatodo.reto_tecnico.domain.model.Customer;

/**
 * Input port for customer registration use case.
 * Defines the contract for registering new customers in the system.
 *
 * Business rules:
 * - Email must be unique (checked before registration)
 * - All customer data must be validated
 * - Generates UUID for new customer
 */
public interface RegisterCustomerUseCase {

    /**
     * Registers a new customer in the system.
     * Validates that email doesn't already exist.
     *
     * @param customer the customer to register
     * @return registered customer with generated ID
     * @throws com.farmatodo.reto_tecnico.domain.exception.CustomerAlreadyExistsException
     *         if customer with same email already exists
     */
    Customer register(Customer customer);
}
