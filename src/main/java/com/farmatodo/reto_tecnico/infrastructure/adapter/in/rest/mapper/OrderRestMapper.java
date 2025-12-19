package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper;

import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.OrderItem;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request.CreateOrderRequest;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.OrderItemResponse;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.OrderResponse;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper for Order <-> REST DTO conversion.
 * Handles order creation requests and order response DTOs.
 */
@Mapper(
    componentModel = "spring",
    uses = {OrderItemRestMapper.class},
    imports = {Email.class, Phone.class}
)
public interface OrderRestMapper {

    /**
     * Converts CreateOrderRequest to domain Customer.
     * Extracts customer information from order request.
     *
     * @param request order creation request
     * @return domain customer
     */
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "name", source = "customerName")
    @Mapping(target = "email", expression = "java(new Email(request.getCustomerEmail()))")
    @Mapping(target = "phone", expression = "java(new Phone(request.getCustomerPhone()))")
    @Mapping(target = "address", source = "customerAddress")
    Customer toCustomer(CreateOrderRequest request);

    /**
     * Converts domain Order to OrderResponse DTO.
     * Excludes sensitive payment information.
     *
     * @param order domain order
     * @return response DTO
     */
    @Mapping(target = "customerName", source = "customer.name")
    @Mapping(target = "customerEmail", expression = "java(order.getCustomer().getEmail().value())")
    @Mapping(target = "deliveryAddress", source = "deliveryAddress")
    @Mapping(target = "items", source = "items")
    @Mapping(target = "totalAmount", expression = "java(order.getTotalAmount().amount())")
    @Mapping(target = "status", expression = "java(order.getStatus().name())")
    @Mapping(target = "paymentCompleted", expression = "java(isPaymentCompleted(order))")
    OrderResponse toResponse(Order order);

    /**
     * Converts list of Orders to list of OrderResponse DTOs.
     * @param orders domain orders
     * @return response DTOs
     */
    List<OrderResponse> toResponseList(List<Order> orders);

    /**
     * Determines if payment is completed based on order status.
     * @param order the order
     * @return true if payment is confirmed
     */
    default boolean isPaymentCompleted(Order order) {
        return order.getStatus() == Order.OrderStatus.PAYMENT_CONFIRMED ||
               order.getStatus() == Order.OrderStatus.COMPLETED;
    }
}
