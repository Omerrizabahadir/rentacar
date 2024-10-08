package com.autorent.rentacar.controller;

import com.autorent.rentacar.dto.AuthDto;
import com.autorent.rentacar.dto.CustomerDto;
import com.autorent.rentacar.dto.LoginDto;
import com.autorent.rentacar.model.Customer;
import com.autorent.rentacar.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customer")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<CustomerDto> createCustomer(@RequestBody Customer customer){
        return new ResponseEntity<>(customerService.createCustomer(customer), HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginDto> login(@RequestBody AuthDto authDto){
        return new ResponseEntity<>(customerService.login(authDto),HttpStatus.OK);
    }
}
