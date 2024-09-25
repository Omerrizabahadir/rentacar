package com.autorent.rentacar.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginDto {

    private String token;

    private Long customerId;
}
