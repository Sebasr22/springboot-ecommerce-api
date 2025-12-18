package com.farmatodo.reto_tecnico.application.service;

import com.farmatodo.reto_tecnico.domain.exception.CustomerAlreadyExistsException;
import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.port.in.RegisterCustomerUseCase;
import com.farmatodo.reto_tecnico.domain.port.out.CustomerRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for customer registration use case.
 * Validates email uniqueness before registering new customers.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements RegisterCustomerUseCase {

    private final CustomerRepositoryPort customerRepository;

    @Override
    @Transactional
    public Customer register(Customer customer) {
        log.info("Registering new customer with email: {}", customer.getEmail().value());

        // Validate email uniqueness
        if (customerRepository.existsByEmail(customer.getEmail())) {
            log.warn("Attempt to register duplicate email: {}", customer.getEmail().value());
            throw new CustomerAlreadyExistsException(customer.getEmail().value());
        }

        // Validate phone uniqueness
        if (customerRepository.existsByPhone(customer.getPhone())) {
            log.warn("Attempt to register duplicate phone: {}", customer.getPhone().value());
            throw new CustomerAlreadyExistsException("Phone number already registered: " + customer.getPhone().value());
        }

        // Save customer
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Customer registered successfully with ID: {}", savedCustomer.getId());

        return savedCustomer;
    }
}
