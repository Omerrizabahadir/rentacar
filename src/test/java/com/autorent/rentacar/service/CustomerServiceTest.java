package com.autorent.rentacar.service;


import com.autorent.rentacar.dto.AuthDto;
import com.autorent.rentacar.dto.CustomerDto;
import com.autorent.rentacar.dto.LoginDto;
import com.autorent.rentacar.exception.CustomerPasswordNotStrongException;
import com.autorent.rentacar.exception.EmailAlreadyExistsException;
import com.autorent.rentacar.helper.CustomerDOFactory;
import com.autorent.rentacar.model.Customer;
import com.autorent.rentacar.repository.CustomerRepository;
import com.autorent.rentacar.util.CustomerMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class CustomerServiceTest {
    @InjectMocks
    private CustomerService customerService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private EmailService emailService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    private CustomerDOFactory customerDOFactory;

    @BeforeEach
    void setup() {

        MockitoAnnotations.openMocks(this);
        this.customerDOFactory = new CustomerDOFactory();
    }
    @Test
    void createCustomer_success() {
        Customer customer = customerDOFactory.getCustomer();

        when(customerRepository.existsByEmail(customer.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn(customer.getPassword());
        when(customerRepository.save(customer)).thenReturn(customer);
        doNothing().when(emailService).sendWelcomeMail(customer.getEmail(), customer.getFirstName(), customer.getLastName());

        CustomerDto customerDto = new CustomerDto();
        customerDto.setEmail("test@gmail.com");
        customerDto.setFirstName("test_firstName");
        customerDto.setLastName("test_lastName");

        when(customerMapper.customerToCustomerDto(customer)).thenReturn(customerDto);

        CustomerDto response = customerService.createCustomer(customer);

        assertNotNull(response);
        assertEquals(customerDto.getEmail(), response.getEmail());
        assertEquals(customerDto.getFirstName(), response.getFirstName());
        assertEquals(customerDto.getLastName(), response.getLastName());

        verify(customerRepository, times(1)).existsByEmail(customer.getEmail());
        verify(passwordEncoder, times(1)).encode(customer.getPassword());
        verify(customerRepository, times(1)).save(customer);
        verify(emailService, times(1)).sendWelcomeMail(customer.getEmail(), customer.getFirstName(), customer.getLastName());
    }


    @Test
    void createCustomer_EmailAlreadyExistsException() {
        Customer customer = customerDOFactory.getCustomer();

        when(customerRepository.existsByEmail(customer.getEmail())).thenReturn(true);

        EmailAlreadyExistsException thrown = assertThrows(EmailAlreadyExistsException.class,
                () -> customerService.createCustomer(customer));

        assertNotNull(thrown);
        assertEquals("Email already in use!", thrown.getMessage());
        verify(customerRepository, times(1)).existsByEmail(customer.getEmail());
    }

    @Test
    void createCustomer_CustomerPasswordNotStrongException() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setEmail("test@example.com");
        customer.setFirstName("test_firstName");
        customer.setLastName("test_lastName");
        customer.setPassword("weak");
        customer.setRoles("ROLE_USER");

        CustomerPasswordNotStrongException thrown = assertThrows(CustomerPasswordNotStrongException.class,
                () -> customerService.createCustomer(customer));

    }

    @Test
    void login_success() {
        AuthDto authDto = new AuthDto();
        authDto.setEmail("test@example.com");
        authDto.setPassword("Password1!");

        // Create a test customer in the database
        Customer customer = new Customer();
        customer.setEmail("test@example.com");
        customer.setPassword(passwordEncoder.encode("Password1!"));
        customerRepository.save(customer);

        Authentication authentication = mock(Authentication.class);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("test@example.com");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        LoginDto loginDto = new LoginDto();
        loginDto.setToken("testToken");
        loginDto.setCustomerId(customer.getId());

        when(jwtService.generateToken(authentication)).thenReturn(loginDto);

        LoginDto response = customerService.login(authDto);

        // Sonuçları doğrulayın
        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        assertEquals(customer.getId(), response.getCustomerId());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateToken(authentication);

    }

    @Test
    void login_fail() {
        AuthDto authDto = new AuthDto();
        authDto.setEmail("test@example.com");
        authDto.setPassword("Password1!");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenThrow(new AuthenticationException("Invalid credentials") {
        });

        assertThrows(AuthenticationException.class, () -> {
            customerService.login(authDto);
        });

        // Verify mock çağrılarını doğrulayın
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}