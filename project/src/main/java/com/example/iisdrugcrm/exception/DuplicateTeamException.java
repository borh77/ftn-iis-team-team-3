package com.example.iisdrugcrm.exception;

public class DuplicateTeamException extends RuntimeException {

    public DuplicateTeamException(String message) {
        super(message);
    }
}