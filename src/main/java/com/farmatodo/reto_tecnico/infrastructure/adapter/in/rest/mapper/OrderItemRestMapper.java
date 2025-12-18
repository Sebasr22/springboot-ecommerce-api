package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper;

import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.OrderItemResponse;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for OrderItem <-> REST DTO conversion.
 * Converts domain order items to response DTOs.
 */
@Mapper(componentModel = "spring")
public interface OrderItemRestMapper {

    /**
     * Converts domain OrderItem to OrderItemResponse DTO.
     * @param orderItem domain model
     * @return response DTO
     */
    @Mapping(target = "productId", expression = "java(orderItem.getProduct().getId())")
    @Mapping(target = "productName", expression = "java(orderItem.getProduct().getName())")
    @Mapping(target = "unitPrice", expression = "java(orderItem.getUnitPrice().amount())")
    @Mapping(target = "subtotal", expression = "java(orderItem.calculateSubtotal().amount())")
    OrderItemResponse toResponse(OrderItem orderItem);

    /**
     * Converts list of OrderItems to list of OrderItemResponse DTOs.
     * @param items domain models
     * @return response DTOs
     */
    List<OrderItemResponse> toResponseList(List<OrderItem> items);
}
