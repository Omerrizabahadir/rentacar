package com.autorent.rentacar.exception;

public class BrandDuplicateException extends RuntimeException{

    public BrandDuplicateException(String message){
        super(message);
    }
}
