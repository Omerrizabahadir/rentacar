package com.autorent.rentacar.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class RentalCarInfo {
    private Long carId;
    private  int quantity;
    private LocalDate startRentalDate;
    private LocalDate endRentalDate;
    private long rentalPeriod;
    private String pickupAddress;
    private String returnAddress;

}
