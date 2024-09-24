package com.autorent.rentacar.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class Address {

    private String city;
    private String district;
    private String pickupAddress;
    private String returnAddress;
}
