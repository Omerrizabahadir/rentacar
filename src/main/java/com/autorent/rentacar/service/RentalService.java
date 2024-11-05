package com.autorent.rentacar.service;

import com.autorent.rentacar.dto.CustomerRentalDto;
import com.autorent.rentacar.dto.PendingRentalDto;
import com.autorent.rentacar.dto.RentalCarInfo;
import com.autorent.rentacar.dto.RentalRequest;
import com.autorent.rentacar.enums.CarStatus;
import com.autorent.rentacar.exception.*;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RentalService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private RentalRepository rentalRepository;
 @Autowired
    private JavaMailSender mailSender;
    @Value("${spring.mail.username}")
    private String emailFrom;
    @Autowired
    private BrandRepository brandRepository;

    public void checkCarAvailability(Long carId, boolean isActive) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException("Car not found"));

        // Mevcut stok kontrolü
        if (car.getCarAvailableStock() <= 0) {
            throw new InsufficientCarStockException("No available stock for this car.");
        }

        List<Rental> activeRentals = carRepository.findActiveRentalsByCarId(carId, LocalDateTime.now());

        // Eğer araç kiralanıyorsa ve aktif kiralamalar varsa hata fırlat
        if (!activeRentals.isEmpty() && isActive) {
            throw new CarAlreadyRentedException("The car you chose was rented by someone else");
        }
    }

    public Boolean rent(RentalRequest rentalRequest) {
        log.info("Rental request time {} customer :{}", LocalDateTime.now(), rentalRequest.getCustomerId());

        for (RentalCarInfo rentalCarInfo : rentalRequest.getRentalList()) {
            Car car = carRepository.findById(rentalCarInfo.getCarId())
                    .orElseThrow(() -> new CarNotFoundException("Car not found, id : " + rentalCarInfo.getCarId()));

            if (car.getIsRented()) {
                throw new InsufficientCarStockException("The car with id " + rentalCarInfo.getCarId() + " is already rented.");
            }
        }

        carUnitStockCheck(rentalRequest.getRentalList());

        List<Double> rentalTotalCostList = new ArrayList<>();
        rentalRequest.getRentalList().forEach(rentalRequestInfo -> {
            Rental rental = new Rental();
            Car car = carRepository.findById(rentalRequestInfo.getCarId())
                    .orElseThrow(() -> new CarNotFoundException("Car not found, id : " + rentalRequestInfo.getCarId()));

            // Start rental date is now set from customer's input
            LocalDateTime startRentalDate = rentalRequestInfo.getStartRentalDate();
            if (startRentalDate == null) {
                throw new IllegalArgumentException("Start rental date cannot be null for car ID: " + rentalRequestInfo.getCarId());
            }
            // Start rental date is set to now
            rental.setStartRentalDate(startRentalDate);

            // endRentalDate, her döngüde rentalRequestInfo'dan alınıyor
            LocalDateTime endRentalDate = rentalRequestInfo.getEndRentalDate();
            if (endRentalDate == null) {
                throw new IllegalArgumentException("End rental date cannot be null for car ID: " + rentalRequestInfo.getCarId());
            }
            rental.setEndRentalDate(endRentalDate);

            long rentalDays = ChronoUnit.DAYS.between(startRentalDate, endRentalDate);
            rental.setTotalRentalPeriodDays(rentalDays);

            double totalPrice = rentalDays * car.getDailyPrice();
            rentalTotalCostList.add(totalPrice);
            rental.setTotalPrice(totalPrice);

            rental.setCarId(rentalRequestInfo.getCarId());
            rental.setCustomerId(rentalRequest.getCustomerId());
            rental.setQuantity(rentalRequestInfo.getQuantity());
            rental.setPickupAddress(rentalRequestInfo.getPickupAddress());
            rental.setReturnAddress(rentalRequestInfo.getReturnAddress());
            rental.setReturned(false);

            if (car.getCarAvailableStock() == 0) {
                car.setIsRented(true);
                car.setActive(false);
                car.setCarStatus(CarStatus.RENTED);
            }
            rentalRepository.save(rental);
            carRepository.save(car);
        });

        Customer customer = customerRepository.findById(rentalRequest.getCustomerId())
                .orElseThrow(() -> new CustomerNotFoundException(rentalRequest.getCustomerId() + " customer not found!"));


        Double orderTotalCost = rentalTotalCostList.stream().mapToDouble(Double::doubleValue).sum();
        sendEmail(customer.getEmail(), customer.getFirstName(), orderTotalCost);



        return true;
    }

    private void carUnitStockCheck(List<RentalCarInfo> rentalCarInfoList) {
        rentalCarInfoList.forEach(carInfo -> {
            Car car = carRepository.findById(carInfo.getCarId())
                    .orElseThrow(() -> new CarNotFoundException("Car not found, id : " + carInfo.getCarId()));
            if (car.getCarAvailableStock() - carInfo.getQuantity() == 0) {
                car.setIsRented(true);
                car.setActive(false);
                car.setCarStatus(CarStatus.RENTED);
                car.setCarAvailableStock(car.getCarAvailableStock() - carInfo.getQuantity());
            } else if (car.getCarAvailableStock() - carInfo.getQuantity() > 0) {
                car.setCarAvailableStock(car.getCarAvailableStock() - carInfo.getQuantity());
            }
            carRepository.save(car);
        });
    }

    public List<CustomerRentalDto> getRentalsWithCustomerInfo(Long customerId) {
        List<Rental> rentals = rentalRepository.findByCustomerId(customerId);
        return rentals.stream().map(rental -> {
            Optional<Customer> customer = customerRepository.findById(rental.getCustomerId());
            Optional<Car> car = carRepository.findById(rental.getCarId());

            if (customer.isPresent() && car.isPresent()) {
                Long brandId = car.get().getBrandId();
                Optional<Brand> brand = brandRepository.findById(brandId);

                // Tarihleri biçimlendirilmiş şekilde ayarlıyoruz
                String formattedStartDate = rental.getStartRentalDate() != null ? rental.getStartRentalDate().format(formatter) : "Bilinmiyor";
                String formattedEndDate = rental.getEndRentalDate() != null ? rental.getEndRentalDate().format(formatter) : "Bilinmiyor";

                return new CustomerRentalDto(
                        rental.getId(),
                        rental.getCustomerId(),
                        customer.get().getFirstName(),
                        customer.get().getLastName(),
                        brand.map(Brand::getName).orElse("Unknown"),
                        car.get().getModelName(),
                        rental.getTotalPrice(),
                        rental.getStartRentalDate(),
                        rental.getEndRentalDate(),
                        rental.getTotalRentalPeriodDays(),
                        rental.getPickupAddress(),
                        rental.getReturnAddress()

                );
            } else {
                return new CustomerRentalDto(rental.getId(),
                        rental.getCustomerId(),
                        "Unknown",
                        "Unknown",
                        "Unknown",
                        "Unknown",
                        null,
                        null,
                        null,
                        rental.getTotalRentalPeriodDays(),
                        "Unknown",
                        "Unknown"
                );
            }
        }).collect(Collectors.toList());
    }

    // Teslim edilmemiş kiralamaları al
    public List<PendingRentalDto> getPendingRentals() {
        return rentalRepository.findByIsReturnedFalse().stream().map(rental -> {
            Customer customer = customerRepository.findById(rental.getCustomerId())
                    .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

            Car car = carRepository.findById(rental.getCarId())
                    .orElseThrow(() -> new CarNotFoundException("Car not found"));

            String brandName = brandRepository.findById(car.getBrandId())
                    .map(Brand::getName).orElse("Unknown");

            String formattedStartDate = rental.getStartRentalDate() != null ? rental.getStartRentalDate().format(formatter) : "Bilinmiyor";
            String formattedEndDate = rental.getEndRentalDate() != null ? rental.getEndRentalDate().format(formatter) : "Bilinmiyor";


            return new PendingRentalDto(
                    rental.getId(),
                    customer.getFirstName(),
                    customer.getLastName(),
                    brandName,
                    car.getModelName(),
                    rental.getStartRentalDate(),
                    rental.getEndRentalDate(),
                    rental.getPickupAddress(),
                    rental.getReturnAddress(),
                    rental.isReturned()
            );
        }).collect(Collectors.toList());
    }

    // Araç teslim alma işlemi
    public void returnCar(Long rentalId) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new RentalNotFoundException("Rental not found"));

        Car car = carRepository.findById(rental.getCarId())
                .orElseThrow(() -> new CarNotFoundException("Car not found"));

        car.setIsRented(false); // Araç teslim alındı
        car.setCarAvailableStock(car.getCarAvailableStock() + rental.getQuantity());
        car.setCarStatus(CarStatus.AVAILABLE);
        carRepository.save(car);
        car.setActive(true);

        rental.setReturned(true); // Kiralama durumu güncellendi
        rentalRepository.save(rental);
    }

    public void sendEmail(String emailTo, String firstName, double totalCost) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper;

        try {
            helper = new MimeMessageHelper(message, true);
            helper.setFrom(emailFrom, "Autorent");
            helper.setTo(emailTo); // Burayı emailTo ile değiştirin
            helper.setSubject("Merhaba " + firstName + ", Kiralama İsteğiniz İşleme Alındı");

            String content = "<p>Merhaba " + firstName + "</p><p>Kiralamanızın toplam maliyeti: " + totalCost + "</p>";
            helper.setText(content, true);

            mailSender.send(message);
            log.info("E-posta {} adresine gönderildi", emailTo);
        } catch (MessagingException e) {
            log.error("E-posta {} adresine gönderilemedi: {}", emailTo, e.getMessage());
            throw new RuntimeException("E-posta gönderilemedi", e);
        } catch (UnsupportedEncodingException e) {
            log.error("E-posta kodlama hatası: {}", e.getMessage());
            throw new RuntimeException("E-posta kodlama hatası", e);
        }
    }
}


