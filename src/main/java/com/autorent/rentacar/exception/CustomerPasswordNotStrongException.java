package com.autorent.rentacar.exception;

public class CustomerPasswordNotStrongException extends RuntimeException{

    public CustomerPasswordNotStrongException(String message){
        super(message);
    }
}
