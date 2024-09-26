package com.autorent.rentacar.controller;

import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.service.CarService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/car")
@RequiredArgsConstructor
public class CarController {

    private final CarService carService;

    @PostMapping("/create")
    public Car createCar(@RequestBody Car car){
        return carService.createCar(car);
    }
}
