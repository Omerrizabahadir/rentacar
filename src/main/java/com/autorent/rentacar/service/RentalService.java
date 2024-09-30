package com.autorent.rentacar.service;

import com.autorent.rentacar.dto.RentalCarInfo;
import com.autorent.rentacar.dto.RentalRequest;
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
            rental.setStartRentalDate(rentalRequestInfo.getStartRentalDate());
            rental.setEndRentalDate(rentalRequestInfo.getEndRentalDate());
            rental.setTotalRentalPeriodDays(rentalDays);
            rental.setQuantity(1);
            rental.setRentalDate(LocalDateTime.parse(formatter.format(LocalDateTime.now()), formatter));

            rental.setPickupAddress(rentalRequestInfo.getPickupAddress());
            rental.setReturnAddress(rentalRequestInfo.getReturnAddress());

            if (car.getCarAvailableStock() - rentalRequestInfo.getQuantity() == 0) {
                car.setActiveToRental(false);
            }
            rentalRepository.save(rental);

            car.setCarAvailableStock(car.getCarAvailableStock() - 1);
            carRepository.save(car);
        });
        Customer customer = customerRepository.findById(rentalRequest.getCustomerId()).orElseThrow(() -> new CustomerNotFoundException(rentalRequest.getCustomerId() + "customer not found!"));

        return true;
    }
    private void carUnitStockCheck(List<RentalCarInfo> rentalCarInfoList) {
        rentalCarInfoList.forEach(carInfo -> {
            Car car = carRepository.findById(carInfo.getCarId())
                    .orElseThrow(() -> new CarNotFoundException("Car not found, id : " + carInfo.getCarId()));
            if (car.getCarAvailableStock() - carInfo.getQuantity() < 0) {
                throw new InsufficientCarStockException("car stock insufficient modelName : " + car.getModelName());

            }
        });
    }
}

