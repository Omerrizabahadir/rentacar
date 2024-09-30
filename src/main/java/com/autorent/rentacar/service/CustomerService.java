package com.autorent.rentacar.service;

import com.autorent.rentacar.dto.AuthDto;
import com.autorent.rentacar.dto.CustomerDto;
import com.autorent.rentacar.dto.LoginDto;
import com.autorent.rentacar.enums.RoleEnum;
import com.autorent.rentacar.model.Customer;
import com.autorent.rentacar.repository.CustomerRepository;
import com.autorent.rentacar.util.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final PasswordEncoder passwordEncoder;

    private final CustomerRepository customerRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    public CustomerDto createCustomer(Customer customer){

        if(Objects.isNull(customer.getRoles())){
            customer.setRoles(RoleEnum.ROLE_USER.toString());
        }
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        return CustomerMapper.INSTANCE.customerToCustomerDto(customerRepository.save(customer));
    }

    public LoginDto login(AuthDto authDto){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDto.getEmail(), authDto.getPassword()));
        if(authentication.isAuthenticated()){
            return jwtService.generateToken(authentication);
        }
        throw  new UsernameNotFoundException("Invalid user details.");
    }
}
