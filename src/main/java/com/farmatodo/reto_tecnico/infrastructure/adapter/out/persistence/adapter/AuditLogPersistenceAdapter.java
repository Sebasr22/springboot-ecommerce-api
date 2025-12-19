package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.adapter;

import com.farmatodo.reto_tecnico.domain.model.AuditLog;
import com.farmatodo.reto_tecnico.domain.port.out.AuditLogRepositoryPort;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.AuditLogEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper.AuditLogMapper;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Persistence adapter for audit logs.
 * Implements the AuditLogRepositoryPort using JPA.
 * Follows hexagonal architecture pattern.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogPersistenceAdapter implements AuditLogRepositoryPort {

    private final AuditLogJpaRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    public AuditLog save(AuditLog auditLog) {
        log.debug("Saving audit log: eventType={}, traceId={}",
                auditLog.getEventType(), auditLog.getTraceId());

        AuditLogEntity entity = auditLogMapper.toEntity(auditLog);
        AuditLogEntity saved = auditLogRepository.save(entity);

        log.debug("Audit log saved successfully with ID: {}", saved.getId());

        return auditLogMapper.toDomain(saved);
    }

    @Override
    public List<AuditLog> findByTraceId(String traceId) {
        log.debug("Finding audit logs for trace ID: {}", traceId);

        List<AuditLogEntity> entities = auditLogRepository.findByTraceIdOrderByEventTimestampAsc(traceId);

        log.debug("Found {} audit logs for trace ID: {}", entities.size(), traceId);

        return entities.stream()
                .map(auditLogMapper::toDomain)
                .toList();
    }
}
