package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.OrderItemEntity;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.ProductEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for OrderItem <-> OrderItemEntity conversion.
 * Automatically generates implementation at compile time.
 *
 * Handles:
 * - Money VO <-> BigDecimal
 * - Product reference mapping
 *
 * NOTE: Requires ProductMapper for Product conversion.
 */
@Mapper(
    componentModel = "spring",
    uses = {ProductMapper.class},
    imports = {Money.class}
)
public interface OrderItemMapper {

    /**
     * Converts domain OrderItem to JPA OrderItemEntity.
     * Extracts product ID from Product domain object.
     *
     * @param orderItem domain model
     * @return JPA entity
     */
    @Mapping(target = "productId", expression = "java(orderItem.getProduct().getId())")
    @Mapping(target = "unitPrice", expression = "java(orderItem.getUnitPrice().amount())")
    @Mapping(target = "order", ignore = true) // Set by OrderEntity
    OrderItemEntity toEntity(OrderItem orderItem);

    /**
     * Converts JPA OrderItemEntity to domain OrderItem.
     * Requires Product to be loaded separately.
     *
     * @param entity JPA entity
     * @param product the product domain object
     * @return domain model
     */
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "product", source = "product")
    @Mapping(target = "quantity", source = "entity.quantity")
    @Mapping(target = "unitPrice", expression = "java(Money.of(entity.getUnitPrice()))")
    OrderItem toDomain(OrderItemEntity entity, Product product);
}
