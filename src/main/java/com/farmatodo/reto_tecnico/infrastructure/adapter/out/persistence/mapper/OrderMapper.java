package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.Order;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Money;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.OrderEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Order <-> OrderEntity conversion.
 * Automatically generates implementation at compile time.
 *
 * Handles:
 * - Money VO <-> BigDecimal
 * - OrderStatus enum mapping
 * - Customer reference mapping
 * - OrderItem collection mapping
 *
 * NOTE: Requires OrderItemMapper and CustomerMapper for related entities.
 */
@Mapper(
    componentModel = "spring",
    uses = {OrderItemMapper.class, CustomerMapper.class},
    imports = {Money.class}
)
public interface OrderMapper {

    /**
     * Converts domain Order to JPA OrderEntity.
     * Extracts customer ID from Customer domain object.
     *
     * @param order domain model
     * @return JPA entity
     */
    @Mapping(target = "customerId", expression = "java(order.getCustomer().getId())")
    @Mapping(target = "totalAmount", expression = "java(order.getTotalAmount().amount())")
    @Mapping(target = "status", expression = "java(mapOrderStatus(order.getStatus()))")
    @Mapping(target = "deliveryAddress", source = "deliveryAddress")
    @Mapping(target = "items", ignore = true) // Handle separately in adapter
    OrderEntity toEntity(Order order);

    /**
     * Converts JPA OrderEntity to domain Order.
     * Requires Customer to be loaded separately.
     *
     * @param entity JPA entity
     * @param customer the customer domain object
     * @return domain model
     */
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "totalAmount", expression = "java(Money.of(entity.getTotalAmount()))")
    @Mapping(target = "status", expression = "java(mapEntityStatus(entity.getStatus()))")
    @Mapping(target = "createdAt", source = "entity.createdAt")
    @Mapping(target = "updatedAt", source = "entity.updatedAt")
    @Mapping(target = "paymentToken", source = "entity.paymentToken")
    @Mapping(target = "deliveryAddress", source = "entity.deliveryAddress")
    @Mapping(target = "items", ignore = true) // Handle separately in adapter
    Order toDomain(OrderEntity entity, Customer customer);

    /**
     * Maps domain OrderStatus to entity OrderStatus.
     * @param status domain status
     * @return entity status
     */
    default OrderEntity.OrderStatus mapOrderStatus(Order.OrderStatus status) {
        return OrderEntity.OrderStatus.valueOf(status.name());
    }

    /**
     * Maps entity OrderStatus to domain OrderStatus.
     * @param status entity status
     * @return domain status
     */
    default Order.OrderStatus mapEntityStatus(OrderEntity.OrderStatus status) {
        return Order.OrderStatus.valueOf(status.name());
    }
}
