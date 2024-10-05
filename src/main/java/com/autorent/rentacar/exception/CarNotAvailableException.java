package com.autorent.rentacar.exception;

public class CarNotAvailableException extends RuntimeException{
    public CarNotAvailableException(String message){
        super(message);
    }
}
