package com.autorent.rentacar.exception;

public class CarAlreadyRentedException extends RuntimeException{
    public CarAlreadyRentedException(String message){
        super(message);
    }
}
