package com.autorent.rentacar.init;

import com.autorent.rentacar.enums.CarStatus;
import com.autorent.rentacar.enums.Color;
import com.autorent.rentacar.enums.GearBox;
import com.autorent.rentacar.exception.CarNotFoundException;
import com.autorent.rentacar.model.Address;
import com.autorent.rentacar.model.Brand;
import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.model.Customer;
import com.autorent.rentacar.repository.BrandRepository;
import com.autorent.rentacar.repository.CarRepository;
import com.autorent.rentacar.repository.CustomerRepository;
import com.autorent.rentacar.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;


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

    @Autowired
    private EmailService emailService;


    private static final String UPLOAD_DIR = "uploads";

    @Override
    public void run(String... args) throws Exception {
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Müşteri verisi yükleme
        loadCustomers();

        // Marka ve Araç verisi yükleme
        loadBrandsAndCars(uploadPath);
    }


    private void loadCustomers() {
        if (!customerRepository.existsByEmail("omrbahadir@gmail.com")) {
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
            adminUser.setPassword(passwordEncoder.encode("Omer03!?"));

            customerRepository.save(adminUser);
            emailService.sendWelcomeMail(adminUser.getEmail(), adminUser.getFirstName(), adminUser.getLastName());
        }

        if (!customerRepository.existsByEmail("omrbahadir@hotmail.com")) {
            Address address2 = new Address();
            address2.setCity("Ankara");
            address2.setDistrict("Çankaya/Çayyolu");
            address2.setAddressLine("A Sitesi B/blok kat:3, no:9");

            Customer clientUser = new Customer();
            clientUser.setFirstName("Ömer Rıza");
            clientUser.setLastName("Bahadır");
            clientUser.setEmail("omrbahadir@hotmail.com");
            clientUser.setPassword(passwordEncoder.encode("Omer14/!"));
            clientUser.setRoles("ROLE_USER");
            clientUser.setAddress(address2);

            customerRepository.save(clientUser);

           emailService.sendWelcomeMail(clientUser.getEmail(), clientUser.getFirstName(), clientUser.getLastName());

        }
    }

    private void loadBrandsAndCars(Path uploadPath) {
        Brand brand = brandRepository.findByName("RENAULT");
        if (brand == null) {
            brand = new Brand();
            brand.setName("RENAULT");
            brandRepository.save(brand);
        }

        // Araçları yükleme
        if (!carRepository.existsByModelName("CLIO")) {
            Car car1 = new Car();
            car1.setBrandId(brand.getId());
            car1.setModelName("CLIO");
            car1.setColor(Color.BLUE);
            car1.setDailyPrice(1100);
            car1.setCarAvailableStock(3L);
            car1.setCarStatus(CarStatus.AVAILABLE);
            car1.setActive(true);
            car1.setIsRented(false);
            car1.setMileage(11234);
            car1.setGearBox(GearBox.MANUAL);

            // CLIO için resim yükle
            String imagePath1 = saveFile("CLIO.jpeg", uploadPath);
            car1.setImage(imagePath1);
            carRepository.save(car1);
        }

        if (!carRepository.existsByModelName("FLUENCE")) {
            Car car2 = new Car();
            car2.setBrandId(brand.getId());
            car2.setModelName("FLUENCE");
            car2.setColor(Color.BLACK);
            car2.setDailyPrice(1500);
            car2.setCarAvailableStock(5L);
            car2.setCarStatus(CarStatus.AVAILABLE);
            car2.setActive(true);
            car2.setIsRented(false);
            car2.setMileage(10234);
            car2.setGearBox(GearBox.MANUAL);

            // FLUENCE için resim yükle
            String imagePath2 = saveFile("FLUENCE.jpeg", uploadPath);
            car2.setImage(imagePath2);
            carRepository.save(car2);
        }
    }


    private String saveFile(String imageName, Path uploadPath) {
        Path uploadFilePath = uploadPath.resolve(imageName);

        // Varsayılan resim dosyalarının yolu
        String sourceImagePath = "/Users/macbook/Documents/" + imageName;

        try {
            // Eğer kaynak resim mevcutsa kopyala
            Path sourcePath = Paths.get(sourceImagePath);
            if (Files.exists(sourcePath)) {
                Files.copy(sourcePath, uploadFilePath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                throw new RuntimeException("Image not found: " + sourceImagePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save image: " + imageName, e);
        }

        return uploadFilePath.toString(); // Yüklenen resmin yolunu döndür
    }
}





/*
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

    private static final String UPLOAD_DIR = "uploads";


    @Override
    public void run(String... args) throws Exception {

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        if (!customerRepository.existsByEmail("omrbahadir@gmail.com")) {
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

            customerRepository.save(adminUser);
        }

        if (!customerRepository.existsByEmail("omrbahadir@hotmail.com")) {
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

            customerRepository.save(clientUser);
        }


        if (!brandRepository.existsByName("RENAULT")) {
            Brand brand = new Brand();
            brand.setName("RENAULT");
            brandRepository.save(brand);
        }

        Brand brand = brandRepository.findByName("RENAULT");

        if (brand != null) {
            if (!carRepository.existsByModelName("CLIO")) {
                Car car1 = new Car();
                car1.setBrandId(brand.getId());
                car1.setModelName("CLIO");
                car1.setColor(Color.BLUE);
                car1.setDailyPrice(1100);
                car1.setCarAvailableStock(3L);
                car1.setCarStatus(CarStatus.AVAILABLE);
                car1.setActive(true);
                car1.setIsRented(false);
                car1.setMileage(11234);
                car1.setGearBox(GearBox.MANUAL);
                car1.setImage("uploads/CLIO.jpeg");

                carRepository.save(car1);
            }

            if (!carRepository.existsByModelName("FLUENCE")) {
                Car car2 = new Car();
                car2.setBrandId(brand.getId());
                car2.setModelName("FLUENCE");
                car2.setColor(Color.BLACK);
                car2.setDailyPrice(1500);
                car2.setCarAvailableStock(5L);
                car2.setCarStatus(CarStatus.AVAILABLE);
                car2.setActive(true);
                car2.setIsRented(false);
                car2.setMileage(10234);
                car2.setGearBox(GearBox.MANUAL);
                car2.setImage("uploads/FLUENCE.jpeg");

                carRepository.save(car2);
            }
        }
    }
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

}
*/