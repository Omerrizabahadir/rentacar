package com.autorent.rentacar.dto;

import com.autorent.rentacar.model.Brand;
import com.autorent.rentacar.model.Car;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PendingRentalDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String brandName;
    private String modelName;
    private LocalDateTime startRentalDate;
    private LocalDateTime endRentalDate;
    private String pickupAddress;
    private String returnAddress;
    private boolean isReturned;
}