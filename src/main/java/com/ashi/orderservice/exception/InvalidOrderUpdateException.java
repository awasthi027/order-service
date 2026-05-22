package com.ashi.orderservice.exception;

public class InvalidOrderUpdateException extends RuntimeException {

    public InvalidOrderUpdateException(String message) {
        super(message);
    }
}

