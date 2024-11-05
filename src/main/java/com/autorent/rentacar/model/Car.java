package com.autorent.rentacar.model;

import com.autorent.rentacar.enums.CarStatus;
import com.autorent.rentacar.enums.Color;
import com.autorent.rentacar.enums.GearBox;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
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

    @Column(name = "brand_id")
    private Long brandId;

    private String modelName;

    @Enumerated(EnumType.STRING)
    @Column(name = "color")
    private Color color;

    @Enumerated(EnumType.STRING)
    @Column(name = "car_status")
    private CarStatus carStatus; //beklemede , kiralamaya hazır,rezerve edildi

    private Boolean active;

    @Column(name = "is_rented")
    private Boolean isRented;

    @Column(name = "car_available_stock")
    private Long carAvailableStock;

    @Enumerated(EnumType.STRING)
    @Column(name = "gear_box")
    private GearBox gearBox;

    private double mileage;

    @Column(name = "daily_price")
    private double dailyPrice;

    private String image;

}
