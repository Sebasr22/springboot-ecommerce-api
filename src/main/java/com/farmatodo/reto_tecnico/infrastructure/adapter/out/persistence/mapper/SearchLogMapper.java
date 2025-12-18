package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.reto_tecnico.domain.model.SearchLog;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.SearchLogEntity;
import org.mapstruct.Mapper;

/**
 * MapStruct mapper for SearchLog domain model and SearchLogEntity JPA entity.
 * Converts between domain and persistence layers.
 */
@Mapper(componentModel = "spring")
public interface SearchLogMapper {

    /**
     * Converts domain SearchLog to JPA SearchLogEntity.
     * @param searchLog domain model
     * @return JPA entity
     */
    SearchLogEntity toEntity(SearchLog searchLog);

    /**
     * Converts JPA SearchLogEntity to domain SearchLog.
     * @param entity JPA entity
     * @return domain model
     */
    SearchLog toDomain(SearchLogEntity entity);
}
