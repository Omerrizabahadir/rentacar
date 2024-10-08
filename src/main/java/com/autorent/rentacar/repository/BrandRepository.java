package com.autorent.rentacar.repository;

import com.autorent.rentacar.model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand,Long> {


    Optional<Brand> findBrandByName(String name);

    boolean existsByName(String name);
    Brand findByName(String name);
}
