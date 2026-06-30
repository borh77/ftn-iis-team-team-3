package com.example.iisdrugcrm.exception;

public class PricelistStartDateInPastException extends RuntimeException {

    public PricelistStartDateInPastException(String message) {
        super(message);
    }
}
