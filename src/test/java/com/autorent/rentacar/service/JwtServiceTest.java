package com.autorent.rentacar.service;

import com.autorent.rentacar.config.UserInfoDetails;
import com.autorent.rentacar.dto.LoginDto;
import com.autorent.rentacar.helper.CustomerDOFactory;
import com.autorent.rentacar.model.Customer;
import com.autorent.rentacar.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class JwtServiceTest {
    @InjectMocks
    private JwtService jwtService;

    @Mock                                           //JwtService dışında bir yere gşiderse Mocklarsın veya
    private CustomerRepository customerRepository;

    @Mock
    private Authentication authentication;

    private CustomerDOFactory customerDOFactory;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.customerDOFactory = new CustomerDOFactory();
    }

    @Test
    void validateToken_expiredTrue() {
        UserInfoDetails userInfoDetails = Optional.of(customerDOFactory.getCustomer()).map(UserInfoDetails::new).orElseThrow(() -> new UsernameNotFoundException("user not found"));
        Customer customer = customerDOFactory.getCustomer();

        when(customerRepository.findByEmail(any())).thenReturn(Optional.of(customer));

        SimpleGrantedAuthority simpleGrantedAuthority = new SimpleGrantedAuthority("ROLE_USER");
        Collection<SimpleGrantedAuthority> authCollection = Collections.singleton(simpleGrantedAuthority);
        when(authentication.getAuthorities())
                .thenReturn((Collection) authCollection);
        when(authentication.getName()).thenReturn(customer.getEmail());

        LoginDto response = jwtService.generateToken(authentication);

        boolean isValidate = jwtService.validateToken(response.getToken(), userInfoDetails);

        assertEquals(true, isValidate);
    }
}
