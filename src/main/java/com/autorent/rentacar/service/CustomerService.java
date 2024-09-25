package com.autorent.rentacar.service;

import com.autorent.rentacar.enums.RoleEnum;
import com.autorent.rentacar.model.Customer;
import com.autorent.rentacar.repository.CustomerRepository;
import com.autorent.rentacar.util.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final PasswordEncoder passwordEncoder;

    private final CustomerRepository customerRepository;

    public Customer createCustomer(Customer customer){

        if(Objects.isNull(customer.getRoles())){
            customer.setRoles(RoleEnum.ROLE_USER.toString());
        }
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        return customerRepository.save(customer);
    }

}
