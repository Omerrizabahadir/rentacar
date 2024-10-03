package com.autorent.rentacar.service;

import com.autorent.rentacar.dto.RentalCarInfo;
import com.autorent.rentacar.dto.RentalRequest;
import com.autorent.rentacar.enums.CarStatus;
import com.autorent.rentacar.exception.CarAlreadyRentedException;
import com.autorent.rentacar.exception.CarNotFoundException;
import com.autorent.rentacar.exception.CustomerNotFoundException;
import com.autorent.rentacar.exception.InsufficientCarStockException;
import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.model.Customer;
import com.autorent.rentacar.model.Rental;
import com.autorent.rentacar.repository.CarRepository;
import com.autorent.rentacar.repository.CustomerRepository;
import com.autorent.rentacar.repository.RentalRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class RentalService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RentalRepository rentalRepository;

   
    public void checkCarAvailability(Long carId, boolean isActive) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new CarNotFoundException("Car not found"));

        // Mevcut stok kontrolü
        if (car.getCarAvailableStock() < 0) {
            throw new InsufficientCarStockException("No available stock for this car.");
        }

        List<Rental> activeRentals = carRepository.findActiveRentalsByCarId(carId, LocalDateTime.now());

        // Eğer araç kiralanıyorsa ve aktif kiralamalar varsa hata fırlat
        if (!activeRentals.isEmpty() && isActive) {
            throw new CarAlreadyRentedException("The car you chose was rented by someone else");
        }
    }

    private void productUnitStockCheck(List<RentalCarInfo> rentalCarInfoList) {
        rentalCarInfoList.forEach(carInfo -> {
            Car car = carRepository.findById(carInfo.getCarId())
                    .orElseThrow(() -> new CarNotFoundException("car not found, id : " + carInfo.getCarId()));

            if (car.getCarAvailableStock() - carInfo.getQuantity() < 0) {
                log.error("the car stock insufficient id : " + carInfo.getCarId());
                throw new InsufficientCarStockException("the car stock insufficient productName : " + car.getModelName());
            }
        });
    }

    public Boolean rent(RentalRequest rentalRequest) {
        log.info("Rental request time {} customer :{}", LocalDateTime.now(), rentalRequest.getCustomerId());
        carUnitStockCheck(rentalRequest.getRentalList());

        List<Double> rentalTotalCostList = new ArrayList<>();
        rentalRequest.getRentalList().forEach(rentalRequestInfo -> {

            Rental rental = new Rental();
            Car car = carRepository.findById(rentalRequestInfo.getCarId()).orElseThrow(() -> new CarNotFoundException("car not found, id : " + rentalRequestInfo.getCarId()));
            Long rentalDays = ChronoUnit.DAYS.between(rentalRequestInfo.getStartRentalDate(), rentalRequestInfo.getEndRentalDate());
            Double totalPrice = rentalDays * car.getDailyPrice();
            rentalTotalCostList.add(totalPrice);
            rental.setTotalPrice(totalPrice);

            rental.setCarId(rentalRequestInfo.getCarId());
            rental.setCustomerId(rentalRequest.getCustomerId());
            rental.setStartRentalDate(LocalDateTime.parse(formatter.format(LocalDateTime.now()), formatter));
            rental.setEndRentalDate(LocalDateTime.parse(formatter.format(LocalDateTime.now()), formatter));
            rental.setTotalRentalPeriodDays(rentalDays);
            rental.setQuantity(1);

            rental.setPickupAddress(rentalRequestInfo.getPickupAddress());
            rental.setReturnAddress(rentalRequestInfo.getReturnAddress());

            if (car.getCarAvailableStock() - rentalRequestInfo.getQuantity() == 0) {
                car.setIsRented(false);
            }
            rentalRepository.save(rental);

            car.setCarAvailableStock(car.getCarAvailableStock());
            carRepository.save(car);
        });
        Customer customer = customerRepository.findById(rentalRequest.getCustomerId()).orElseThrow(() -> new CustomerNotFoundException(rentalRequest.getCustomerId() + "customer not found!"));

        return true;
    }

    public void carUnitStockCheck(List<RentalCarInfo> rentalCarInfoList) {
        rentalCarInfoList.forEach(carInfo -> {
            Car car = carRepository.findById(carInfo.getCarId())
                    .orElseThrow(() -> new CarNotFoundException("Car not found, id : " + carInfo.getCarId()));
            if (car.getCarAvailableStock() - carInfo.getQuantity() == 0) {
                car.setIsRented(true);
                car.setActive(false);
                car.setCarStatus(CarStatus.RENTED);
                car.setCarAvailableStock(car.getCarAvailableStock() - carInfo.getQuantity());
            }
            carRepository.save(car);
        });
    }

}

