package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.reto_tecnico.domain.model.CartItem;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CartItemEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for CartItem <-> CartItemEntity conversion.
 * Automatically generates implementation at compile time.
 */
@Mapper(
        componentModel = "spring",
        uses = {ProductMapper.class},
        imports = {Money.class}
)
public interface CartItemMapper {

    /**
     * Converts domain CartItem to JPA CartItemEntity.
     *
     * @param cartItem domain model
     * @return JPA entity
     */
    @Mapping(target = "unitPrice", expression = "java(cartItem.getUnitPrice().amount())")
    @Mapping(target = "cart", ignore = true) // Handle in adapter
    CartItemEntity toEntity(CartItem cartItem);

    /**
     * Converts JPA CartItemEntity to domain CartItem.
     *
     * @param entity JPA entity
     * @return domain model
     */
    @Mapping(target = "unitPrice", expression = "java(new Money(entity.getUnitPrice()))")
    CartItem toDomain(CartItemEntity entity);
}
