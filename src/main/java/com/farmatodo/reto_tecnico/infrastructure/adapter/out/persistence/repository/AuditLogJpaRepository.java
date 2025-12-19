package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository;

import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for audit logs.
 * Provides database access for audit log entities.
 */
@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, UUID> {

    /**
     * Finds all audit logs with a specific trace ID.
     * Used to retrieve all events in a single HTTP request.
     *
     * @param traceId the trace ID to search for
     * @return list of audit log entities with matching trace ID
     */
    List<AuditLogEntity> findByTraceIdOrderByEventTimestampAsc(String traceId);
}
