package com.autorent.rentacar.repository;

import com.autorent.rentacar.model.Car;
import com.autorent.rentacar.model.Rental;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {

    @Query("SELECT c FROM Car c WHERE c.brandId = :brandId AND c.active = true")
    List<Car> findCarListByBrandId(@Param("brandId") Long brandId);

    @Query("SELECT c FROM Car c")
    List<Car> getAllCarList();

    @Query("SELECT COUNT(c) FROM Car c WHERE c.brandId = :brandId")
    Long getCountBrandOfCarByBrandId(@Param("brandId") Long brandId);

    @Modifying
    @Transactional
    @Query("UPDATE Car c SET c.active = :active WHERE c.id = :id")
    void updateCarActive(@Param("active") Boolean isActive, @Param("id") Long id);

    @Query("SELECT r from Rental r WHERE r.carId = :carId AND r.endRentalDate > :now")
    List<Rental> findActiveRentalsByCarId(@Param("carId") Long carId,
                                          @Param("now") LocalDateTime now);

    boolean existsByModelName(String modelName);

    @Query("SELECT c FROM Car c WHERE c.id = :id")
    Optional<Car> getActiveCarById(@Param("id")Long id);
}
