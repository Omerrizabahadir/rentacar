package com.autorent.rentacar.service;

import com.autorent.rentacar.exception.BrandDeleteException;
import com.autorent.rentacar.exception.BrandDuplicateException;
import com.autorent.rentacar.exception.BrandNotFoundException;
import com.autorent.rentacar.helper.BrandDOFactory;
import com.autorent.rentacar.model.Brand;
import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.repository.BrandRepository;
import com.autorent.rentacar.repository.CarRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BrandServiceTest {

    @InjectMocks
    private BrandService brandService;

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private CarRepository carRepository;

    private BrandDOFactory brandDOFactory;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.brandDOFactory = new BrandDOFactory();
    }

    @Test
    void createBrand_successful() {
        Brand brand = brandDOFactory.getBrandWithoutId();

        Brand savedBrand = brandDOFactory.getBrandWithId(1L);

        when(brandRepository.findBrandByName(brand.getName())).thenReturn(Optional.empty());
        when(brandRepository.save(brand)).thenReturn(savedBrand);

        Brand response = brandService.createBrand(brand);

        assertEquals(brand.getName(), response.getName());
        verify(brandRepository, times(1)).findBrandByName(brand.getName());
        verify(brandRepository, times(1)).save(brand);
    }

    @Test
    void createBrand_fail() {

        Brand brand = brandDOFactory.getBrandWithoutId();

        Brand savedBrand = brandDOFactory.getBrandWithId(1L);

        when(brandRepository.findBrandByName(brand.getName())).thenReturn(Optional.ofNullable(savedBrand));

        BrandDuplicateException thrown = Assertions.assertThrows(BrandDuplicateException.class,
                () -> brandService.createBrand(brand));

        assertEquals("Brand name is already defined: " + brand.getName(), thrown.getMessage());
        verify(brandRepository, times(1)).findBrandByName(brand.getName());
        verify(brandRepository, times(0)).save(brand);
    }

    @Test
    void getAllBrandList_successful() {

        List<Brand> brandList = brandDOFactory.getBrandListWithId();

        when(brandRepository.findAll()).thenReturn(brandList);

        List<Brand> response = brandService.getAllBrandList();

        assertEquals(brandList.size(), response.size());
        assertEquals(brandList.get(0).getName(), response.get(0).getName());
        assertEquals(brandList.get(1).getName(), response.get(1).getName());
        verify(brandRepository, times(1)).findAll();
    }

    @Test
    void getAllBrandList_emptyList_fail() {

        when(brandRepository.findAll()).thenReturn(Collections.emptyList());

        List<Brand> brandList = brandService.getAllBrandList();

        assertNotNull(brandList);// brandList'in null olmaması gerek
        assertTrue(brandList.isEmpty());// brandList'in boş olması gerek
        assertEquals(0, brandList.size(), "List should be empty");
    }

    @Test
    void getBrand_successful() {
        Long brandId = 1L;

        Brand brand = brandDOFactory.getBrandWithId(1L);

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));

        Brand response = brandService.getBrand(brandId);

        assertEquals(brandId, response.getId());
        assertEquals(brand.getName(), response.getName());
        verify(brandRepository, times(1)).findById(brandId);
    }

    @Test
    void getBrand_fail() {
        Long brandId = 1L;

        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        BrandNotFoundException thrown = Assertions.assertThrows(BrandNotFoundException.class,
                () -> brandService.getBrand(brandId));

        assertEquals("Brand not found id : 1", thrown.getMessage());
        verify(brandRepository, times(1)).findById(brandId);
    }

    @Test
    void updateBrand_successful() {
        Brand brand = brandDOFactory.getBrandWithId(1L);

        when(brandRepository.save(brand)).thenReturn(brand);

        Brand response = brandService.updateBrand(brand);

        assertEquals(brand.getId(), response.getId());
        assertEquals(brand.getName(), response.getName());
        verify(brandRepository, times(1)).save(brand);
    }

    @Test
    void deleteBrand_successful() {
        Long brandId = 3L;

        when(carRepository.getCountBrandOfCarByBrandId(brandId)).thenReturn(0L);

        brandService.deleteBrand(brandId);

        //herhangi bir şey dönmediği için assertEquals olmaz
        verify(carRepository, times(1)).getCountBrandOfCarByBrandId(brandId);
        verify(brandRepository, times(1)).deleteById(brandId);
    }

    @Test
    void deleteBrand_fail() {

        Long brandId = 3L;

        when(carRepository.getCountBrandOfCarByBrandId(brandId)).thenReturn(1L);

        BrandDeleteException thrown = Assertions.assertThrows(BrandDeleteException.class,
                () -> brandService.deleteBrand(brandId));

        assertEquals("You can not delete this brand because brand has 1car(s)", thrown.getMessage());
        verify(carRepository, times(1)).getCountBrandOfCarByBrandId(brandId);
        verify(brandRepository, times(0)).deleteById(brandId);
    }

    @Test
    void getCarsByBrandId_success() {
        Long brandId = 1L;
        Brand brand = new Brand();
        brand.setId(brandId);
        brand.setName("TEST_BRAND");

        Car car = new Car();
        car.setBrandId(brandId);
        car.setModelName("TEST_CAR");

        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));
        when(carRepository.findCarsByBrandId(brandId)).thenReturn(Collections.singletonList(car));

        List<Car> cars = brandService.getCarsByBrandId(brandId);

        assertNotNull(cars);
        assertEquals(1, cars.size());
        assertEquals("TEST_CAR", cars.get(0).getModelName());
        verify(brandRepository, times(1)).findById(brandId);
        verify(carRepository, times(1)).findCarsByBrandId(brandId);
    }

    @Test
    void getCarsByBrandId_fail() {
        Long brandId = 1L;

        when(brandRepository.findById(brandId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(BrandNotFoundException.class, () -> {
            brandService.getCarsByBrandId(brandId);
        });

        assertEquals("Brand not found with id: " + brandId, exception.getMessage());
        verify(brandRepository, times(1)).findById(brandId);
    }

    @Test
    void getCarsByBrandId_shouldReturnCars() {
        Long brandId = 1L;

        Brand brand = new Brand();
        brand.setId(brandId);
        brand.setName("TEST_BRAND");

        // Mocking repository behavior
        when(brandRepository.findById(brandId)).thenReturn(Optional.of(brand));

        Car car1 = new Car();
        car1.setBrandId(brandId);
        car1.setModelName("TEST_CAR_1");

        Car car2 = new Car();
        car2.setBrandId(brandId);
        car2.setModelName("TEST_CAR_2");

        // Mocking car repository behavior
        when(carRepository.findCarsByBrandId(brandId)).thenReturn(List.of(car1, car2));

        // Method call
        List<Car> cars = brandService.getCarsByBrandId(brandId);

        // Assertions
        assertEquals(2, cars.size());
        assertEquals("TEST_CAR_1", cars.get(0).getModelName());
        assertEquals("TEST_CAR_2", cars.get(1).getModelName());

        // Verify the interactions
        verify(brandRepository, times(1)).findById(brandId);
        verify(carRepository, times(1)).findCarsByBrandId(brandId);
    }
}

