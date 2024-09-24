package com.autorent.rentacar.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "rental")
@Getter
@Setter
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_name")
    private Long customerId;

    @Column(name = "rental_id")
    private Long rentalId;

    private Double price;

    private Double totalPrice;

    private LocalDate startDate;

    private LocalDate endDate;

    @Column(name = "rental_period")
    private int rentalPeriod;

}
