package com.autorent.rentacar.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "car")
@Getter
@Setter
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String color;

    @Column(name = "brand_id")
    private Long brandId;

    @Column(name = "car_status")
    private String carStatus; //beklemede , kiralamaya hazır,rezerve edildi

    @Column(name = "waiting_to_rental")
    private Long waitingToRental;

    @Column(name = "gear_box")
    private String gearBox;   //otomatik,manuel,yarı otomatik

    private double mileage;    //arabanın km 'si

    @Column(name = "daily_price")
    private double dailyPrice;

    private String image;
}
