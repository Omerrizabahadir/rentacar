package com.autorent.rentacar.exception;

public class BrandNotFoundException extends RuntimeException{
    public BrandNotFoundException(String message){
        super (message);
    }
}
