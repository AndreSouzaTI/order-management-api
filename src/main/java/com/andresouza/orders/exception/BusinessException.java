package com.andresouza.orders.exception;

/** Violação de regra de negócio (HTTP 422). */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
