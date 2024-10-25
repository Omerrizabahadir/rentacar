package com.autorent.rentacar.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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

}
