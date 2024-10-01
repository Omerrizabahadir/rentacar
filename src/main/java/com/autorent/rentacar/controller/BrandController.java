package com.autorent.rentacar.controller;

import com.autorent.rentacar.model.Brand;
import com.autorent.rentacar.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/brand")
@RequiredArgsConstructor
public class BrandController {
    private final BrandService brandService;

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Brand> createBrand(@RequestBody Brand brand){
        return new ResponseEntity<>(brandService.createBrand(brand), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<List<Brand>> getAllBrandList(){
        return new ResponseEntity<>(brandService.getAllBrandList(),HttpStatus.OK);
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')")
    public ResponseEntity<Brand> getBrand(@PathVariable("id")Long id){
        return new ResponseEntity<>(brandService.getBrand(id),HttpStatus.OK);
    }
    @PutMapping("/update")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Brand> updateBrand(@RequestBody Brand brand){
        return new ResponseEntity<>(brandService.updateBrand(brand),HttpStatus.CREATED);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteBrand(@PathVariable("id") Long id){
        brandService.deleteBrand(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
