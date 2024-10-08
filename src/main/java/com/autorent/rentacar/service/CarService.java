package com.autorent.rentacar.service;

import com.autorent.rentacar.enums.CarStatus;
import com.autorent.rentacar.exception.CarNotFoundException;
import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;

@Service
public class CarService {

    private static final String UPLOAD_DIR = "uploads";

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private RentalService rentalService;


    public Car createCar(MultipartFile file, Car car) {
        car.setActive(true); // Araba kullanılabilir
        car.setIsRented(false); // Araba kiralanmamış
        car.setCarStatus(CarStatus.AVAILABLE);

        if (Objects.nonNull(file)) {
            String imagePath = saveFile(file, car.getModelName());
            car.setImage(imagePath);
        } else {
            Car existCar = carRepository.findById(car.getId()).orElseThrow(() -> new CarNotFoundException("car not found id :" + car.getId()));
            car.setImage(existCar.getImage());
        }
        return carRepository.save(car);
    }

    private String saveFile(MultipartFile file, String carName) {
        carName = carName.replaceAll("\\s", "");
        String fileName = carName + "." + StringUtils.getFilenameExtension(file.getOriginalFilename());
        Path uploadPath = Path.of(UPLOAD_DIR);
        Path filePath;
        try {
            Files.createDirectories(uploadPath);
            filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return filePath.toString();
    }

    public List<Car> getCarListByBrandId(Long brandId) {
        return carRepository.findCarListByBrandId(brandId);
    }

    public Car getCarById(Long id) {
        return carRepository.findById(id).orElseThrow(() -> new CarNotFoundException("Car not found id : " + id));
    }

    public List<Car> getAllCarList() {
        return carRepository.getAllCarList();
    }

    public Car updateCar(MultipartFile file, Car car) {
        if (Objects.nonNull(file)) {
            String imagePath = saveFile(file, car.getModelName());
            car.setImage(imagePath);
        } else {
            Car existCar = carRepository.findById(car.getId()).orElseThrow(() -> new CarNotFoundException("car not found id : " + car.getId()));
        }
        return carRepository.save(car);
    }

    public void deleteCar(Long id) {
        Car car = carRepository.findById(id).orElseThrow(() -> new CarNotFoundException(id + "car is not found"));
        try {
            Files.delete(Paths.get(car.getImage()));
        } catch (IOException e) {
            throw new RuntimeException("IO Exception is occured while deleting image of " + car.getModelName());
        }
        carRepository.deleteById(id);
    }

    public void activeOrDeActiveToRentalCar(Long id, boolean isActive) {

        carRepository.updateCarActive(isActive, id);
    }

    public String checkIfCarRented(Long carId) {
        Car car = carRepository.findById(carId).orElseThrow(() -> new CarNotFoundException("Car not found"));
        return car.getIsRented() ? "car has been rented." : "car can be rented.";
    }


}
