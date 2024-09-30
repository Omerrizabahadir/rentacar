package com.autorent.rentacar.exception;

public class InsufficientCarStockException extends RuntimeException{

    public InsufficientCarStockException(String message){
        super(message);
    }
}
