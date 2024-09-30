package com.autorent.rentacar.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "rentals")
@Getter
@Setter
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "car_id")
    private Long carId;

    private Double totalPrice;

    @Column(name = "start_rental_date")
    private LocalDate startRentalDate;

    @Column(name = "end_rental_date")
    private LocalDate endRentalDate;

    @Column(name = "total_rental_period_days")
    private long totalRentalPeriodDays;

    @Column(name = "pickup_address")
    private String pickupAddress;

    @Column(name = "return_address")
    private String returnAddress;

    private LocalDateTime rentalDate;

    private int quantity;
}
