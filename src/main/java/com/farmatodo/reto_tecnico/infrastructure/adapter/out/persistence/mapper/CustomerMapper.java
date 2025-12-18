package com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.mapper;

import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.infrastructure.adapter.out.persistence.entity.CustomerEntity;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Customer <-> CustomerEntity conversion.
 * Automatically generates implementation at compile time.
 *
 * Handles Value Object conversions:
 * - Email VO <-> String
 * - Phone VO <-> String
 */
@Mapper(
    componentModel = "spring",
    imports = {Email.class, Phone.class}
)
public interface CustomerMapper {

    /**
     * Converts domain Customer to JPA CustomerEntity.
     * @param customer domain model
     * @return JPA entity
     */
    @Mapping(target = "email", expression = "java(customer.getEmail().value())")
    @Mapping(target = "phone", expression = "java(customer.getPhone().value())")
    CustomerEntity toEntity(Customer customer);

    /**
     * Converts JPA CustomerEntity to domain Customer.
     * @param entity JPA entity
     * @return domain model
     */
    @Mapping(target = "email", expression = "java(new Email(entity.getEmail()))")
    @Mapping(target = "phone", expression = "java(new Phone(entity.getPhone()))")
    Customer toDomain(CustomerEntity entity);
}
