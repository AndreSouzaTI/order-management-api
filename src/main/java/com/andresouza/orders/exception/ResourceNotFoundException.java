package com.andresouza.orders.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Object id) {
        super("%s %s não encontrado".formatted(resource, id));
    }
}
