package com.autorent.rentacar.service;

import com.autorent.rentacar.enums.CarStatus;
import com.autorent.rentacar.exception.CarNotFoundException;
import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.repository.CarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
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

        if (Objects.nonNull(file)) {
            String imagePath = saveFile(file, car.getModelName());
            car.setActive(true);
            car.setIsRented(false);
            car.setCarStatus(CarStatus.AVAILABLE);
            car.setImage(imagePath);
        } else {
            Car existCar = carRepository.findById(car.getId()).orElseThrow(() -> new CarNotFoundException("car not found id :" + car.getId()));
            car.setImage(existCar.getImage());
        }
        return carRepository.save(car);
    }

    protected String saveFile(MultipartFile file, String carName) {
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
       Car existingcar = carRepository.findById(car.getId())
               .orElseThrow(() -> new CarNotFoundException("Product not found with id: " + car.getId()));

       if (file != null && !file.isEmpty()) {
           car.setActive(car.getActive());
           car.setIsRented(car.getIsRented());
           car.setCarStatus(car.getCarStatus());

           String imagePath = saveFile(file, car.getModelName());
           car.setImage(imagePath);
       } else {
           car.setImage(existingcar.getImage());
       }

       existingcar.setId(car.getId());
       existingcar.setBrandId(car.getBrandId());
       existingcar.setModelName(car.getModelName());
       existingcar.setColor(car.getColor());
       existingcar.setCarStatus(car.getCarStatus());
       existingcar.setActive(car.getActive());
       existingcar.setIsRented(car.getIsRented());
       existingcar.setCarAvailableStock(car.getCarAvailableStock());
       existingcar.setGearBox(car.getGearBox());
       existingcar.setMileage(car.getMileage());
       existingcar.setDailyPrice(car.getDailyPrice());

       return carRepository.save(existingcar);
   }

    public void deleteCar(Long id) {
        Car car = carRepository.findById(id).orElseThrow(() -> new CarNotFoundException(id + " car is not found"));
        try {

            Files.delete(Paths.get(car.getImage()));
        } catch (IOException e) {
            throw new RuntimeException("IO Exception occurred while deleting image of " + car.getModelName(), e);
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

    public List<String> getCarImages() {
        File dir = new File(UPLOAD_DIR);
        String[] images = dir.list((d, name) -> name.endsWith(".jpg") || name.endsWith(".png"));
        return images != null ? Arrays.asList(images) : List.of();
    }
}
