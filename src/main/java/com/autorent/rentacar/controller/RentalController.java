package com.autorent.rentacar.controller;

import com.autorent.rentacar.dto.PendingRentalDto;
import com.autorent.rentacar.dto.RentalRequest;
import com.autorent.rentacar.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rental")
public class RentalController {

    @Autowired
    private RentalService rentalService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<Boolean> rent(@RequestBody RentalRequest rentalRequest) {
        return new ResponseEntity<>(rentalService.rent(rentalRequest), HttpStatus.OK);
    }
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAuthority('ROLE_USER')")
    public ResponseEntity<List>getRentalWithCustomerInfo(@PathVariable Long customerId) {

        return new ResponseEntity<>(rentalService.getRentalsWithCustomerInfo(customerId),HttpStatus.OK);
    }
    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<PendingRentalDto>> getPendingRentals() {
        return new ResponseEntity<>(rentalService.getPendingRentals(), HttpStatus.OK);
    }

    @PutMapping("/return/{rentalId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> returnCar(@PathVariable Long rentalId) {
        rentalService.returnCar(rentalId);
        return ResponseEntity.ok().build();
    }
}

