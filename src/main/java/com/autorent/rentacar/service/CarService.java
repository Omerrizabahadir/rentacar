package com.autorent.rentacar.service;

import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;

    public Car createCar(Car car){
        return carRepository.save(car);
    }

}
