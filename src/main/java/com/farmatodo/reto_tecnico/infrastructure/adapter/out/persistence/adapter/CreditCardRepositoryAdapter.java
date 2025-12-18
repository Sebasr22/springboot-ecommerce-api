package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.adapter;

import com.farmatodo.reto_tecnico.domain.model.CreditCard;
import com.farmatodo.reto_tecnico.domain.port.out.CreditCardRepositoryPort;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CreditCardEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.CreditCardMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.CreditCardJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapter implementation for CreditCard persistence.
 * Implements hexagonal architecture output port using JPA repository.
 * Translates between domain CreditCard and JPA CreditCardEntity.
 *
 * SECURITY: All token data is encrypted at rest via CryptoConverter.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class CreditCardRepositoryAdapter implements CreditCardRepositoryPort {

    private final CreditCardJpaRepository jpaRepository;
    private final CreditCardMapper mapper;

    @Override
    public CreditCard save(CreditCard creditCard) {
        log.debug("Saving credit card: {} for customer: {}",
                creditCard.getId(), creditCard.getCustomerId());
        CreditCardEntity entity = mapper.toEntity(creditCard);
        CreditCardEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<CreditCard> findById(UUID id) {
        log.debug("Finding credit card by ID: {}", id);
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<CreditCard> findByToken(String token) {
        log.debug("Finding credit card by token");
        return jpaRepository.findByToken(token)
                .map(mapper::toDomain);
    }

    @Override
    public List<CreditCard> findByCustomerId(UUID customerId) {
        log.debug("Finding credit cards for customer: {}", customerId);
        return jpaRepository.findByCustomerId(customerId).stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByToken(String token) {
        log.debug("Checking if credit card exists with token");
        return jpaRepository.findByToken(token).isPresent();
    }
}
