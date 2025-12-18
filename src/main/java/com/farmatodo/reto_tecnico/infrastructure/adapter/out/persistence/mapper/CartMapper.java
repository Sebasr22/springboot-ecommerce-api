package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.reto_tecnico.domain.model.Cart;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CartEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Cart <-> CartEntity conversion.
 * Automatically generates implementation at compile time.
 */
@Mapper(
        componentModel = "spring",
        uses = {CartItemMapper.class}
)
public interface CartMapper {

    /**
     * Converts domain Cart to JPA CartEntity.
     *
     * @param cart domain model
     * @return JPA entity
     */
    @Mapping(target = "items", ignore = true) // Handle manually in adapter to maintain bidirectional relationship
    CartEntity toEntity(Cart cart);

    /**
     * Converts JPA CartEntity to domain Cart.
     *
     * @param entity JPA entity
     * @return domain model
     */
    Cart toDomain(CartEntity entity);
}
