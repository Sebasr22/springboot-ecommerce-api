package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper;

import com.farmatodo.reto_tecnico.domain.model.Product;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.ProductResponse;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Product <-> ProductResponse conversion.
 * Converts domain models to REST API response DTOs.
 */
@Mapper(componentModel = "spring")
public interface ProductRestMapper {

    /**
     * Converts domain Product to ProductResponse DTO.
     * @param product domain model
     * @return response DTO
     */
    @Mapping(target = "price", expression = "java(product.getPrice().amount())")
    @Mapping(target = "inStock", expression = "java(product.isInStock())")
    ProductResponse toResponse(Product product);

    /**
     * Converts list of Products to list of ProductResponse DTOs.
     * @param products domain models
     * @return response DTOs
     */
    List<ProductResponse> toResponseList(List<Product> products);
}
