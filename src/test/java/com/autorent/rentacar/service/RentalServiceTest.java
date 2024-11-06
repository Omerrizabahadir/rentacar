package com.autorent.rentacar.service;

import com.autorent.rentacar.dto.RentalCarInfo;
import com.autorent.rentacar.dto.RentalRequest;
import com.autorent.rentacar.exception.CarNotFoundException;
import com.autorent.rentacar.exception.InsufficientCarStockException;
import com.autorent.rentacar.helper.CarDOFactory;
import com.autorent.rentacar.helper.RentalRequestDOFactory;
import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.model.Customer;
import com.autorent.rentacar.repository.CarRepository;
import com.autorent.rentacar.repository.CustomerRepository;
import com.autorent.rentacar.repository.RentalRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RentalServiceTest {
    @InjectMocks
    private RentalService rentalService;

    @Mock
    private RentalRepository rentalRepository;

    @Mock
    private CarRepository carRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private MimeMessage mimeMessage;

    @Mock
    private MimeMessageHelper mimeMessageHelper;

    private RentalRequestDOFactory rentalRequestDOFactory;

    private CarDOFactory carDOFactory;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        this.rentalRequestDOFactory = new RentalRequestDOFactory();
        this.carDOFactory = new CarDOFactory();
    }

    @Test
    void rent_success() throws MessagingException {
        Long carId = 34L;
        Long customerId = 5L;
        int quantity = 1;

        RentalCarInfo rentalCarInfo = rentalRequestDOFactory.getRentalCarInfo(quantity, carId);
        RentalRequest rentalRequest = rentalRequestDOFactory.getRentalRequest(quantity, carId, customerId);

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setFirstName("TEST_FIRSTNAME");
        customer.setLastName("TEST_LASTNAME");
        customer.setEmail("test@gmail.com");

        Car car = carDOFactory.getCarWithId(carId);
        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        // MimeMessage objesini oluşturun
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

        // Email içeriğini ayarlayın
        mimeMessageHelper.setFrom("test@gmail.com");
        mimeMessageHelper.setTo(customer.getEmail());
        mimeMessageHelper.setSubject("Rental Confirmation");
        mimeMessageHelper.setText("Dear " + customer.getFirstName() + ",\n\n" +
                "Your rental has been successfully processed.\n\n" +
                "Total Amount: $15000.0\n\nThank you for choosing us!\n\nBest regards,\nYour Car Rental Team");

        // Set field for email
        ReflectionTestUtils.setField(rentalService, "emailFrom", "test@gmail.com");

        boolean response = rentalService.rent(rentalRequest);

        assertTrue(response);

        verify(rentalRepository, times(1)).save(any());
        verify(javaMailSender).send(mimeMessage);
        verify(carRepository, times(2)).save(any());
    }

    @Test
    void rent_fail_CarNotFoundException() {
        Long carId = 34L;
        Long customerId = 5L;
        int quantity = 1;

        RentalCarInfo rentalCarInfo = rentalRequestDOFactory.getRentalCarInfo(quantity, carId);
        RentalRequest rentalRequest = rentalRequestDOFactory.getRentalRequest(quantity, carId, customerId);

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setFirstName("TEST_FIRSTNAME");
        customer.setLastName("TEST_LASTNAME");
        customer.setEmail("test@gmail.com");

        when(carRepository.findById(carId)).thenReturn(Optional.empty());
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        ReflectionTestUtils.setField(rentalService, "emailFrom", "test@gmail.com");

        assertThrows(CarNotFoundException.class, () -> rentalService.rent(rentalRequest));

        verify(javaMailSender, times(0)).send(mimeMessage);
        verify(carRepository, times(1)).findById(carId);
    }

    @Test
    void checkAvailability_fail_InsufficientCarStockException() throws Exception {
        MockitoAnnotations.openMocks(this);

        Long carId = 34L;
        boolean isActive = false;
        Long customerId = 5L;
        int quantity = 10;


        RentalCarInfo rentalCarInfo = rentalRequestDOFactory.getRentalCarInfo(quantity, carId);
        RentalRequest rentalRequest = rentalRequestDOFactory.getRentalRequest(quantity, carId, customerId);

        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setFirstName("TEST_FIRSTNAME");
        customer.setLastName("TEST_LASTNAME");
        customer.setEmail("test@gmail.com");

        Car car = new Car();
        car.setId(carId);
        car.setCarAvailableStock(0L);
        car.setIsRented(false);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        ReflectionTestUtils.setField(rentalService, "emailFrom", "test@gmail.com");

        InsufficientCarStockException thrown = Assertions.assertThrows(InsufficientCarStockException.class,
                () -> rentalService.checkCarAvailability(carId, isActive));

        assertEquals("No available stock for this car.", thrown.getMessage());

        verify(javaMailSender, times(0)).send(mimeMessage);

        verify(carRepository, times(1)).findById(carId);
    }
}


