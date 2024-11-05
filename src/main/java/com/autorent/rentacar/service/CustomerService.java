package com.autorent.rentacar.service;

import com.autorent.rentacar.dto.AuthDto;
import com.autorent.rentacar.dto.CustomerDto;
import com.autorent.rentacar.dto.LoginDto;
import com.autorent.rentacar.enums.RoleEnum;
import com.autorent.rentacar.exception.CustomerPasswordNotStrongException;
import com.autorent.rentacar.exception.EmailAlreadyExistsException;
import com.autorent.rentacar.model.Customer;
import com.autorent.rentacar.repository.CustomerRepository;
import com.autorent.rentacar.util.CustomerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
public class CustomerService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private EmailService emailService;

    public CustomerDto createCustomer(Customer customer){

       //E-posta adresinin benzersizliğini kontrol et
        String email = customer.getEmail().trim().toLowerCase();
        if (customerRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email already in use!");
        }
        //Şifrenin güçlü olduğunu kontrol et
        if (!isPasswordStrong(customer.getPassword())) {
            throw new CustomerPasswordNotStrongException("Password does not meet the security criteria.");
        }
        //customer ROLE
        if(Objects.isNull(customer.getRoles())){
            customer.setRoles(RoleEnum.ROLE_USER.toString());
        }
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));

        Customer savedCustomer = customerRepository.save(customer);

        emailService.sendWelcomeMail(customer.getEmail(), customer.getFirstName(), customer.getLastName());
        return CustomerMapper.INSTANCE.customerToCustomerDto(savedCustomer);
    }

    public LoginDto login(AuthDto authDto){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authDto.getEmail(), authDto.getPassword()));
        if(authentication.isAuthenticated()){
            return jwtService.generateToken(authentication);
        }
        throw  new UsernameNotFoundException("Invalid user details.");
    }

    private boolean isPasswordStrong(String password) {
        // Şifre en az 8 karakter olmalı, büyük harf, küçük harf, rakam ve özel karakter içermeli
        return password.length() >= 8 &&
                password.matches("(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}");
    }
}
