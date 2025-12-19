package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.reto_tecnico.domain.model.AuditLog;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.AuditLogEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for AuditLog domain model and AuditLogEntity.
 * Converts between domain layer and infrastructure layer representations.
 */
@Mapper(componentModel = "spring")
public interface AuditLogMapper {

    /**
     * Converts domain model to JPA entity.
     *
     * @param auditLog domain model
     * @return JPA entity
     */
    AuditLogEntity toEntity(AuditLog auditLog);

    /**
     * Converts JPA entity to domain model.
     *
     * @param entity JPA entity
     * @return domain model
     */
    AuditLog toDomain(AuditLogEntity entity);
}
