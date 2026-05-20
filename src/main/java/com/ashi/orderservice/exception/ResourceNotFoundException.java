package com.ashi.orderservice.exception;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException orderNotFound(UUID id) {
        return new ResourceNotFoundException("Order not found with id: " + id);
    }
}

