package com.autorent.rentacar.helper;

import com.autorent.rentacar.enums.CarStatus;
import com.autorent.rentacar.enums.Color;
import com.autorent.rentacar.enums.GearBox;
import com.autorent.rentacar.model.Car;

import java.util.ArrayList;
import java.util.List;

public class CarDOFactory {
        public List<Car> getCarListWithId(Long brandId) {
            Car car1 = new Car();
            car1.setId(1L);
            car1.setActive(true);
            car1.setCarAvailableStock(5L);
            car1.setModelName("CLIO");
            car1.setBrandId(brandId);
            car1.setImage("uploads/default_image/CLIO.txt");

            Car car2 = new Car();
            car2.setId(2L);
            car2.setActive(true);
            car2.setCarAvailableStock(2L);
            car2.setModelName("FLUENCE");
            car2.setBrandId(brandId);
            car2.setImage("uploads/default_image/FLUENCE.png");

            List<Car> carList = new ArrayList<>();

            carList.add(car1);
            carList.add(car2);

            return carList;
        }

        public Car getCarWithId(Long brandId) {
            Car car = new Car();
            car.setActive(true);
            car.setCarAvailableStock(5L);
            car.setModelName("CLIO");
            car.setDailyPrice(3000D);
            car.setBrandId(brandId);
            car.setCarStatus(CarStatus.AVAILABLE);
            car.setIsRented(false);
            car.setGearBox(GearBox.AUTOMATIC);
            car.setColor(Color.WHITE);
            car.setImage("uploads/default_image/CLIO.png");

            return car;
        }
    }

