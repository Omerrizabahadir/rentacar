package com.autorent.rentacar.init;

import com.autorent.rentacar.enums.CarStatus;
import com.autorent.rentacar.enums.Color;
import com.autorent.rentacar.enums.GearBox;
import com.autorent.rentacar.model.Address;
import com.autorent.rentacar.model.Brand;
import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.model.Customer;
import com.autorent.rentacar.repository.BrandRepository;
import com.autorent.rentacar.repository.CarRepository;
import com.autorent.rentacar.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Override
    public void run(String... args) throws Exception {

        Address address1 = new Address();
        address1.setCity("Ankara");
        address1.setAddressLine("Örnek Sitesi Örnek Blokları kat:2,no:6");
        address1.setDistrict("Çankaya/Çayyolu");

        Customer adminUser = new Customer();
        adminUser.setFirstName("Ömer Rıza");
        adminUser.setLastName("Bahadır");
        adminUser.setRoles("ROLE_ADMIN");
        adminUser.setAddress(address1);
        adminUser.setEmail("omrbahadir@gmail.com");
        adminUser.setPassword(passwordEncoder.encode("123456"));


        Address address2 = new Address();
        address2.setCity("Ankara");
        address2.setDistrict("Çankaya/Çayyolu");
        address2.setAddressLine("A Sitesi B/blok kat:3, no:9");

        Customer clientUser = new Customer();
        clientUser.setFirstName("Ömer Rıza");
        clientUser.setLastName("Bahadır");
        clientUser.setEmail("omrbahadir@hotmail.com");
        clientUser.setPassword(passwordEncoder.encode("123456"));
        clientUser.setRoles("ROLE_USER");
        clientUser.setAddress(address2);

        customerRepository.saveAll(Arrays.asList(adminUser, clientUser));

        Brand brand = new Brand();
        brand.setName("Renault");
        brandRepository.save(brand);


        Car car1 = new Car();
        car1.setBrandId(brand.getId());
        car1.setModelName("clio");
        car1.setColor(Color.BLUE);
        car1.setDailyPrice(1100);
        car1.setCarAvailableStock(3L);
        car1.setCarStatus(CarStatus.AVAILABLE);
        car1.setActive(true);
        car1.setIsRented(false);
        car1.setMileage(11234);
        car1.setGearBox(GearBox.MANUAL);
        car1.setImage("uploads/clio.jpeg");

        Car car2 = new Car();
        car2.setBrandId(brand.getId());
        car2.setModelName("fluence");
        car2.setColor(Color.BLACK);
        car2.setDailyPrice(1500);
        car2.setCarAvailableStock(5L);
        car2.setCarStatus(CarStatus.AVAILABLE);
        car2.setActive(true);
        car2.setIsRented(false);
        car2.setMileage(10234);
        car2.setGearBox(GearBox.MANUAL);
        car2.setImage("uploads/fluence.jpeg");

        carRepository.saveAll(Arrays.asList(car1, car2));
    }
}
