package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for CustomerEntity.
 * Provides CRUD operations and custom queries for customer persistence.
 */
@Repository
public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {

    /**
     * Finds a customer by email address.
     * @param email the customer email
     * @return Optional containing customer if found
     */
    Optional<CustomerEntity> findByEmail(String email);

    /**
     * Checks if a customer exists with given email.
     * @param email the customer email
     * @return true if customer exists
     */
    boolean existsByEmail(String email);

    /**
     * Checks if a customer exists with given phone number.
     * @param phone the customer phone number
     * @return true if customer exists
     */
    boolean existsByPhone(String phone);
}
