package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.adapter;

import com.farmatodo.reto_tecnico.domain.model.SearchLog;
import com.farmatodo.reto_tecnico.domain.port.out.SearchLogRepositoryPort;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.SearchLogEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.SearchLogMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.SearchLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * Adapter implementation for SearchLog persistence.
 * Implements hexagonal architecture output port using JPA repository.
 * Translates between domain SearchLog and JPA SearchLogEntity.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class SearchLogPersistenceAdapter implements SearchLogRepositoryPort {

    private final SearchLogJpaRepository jpaRepository;
    private final SearchLogMapper mapper;

    @Override
    public SearchLog save(SearchLog searchLog) {
        log.debug("Persisting search log: query='{}', results={}",
                searchLog.getQuery(), searchLog.getResultsCount());

        SearchLogEntity entity = mapper.toEntity(searchLog);
        SearchLogEntity saved = jpaRepository.save(entity);

        log.debug("Search log persisted successfully with ID: {}", saved.getId());
        return mapper.toDomain(saved);
    }
}
