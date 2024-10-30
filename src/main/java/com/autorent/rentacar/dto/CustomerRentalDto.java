package com.autorent.rentacar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CustomerRentalDto {
    private Long rentalId;
    private Long customerId;
    private String firstName;
    private String lastName;
    private String name;
    private String modelName;
    private Double totalPrice;
    private LocalDateTime startRentalDate;
    private LocalDateTime endRentalDate;
    private long totalRentalPeriodDays;
    private String pickupAddress;
    private String returnAddress;

}
