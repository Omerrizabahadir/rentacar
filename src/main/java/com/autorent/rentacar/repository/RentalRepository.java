package com.autorent.rentacar.repository;

import com.autorent.rentacar.model.Rental;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RentalRepository extends JpaRepository<Rental, Long> {


    List<Rental> findByCustomerId(long customerId);

    List<Rental> findByIsReturnedFalse();
}
