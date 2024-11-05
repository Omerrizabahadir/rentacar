package com.autorent.rentacar.service;

import com.autorent.rentacar.enums.CarStatus;
import com.autorent.rentacar.enums.Color;
import com.autorent.rentacar.enums.GearBox;
import com.autorent.rentacar.exception.CarNotFoundException;
import com.autorent.rentacar.helper.CarDOFactory;
import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.repository.CarRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CarServiceTest {

    @InjectMocks
    private CarService carService;

    @Mock
    private CarRepository carRepository;

    private CarDOFactory carDOFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        this.carDOFactory = new CarDOFactory();
    }

    @Test
    void createCar_successful() {
        MockMultipartFile firstFile = new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes());

        Car car = new Car();
        car.setActive(true);
        car.setCarAvailableStock(5L);
        car.setModelName("CLIO");
        car.setDailyPrice(2000D);
        car.setCarStatus(CarStatus.AVAILABLE);
        car.setIsRented(false);
        car.setGearBox(GearBox.AUTOMATIC);
        car.setBrandId(1L);
        car.setColor(Color.WHITE);
        car.setImage("uploads/CLIO.png");

        Car savedCar = new Car();
        savedCar.setId(1L);
        savedCar.setActive(true);
        savedCar.setCarAvailableStock(5L);
        savedCar.setCarStatus(CarStatus.AVAILABLE);
        savedCar.setColor(Color.WHITE);
        savedCar.setDailyPrice(2000D);
        savedCar.setGearBox(GearBox.AUTOMATIC);
        savedCar.setBrandId(1L);
        savedCar.setIsRented(false);
        savedCar.setImage("uploads/CLIO.png"); // Düzeltme: Bu görüntü dosyasıyla tutarlı hale getirildi


        when(carRepository.save(car)).thenReturn(savedCar);

        Car response = carService.createCar(firstFile, car);

        assertNotNull(response, "Response should not be null");
        assertEquals(savedCar.getModelName(), response.getModelName(), "Model names should match");
        assertEquals(savedCar.getId(), response.getId(), "Ids should match");
        assertEquals(savedCar.getImage(), response.getImage(), "Images should match");

        verify(carRepository, times(1)).save(car); // save metodunun bir kez çağrıldığını doğruluyoruz
        verify(carRepository, times(0)).findById(car.getId()); // findById metodunun çağrılmadığını doğruluyoruz
    }

    @Test
    void createCar_fileSavingFails_throwsRuntimeException() {

        MockMultipartFile invalidFile = new MockMultipartFile("data", "filename.txt", "text/plain", "some xml".getBytes());

        Car car = new Car();
        car.setActive(true);
        car.setCarAvailableStock(5L);
        car.setModelName("CLIO");
        car.setDailyPrice(3000D);
        car.setCarStatus(CarStatus.AVAILABLE);
        car.setIsRented(false);
        car.setGearBox(GearBox.AUTOMATIC);
        car.setBrandId(1L);
        car.setColor(Color.WHITE);
        car.setImage("uploads/default_image/CLIO.png");

        CarService spyCarService = Mockito.spy(carService);
        Mockito.doThrow(new RuntimeException("File save failed")).when(spyCarService).saveFile(any(MultipartFile.class), any(String.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> spyCarService.createCar(invalidFile, car),
                "Expected createCar to throw, but it didn't");
        assertEquals("File save failed", exception.getMessage());

        Mockito.verify(carRepository, Mockito.times(0)).save(any(Car.class));
    }

    @Test
    void getCarById_successful() {
        Long carId = 1L;

        Car car = carDOFactory.getCarWithId(carId);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        Car response = carService.getCarById(carId);

        assertEquals(car.getId(), response.getId());
        assertEquals(car.getModelName(), response.getModelName());
        assertEquals(car.getDailyPrice(), response.getDailyPrice());
        assertEquals(car.getCarAvailableStock(), response.getCarAvailableStock());

        verify(carRepository, times(1)).findById(carId);
    }

    @Test
    void getCarById_fail() {
        Long carId = 1L;

        Car car = carDOFactory.getCarWithId(carId);

        when(carRepository.findById(carId)).thenReturn(Optional.ofNullable(null)); //or thenReturn(Optional.empty()

        CarNotFoundException thrown = Assertions.assertThrows(CarNotFoundException.class,
                () -> carService.getCarById(carId));

        assertEquals("Car not found id : " + carId, thrown.getMessage());
        verify(carRepository, times(1)).findById(carId);
    }

    @Test
    void getCarListByBrandId_successful() {
        Long brandId = 1L;

        Car car1 = new Car();
        car1.setId(1L);
        car1.setModelName("CLIO");
        car1.setBrandId(brandId);

        Car car2 = new Car();
        car2.setId(2L);
        car2.setModelName("FLUENCE");
        car2.setBrandId(brandId);

        List<Car> mockCarList = Arrays.asList(car1, car2);

        when(carRepository.findCarListByBrandId(brandId)).thenReturn(mockCarList);

        List<Car> result = carService.getCarListByBrandId(brandId);

        assertNotNull(result, "Result should not be null");
        assertEquals(2, result.size(), "The number of cars in the result should match the expected count");
        assertEquals("CLIO", result.get(0).getModelName(), "The first car model should be CLIO");
        assertEquals("FLUENCE", result.get(1).getModelName(), "The second car model should be FLUENCE");

        verify(carRepository, times(1)).findCarListByBrandId(brandId);
    }

    @Test
    void getCarListByBrandId_noCarsFound() {
        Long brandId = 1L;

        when(carRepository.findCarListByBrandId(brandId)).thenReturn(List.of());

        List<Car> result = carService.getCarListByBrandId(brandId);

        assertNotNull(result, "Result should not be null");
        assertTrue(result.isEmpty(), "The result list should be empty");

        verify(carRepository, times(1)).findCarListByBrandId(brandId);
    }

    @Test
    void getAllCarList_successful() {
        Long brandId = 3L;

        List<Car> carList = carDOFactory.getCarListWithId(brandId);

        when(carRepository.getAllCarList()).thenReturn(carList);

        List<Car> responseList = carService.getAllCarList();

        assertEquals(carList.size(), responseList.size());
        assertEquals(carList.get(0).getModelName(), responseList.get(0).getModelName());
        assertEquals(carList.get(0).getDailyPrice(), responseList.get(0).getDailyPrice());

        verify(carRepository, times(1)).getAllCarList();
    }

    @Test
    void getAllCarList_noCarsFound() {

        when(carRepository.getAllCarList()).thenReturn(Collections.emptyList());

        List<Car> responseList = carService.getAllCarList();

        assertNotNull(responseList, "The result should not be null");
        assertTrue(responseList.isEmpty(), "The result list should be empty");

        verify(carRepository, times(1)).getAllCarList();
    }

    @Test
    void updateCar_successful() {

        MockMultipartFile file = null;

        Car car = new Car();
        car.setId(1L);
        car.setActive(true);
        car.setCarAvailableStock(5L);
        car.setModelName("CLIO");
        car.setBrandId(1L);
        car.setImage("uploads/CLIO.png");
        car.setDailyPrice(100.0);

        Car existingCar = new Car();
        existingCar.setId(1L);
        existingCar.setActive(true);
        existingCar.setCarAvailableStock(5L);
        existingCar.setModelName("CLIO");
        existingCar.setBrandId(1L);
        existingCar.setImage("uploads/CLIO.png");
        existingCar.setDailyPrice(100.0);

        when(carRepository.findById(car.getId())).thenReturn(Optional.of(existingCar));
        when(carRepository.save(existingCar)).thenReturn(existingCar); // existingCar kaydedilmeli

        Car response = carService.updateCar(file, car);

        assertNotNull(response, "Response should not be null");
        assertEquals(existingCar.getImage(), response.getImage(), "Image path should match");
        assertEquals(existingCar.getDailyPrice(), response.getDailyPrice(), "Daily price should match");

        verify(carRepository, times(1)).findById(car.getId());
        verify(carRepository, times(1)).save(existingCar);
    }

    @Test
    void updateCar_fail() {
        MockMultipartFile file = null;

        Car car = new Car();
        car.setId(1L);

        when(carRepository.findById(car.getId())).thenReturn(Optional.empty());

        CarNotFoundException thrown = Assertions.assertThrows(CarNotFoundException.class,
                () -> carService.createCar(file, car));

        assertEquals("car not found id :1", thrown.getMessage());
        verify(carRepository, times(1)).findById(car.getId());
        verify(carRepository, never()).save(car);
    }

    @Test
    void updateCar_carNotFound() {

        MockMultipartFile file = new MockMultipartFile("file", "CLIO.png", "image/png", "some image data".getBytes());

        Car car = new Car();
        car.setId(1L);
        car.setActive(true);
        car.setCarAvailableStock(5L);
        car.setModelName("CLIO");
        car.setBrandId(1L);
        car.setImage("uploads/CLIO.png");

        when(carRepository.findById(car.getId())).thenReturn(Optional.empty());


        Exception exception = assertThrows(CarNotFoundException.class, () -> {
            carService.updateCar(file, car);
        });

        assertEquals("Product not found with id: " + car.getId(), exception.getMessage());

        verify(carRepository, times(1)).findById(car.getId());
        verify(carRepository, never()).save(any());
    }

    @Test
    void updateCar_fileIsNull() {
        MultipartFile file = null;

        Car existingCar = new Car();
        existingCar.setId(1L);
        existingCar.setActive(true);
        existingCar.setCarAvailableStock(5L);
        existingCar.setModelName("CLIO");
        existingCar.setBrandId(1L);
        existingCar.setImage("uploads/CLIO.png");

        when(carRepository.findById(existingCar.getId())).thenReturn(Optional.of(existingCar));

        Car carToUpdate = new Car();
        carToUpdate.setId(existingCar.getId());
        carToUpdate.setActive(false);
        carToUpdate.setCarAvailableStock(10L);

        when(carRepository.save(any(Car.class))).thenReturn(existingCar);

        Car response = carService.updateCar(file, carToUpdate);

        assertNotNull(response, "Response should not be null");
        assertEquals(existingCar.getImage(), response.getImage(), "Image path should be the same as existing car");
        assertEquals(carToUpdate.getCarAvailableStock(), response.getCarAvailableStock(), "Stock should be updated");

        verify(carRepository, times(1)).findById(existingCar.getId());
        verify(carRepository, times(1)).save(existingCar);
    }

    @Test
    void updateCar_fileIsEmpty() {
        MockMultipartFile file = new MockMultipartFile("file", "", "image/png", new byte[0]); // Boş dosya

        Car existingCar = new Car();
        existingCar.setId(1L);
        existingCar.setActive(true);
        existingCar.setCarAvailableStock(5L);
        existingCar.setModelName("CLIO");
        existingCar.setBrandId(1L);
        existingCar.setImage("uploads/CLIO.png"); // Mevcut resim

        // Mevcut aracı bulma mocklama
        when(carRepository.findById(existingCar.getId())).thenReturn(Optional.of(existingCar));

        // Güncellenen araç nesnesi
        Car carToUpdate = new Car();
        carToUpdate.setId(existingCar.getId());
        carToUpdate.setActive(false); // Diğer alanlarda değişiklik yapılabilir
        carToUpdate.setCarAvailableStock(10L); // Stok miktarını değiştiriyoruz

        // 'save' metodunun mocklanması
        when(carRepository.save(existingCar)).thenReturn(existingCar);

        // Testi gerçekleştiriyoruz
        Car response = carService.updateCar(file, carToUpdate);

        // Sonuçları doğruluyoruz
        assertNotNull(response, "Response should not be null");
        assertEquals(existingCar.getImage(), response.getImage(), "Image path should be the same as existing car");
        assertEquals(carToUpdate.getCarAvailableStock(), response.getCarAvailableStock(), "Stock should be updated");

        // Mock doğrulaması
        verify(carRepository, times(1)).findById(existingCar.getId());
        verify(carRepository, times(1)).save(existingCar); // Save metodu çağrılmalıdır
    }

    @Test
    void deleteCar_successful() throws IOException {
        String filePath = "uploads/test_txt";

        File file = new File(filePath);
        file.createNewFile();

        Long carId = 1L;

        Car car = new Car();
        car.setId(carId);
        car.setImage(filePath);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));
        Mockito.doNothing().when(carRepository).deleteById(carId);

        carService.deleteCar(carId);

        //db'ye gitmediği için assertEquals kullanamayız

        verify(carRepository, times(1)).findById(carId);
        verify(carRepository, times(1)).deleteById(carId);
    }

    @Test
    void deleteCar_fail() {
        Long carId = 1L;

        Car car = carDOFactory.getCarWithId(carId);
        car.setImage("test.txt");

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
                () -> carService.deleteCar(carId));

        assertEquals("IO Exception occurred while deleting image of " + car.getModelName(), thrown.getMessage());

        verify(carRepository, times(1)).findById(carId);
        verify(carRepository, never()).deleteById(carId);
    }

    @Test
    void activeOrDeActiveToRentalCar_successful() {
        Long carId = 1L;
        boolean isActive = true;

        Mockito.doNothing().when(carRepository).updateCarActive(isActive, carId);

        //metot void old. için response dönmez.Bu nedenle carService.activeOrDeActiveToRentalCar(carId, isActive); gösterdik
        carService.activeOrDeActiveToRentalCar(carId, isActive);

        verify(carRepository, times(1)).updateCarActive(isActive, carId);
    }

    @Test
    void activeOrDeActiveToRentalCar_fail() {
        Long carId = 1L;
        boolean isActive = true;

        Mockito.doThrow(new RuntimeException("Database error")).when(carRepository).updateCarActive(isActive, carId);

        assertThrows(RuntimeException.class, () -> {
            carService.activeOrDeActiveToRentalCar(carId, isActive);
        });
        verify(carRepository, times(1)).updateCarActive(isActive, carId);
    }

    @Test
    void checkIfCarRented_successful() {
        Long carId = 1L;

        Car car = new Car();
        car.setId(carId);
        car.setIsRented(false);

        when(carRepository.findById(carId)).thenReturn(Optional.of(car));

        String result = carService.checkIfCarRented(carId);

        assertEquals("car can be rented.", result);
        verify(carRepository, times(1)).findById(carId);
    }

    @Test
    void checkIfCarRented_carNotFound() {
        Long carId = 1L;

        when(carRepository.findById(carId)).thenReturn(Optional.empty());

        assertThrows(CarNotFoundException.class, () -> {
            carService.checkIfCarRented(carId);
        });

        verify(carRepository, times(1)).findById(carId);
    }
   }

