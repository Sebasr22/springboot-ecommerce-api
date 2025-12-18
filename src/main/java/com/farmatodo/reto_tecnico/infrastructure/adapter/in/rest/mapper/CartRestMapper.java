package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper;

import com.farmatodo.reto_tecnico.domain.model.Cart;
import com.farmatodo.reto_tecnico.domain.model.CartItem;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.CartItemResponse;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.CartResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for Cart <-> REST DTO conversion.
 */
@Mapper(componentModel = "spring")
public interface CartRestMapper {

    /**
     * Converts domain Cart to CartResponse DTO.
     *
     * @param cart domain model
     * @return REST response DTO
     */
    @Mapping(target = "totalAmount", expression = "java(cart.calculateTotal().amount())")
    @Mapping(target = "totalItemCount", expression = "java(cart.getTotalItemCount())")
    CartResponse toResponse(Cart cart);

    /**
     * Converts domain CartItem to CartItemResponse DTO.
     *
     * @param cartItem domain model
     * @return REST response DTO
     */
    @Mapping(target = "productId", expression = "java(cartItem.getProduct().getId())")
    @Mapping(target = "productName", expression = "java(cartItem.getProduct().getName())")
    @Mapping(target = "unitPrice", expression = "java(cartItem.getUnitPrice().amount())")
    @Mapping(target = "subtotal", expression = "java(cartItem.calculateSubtotal().amount())")
    CartItemResponse toItemResponse(CartItem cartItem);
}
