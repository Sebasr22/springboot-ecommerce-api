package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.SearchLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Spring Data JPA repository for SearchLogEntity.
 * Provides persistence operations for search query logging.
 */
@Repository
public interface SearchLogJpaRepository extends JpaRepository<SearchLogEntity, UUID> {
    // Spring Data JPA auto-implements basic CRUD operations
}
