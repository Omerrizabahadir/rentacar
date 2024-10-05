package com.autorent.rentacar.repository;

import com.autorent.rentacar.model.Rental;
import org.springframework.data.repository.CrudRepository;

public interface RentalRepository extends CrudRepository<Rental, Long> {


}
