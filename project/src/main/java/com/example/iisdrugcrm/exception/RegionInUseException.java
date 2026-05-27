package com.example.iisdrugcrm.exception;

public class RegionInUseException extends RuntimeException {

    public RegionInUseException(String message) {
        super(message);
    }
}