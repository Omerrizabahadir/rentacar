package com.autorent.rentacar.service;

import com.autorent.rentacar.dto.CustomerRentalDto;
import com.autorent.rentacar.dto.PendingRentalDto;
import com.autorent.rentacar.dto.RentalCarInfo;
import com.autorent.rentacar.dto.RentalRequest;
import com.autorent.rentacar.enums.CarStatus;
import com.autorent.rentacar.exception.CarAlreadyRentedException;
import com.autorent.rentacar.exception.CarNotFoundException;
import com.autorent.rentacar.exception.InsufficientCarStockException;
import com.autorent.rentacar.helper.CarDOFactory;
import com.autorent.rentacar.helper.RentalRequestDOFactory;
import com.autorent.rentacar.model.Brand;
import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.model.Customer;
import com.autorent.rentacar.model.Rental;
import com.autorent.rentacar.repository.BrandRepository;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
    private BrandRepository brandRepository;

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

    @Test
    void checkCarAvailability_shouldThrowCarAlreadyRentedException_whenCarIsAlreadyRented() {

        Long carId = 1L;
        boolean isActive = true;

        Car car = new Car();
        car.setId(carId);
        car.setCarAvailableStock(1L);
        car.setCarStatus(CarStatus.RENTED);
        car.setIsRented(true);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        when(carRepository.findActiveRentalsByCarId(eq(carId), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(new Rental()));

        CarAlreadyRentedException thrown = Assertions.assertThrows(
                CarAlreadyRentedException.class,
                () -> rentalService.checkCarAvailability(carId, isActive)
        );

        assertEquals("The car you chose was rented by someone else", thrown.getMessage());

        verify(carRepository, times(1)).findById(carId);
        verify(carRepository, times(1)).findActiveRentalsByCarId(eq(carId), any(LocalDateTime.class));
    }

    @Test
    void getRentalsWithCustomerInfo_shouldReturnCustomerRentalDto_whenValidCustomerId() {

        Long customerId = 1L;
        Customer customer = new Customer();
        customer.setId(customerId);
        customer.setFirstName("test_firstName");
        customer.setLastName("test_lastName");

        Car car = new Car();
        car.setId(1L);
        car.setModelName("test_modelName");
        car.setBrandId(1L);

        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("test_name");

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setCustomerId(customerId);
        rental.setCarId(1L);
        rental.setStartRentalDate(LocalDateTime.of(2024, 11, 1, 0, 0));
        rental.setEndRentalDate(LocalDateTime.of(2024, 11, 7, 23, 59));
        rental.setTotalPrice(700.00);
        rental.setTotalRentalPeriodDays(6);
        rental.setPickupAddress("Address 1");
        rental.setReturnAddress("Address 2");

        when(rentalRepository.findByCustomerId(customerId)).thenReturn(List.of(rental));
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        List<CustomerRentalDto> rentals = rentalService.getRentalsWithCustomerInfo(customerId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        assertNotNull(rentals);
        assertEquals(1, rentals.size());
        CustomerRentalDto dto = rentals.get(0);

        assertEquals("test_firstName", dto.getFirstName());
        assertEquals("test_lastName", dto.getLastName());
        assertEquals("test_name", dto.getName());
        assertEquals("test_modelName", dto.getModelName());
        assertEquals(700.00, dto.getTotalPrice());
        assertEquals("2024-11-01", dto.getStartRentalDate().toLocalDate().toString());
        assertEquals("2024-11-07", dto.getEndRentalDate().toLocalDate().toString());
        assertEquals(6, dto.getTotalRentalPeriodDays());
        assertEquals("Address 1", dto.getPickupAddress());
        assertEquals("Address 2", dto.getReturnAddress());
    }

    @Test
    void getPendingRentals_ShouldReturnPendingRentals() {

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setCustomerId(1L);
        rental.setCarId(1L);
        rental.setStartRentalDate(LocalDateTime.now());
        rental.setEndRentalDate(LocalDateTime.now().plusDays(5));
        rental.setPickupAddress("test_Address 1");
        rental.setReturnAddress("test_Address 2");
        rental.setReturned(false);

        Customer customer = new Customer();
        customer.setId(1L);
        customer.setFirstName("test_firstName");
        customer.setLastName("test_lastName");

        Car car = new Car();
        car.setId(1L);
        car.setModelName("test_modelName");
        car.setBrandId(1L);

        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("test_name");

        when(rentalRepository.findByIsReturnedFalse()).thenReturn(Arrays.asList(rental));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(carRepository.findById(1L)).thenReturn(Optional.of(car));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        List<PendingRentalDto> pendingRentals = rentalService.getPendingRentals();

        assertEquals(1, pendingRentals.size());
        PendingRentalDto pendingRental = pendingRentals.get(0);
        assertEquals("test_firstName", pendingRental.getFirstName());
        assertEquals("test_lastName", pendingRental.getLastName());
        assertEquals("test_name", pendingRental.getBrandName());
        assertEquals("test_modelName", pendingRental.getModelName());
        assertEquals("test_Address 1", pendingRental.getPickupAddress());
        assertEquals("test_Address 2", pendingRental.getReturnAddress());
        assertEquals(false, pendingRental.isReturned());

        verify(rentalRepository, times(1)).findByIsReturnedFalse();
        verify(customerRepository, times(1)).findById(1L);
        verify(carRepository, times(1)).findById(1L);
        verify(brandRepository, times(1)).findById(1L);
    }
    @Test
    void returnCar_ShouldThrowCarNotFoundException() {

        Rental rental = new Rental();
        rental.setId(1L);
        rental.setCarId(1L);

        when(rentalRepository.findById(1L)).thenReturn(Optional.of(rental));
        when(carRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class, () -> rentalService.returnCar(1L));

        verify(rentalRepository, times(1)).findById(1L);
        verify(carRepository, times(1)).findById(1L);
        verify(carRepository, never()).save(any());
    }

}


