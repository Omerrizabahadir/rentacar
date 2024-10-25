package com.autorent.rentacar.service;

import com.autorent.rentacar.dto.CustomerRentalDto;
import com.autorent.rentacar.dto.RentalCarInfo;
import com.autorent.rentacar.dto.RentalRequest;
import com.autorent.rentacar.enums.CarStatus;
import com.autorent.rentacar.exception.CarAlreadyRentedException;
import com.autorent.rentacar.exception.CarNotFoundException;
import com.autorent.rentacar.exception.CustomerNotFoundException;
import com.autorent.rentacar.exception.InsufficientCarStockException;
import com.autorent.rentacar.model.Brand;
import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.model.Customer;
import com.autorent.rentacar.model.Rental;
import com.autorent.rentacar.repository.BrandRepository;
import com.autorent.rentacar.repository.CarRepository;
import com.autorent.rentacar.repository.CustomerRepository;
import com.autorent.rentacar.repository.RentalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

            LocalDateTime startRentalDate = LocalDateTime.now();
            rental.setStartRentalDate(startRentalDate);

            long rentalDays = rentalRequestInfo.getRentalPeriod();
            LocalDateTime userEndRentalDate = startRentalDate.plusDays(rentalDays);
            rental.setEndRentalDate(userEndRentalDate);

            double totalPrice = rentalDays * car.getDailyPrice();
            rentalTotalCostList.add(totalPrice);
            rental.setTotalPrice(totalPrice);

            rental.setCarId(rentalRequestInfo.getCarId());
            rental.setCustomerId(rentalRequest.getCustomerId());
            rental.setQuantity(rentalRequestInfo.getQuantity());
            rental.setTotalRentalPeriodDays(rentalDays);
            rental.setPickupAddress(rentalRequestInfo.getPickupAddress());
            rental.setReturnAddress(rentalRequestInfo.getReturnAddress());

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
                return new CustomerRentalDto(
                        rental.getId(),
                        rental.getCustomerId(),
                        customer.get().getFirstName(),
                        customer.get().getLastName(),
                        brand.map(Brand::getName).orElse("Unknown"),
                        car.get().getModelName()
                );
            } else {
                return new CustomerRentalDto(rental.getId(), rental.getCustomerId(), "Unknown", "Unknown", "Unknown", "Unknown");
            }
        }).collect(Collectors.toList());
    }

}