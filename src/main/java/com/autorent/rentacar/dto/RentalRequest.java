package com.autorent.rentacar.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RentalRequest {
    private Long customerId;
    private List<RentalCarInfo> rentalList;

}
