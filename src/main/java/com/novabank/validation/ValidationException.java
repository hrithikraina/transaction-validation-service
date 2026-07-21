package com.novabank.validation;

class ValidationException extends RuntimeException {
    private final String errorCode;

    ValidationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    String errorCode() {
        return errorCode;
    }
}
