package com.autorent.rentacar.util;

import com.autorent.rentacar.dto.CustomerDto;
import com.autorent.rentacar.model.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CustomerMapper {
    CustomerMapper INSTANCE = Mappers.getMapper(CustomerMapper.class);
    CustomerDto customerToCustomerDto(Customer customer);
}
