package com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.mapper;

import com.farmatodo.reto_tecnico.domain.model.Customer;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Email;
import com.farmatodo.reto_tecnico.domain.model.valueobjects.Phone;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.request.CreateCustomerRequest;
import com.farmatodo.reto_tecnico.infrastructure.adapter.in.rest.dto.response.CustomerResponse;
import org.mapstruct.*;

/**
 * MapStruct mapper for Customer <-> REST DTO conversion.
 * Handles customer registration requests and customer responses.
 */
@Mapper(
    componentModel = "spring",
    imports = {Email.class, Phone.class}
)
public interface CustomerRestMapper {

    /**
     * Converts CreateCustomerRequest to domain Customer.
     * @param request customer registration request DTO
     * @return domain customer
     */
    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "email", expression = "java(new Email(request.getEmail()))")
    @Mapping(target = "phone", expression = "java(new Phone(request.getPhone()))")
    Customer toDomain(CreateCustomerRequest request);

    /**
     * Converts domain Customer to CustomerResponse DTO.
     * @param customer domain model
     * @return response DTO
     */
    @Mapping(target = "email", expression = "java(customer.getEmail().value())")
    @Mapping(target = "phone", expression = "java(customer.getPhone().value())")
    CustomerResponse toResponse(Customer customer);
}
