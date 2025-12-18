package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.adapter;

import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.domain.port.out.CustomerRepositoryPort;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.CustomerMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.CustomerJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementation for Customer persistence.
 * Implements hexagonal architecture output port using JPA repository.
 * Translates between domain Customer and JPA CustomerEntity.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class CustomerRepositoryAdapter implements CustomerRepositoryPort {

    private final CustomerJpaRepository jpaRepository;
    private final CustomerMapper mapper;

    @Override
    public Customer save(Customer customer) {
        log.debug("Saving customer: {}", customer.getId());
        CustomerEntity entity = mapper.toEntity(customer);
        CustomerEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        log.debug("Finding customer by ID: {}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByEmail(Email email) {
        log.debug("Finding customer by email: {}", email.value());
        return jpaRepository.findByEmail(email.value())
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsByEmail(Email email) {
        log.debug("Checking if customer exists with email: {}", email.value());
        return jpaRepository.existsByEmail(email.value());
    }

    @Override
    public boolean existsByPhone(Phone phone) {
        log.debug("Checking if customer exists with phone: {}", phone.value());
        return jpaRepository.existsByPhone(phone.value());
    }

    @Override
    public List<Customer> findAll() {
        log.debug("Finding all customers");
        return jpaRepository.findAll().stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(UUID id) {
        log.debug("Deleting customer: {}", id);
        if (jpaRepository.existsById(id)) {
            jpaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
