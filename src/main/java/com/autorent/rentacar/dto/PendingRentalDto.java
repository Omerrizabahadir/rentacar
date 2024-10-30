package com.autorent.rentacar.dto;

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
    private String modelName;
    private Double totalPrice;
    private LocalDateTime startRentalDate;
    private LocalDateTime endRentalDate;
    private String pickupAddress;
    private String returnAddress;
    private Long totalPeriodDays;
    private boolean isReturned;
}