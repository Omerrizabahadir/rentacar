package com.autorent.rentacar.controller;

import com.autorent.rentacar.dto.RentalRequest;
import com.autorent.rentacar.service.RentalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}