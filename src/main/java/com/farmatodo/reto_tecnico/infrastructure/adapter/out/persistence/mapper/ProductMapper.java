package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.ProductEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Product <-> ProductEntity conversion.
 * Automatically generates implementation at compile time.
 *
 * Handles Value Object conversions:
 * - Money VO <-> BigDecimal
 */
@Mapper(
    componentModel = "spring",
    imports = {Money.class}
)
public interface ProductMapper {

    /**
     * Converts domain Product to JPA ProductEntity.
     * @param product domain model
     * @return JPA entity
     */
    @Mapping(target = "price", expression = "java(product.getPrice().amount())")
    @Mapping(target = "version", ignore = true)
    ProductEntity toEntity(Product product);

    /**
     * Converts JPA ProductEntity to domain Product.
     * @param entity JPA entity
     * @return domain model
     */
    @Mapping(target = "price", expression = "java(Money.of(entity.getPrice()))")
    Product toDomain(ProductEntity entity);

    /**
     * Converts list of ProductEntity to list of Product.
     * @param entities list of JPA entities
     * @return list of domain models
     */
    List<Product> toDomainList(List<ProductEntity> entities);
}
