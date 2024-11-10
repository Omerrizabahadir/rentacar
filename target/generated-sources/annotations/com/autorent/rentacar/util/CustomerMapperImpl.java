package com.autorent.rentacar.util;

import com.autorent.rentacar.dto.CustomerDto;
import com.autorent.rentacar.model.Customer;
import javax.annotation.processing.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-11-07T15:47:22+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 23.0.1 (Oracle Corporation)"
)
public class CustomerMapperImpl implements CustomerMapper {

    @Override
    public CustomerDto customerToCustomerDto(Customer customer) {
        if ( customer == null ) {
            return null;
        }

        CustomerDto customerDto = new CustomerDto();

        customerDto.setId( customer.getId() );
        customerDto.setEmail( customer.getEmail() );
        customerDto.setFirstName( customer.getFirstName() );
        customerDto.setLastName( customer.getLastName() );
        customerDto.setAddress( customer.getAddress() );
        customerDto.setRoles( customer.getRoles() );

        return customerDto;
    }
}
