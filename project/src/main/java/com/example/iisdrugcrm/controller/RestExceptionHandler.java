package com.example.iisdrugcrm.controller;

import com.example.iisdrugcrm.exception.DuplicateUserException;
import com.example.iisdrugcrm.exception.DuplicateTeamException;
import com.example.iisdrugcrm.exception.InvalidPricelistThresholdException;
import com.example.iisdrugcrm.exception.InvalidPricelistStatusTransitionException;
import com.example.iisdrugcrm.exception.PricelistConflictException;
import com.example.iisdrugcrm.exception.PricelistLockedException;
import com.example.iisdrugcrm.exception.PricelistNotFoundException;
import com.example.iisdrugcrm.exception.PricelistSubmissionValidationException;
import com.example.iisdrugcrm.exception.PricelistStartDateInPastException;
import com.example.iisdrugcrm.exception.RegionConflictException;
import com.example.iisdrugcrm.exception.RegionInUseException;
import com.example.iisdrugcrm.exception.VariantNotFoundException;
import java.util.Map;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateUser(DuplicateUserException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(DuplicateTeamException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateTeam(DuplicateTeamException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(RegionConflictException.class)
    public ResponseEntity<Map<String, String>> handleRegionConflict(RegionConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(RegionInUseException.class)
    public ResponseEntity<Map<String, String>> handleRegionInUse(RegionInUseException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(VariantNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleVariantNotFound(VariantNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(InvalidPricelistThresholdException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPricelistThreshold(InvalidPricelistThresholdException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(InvalidPricelistStatusTransitionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidPricelistStatusTransition(InvalidPricelistStatusTransitionException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(PricelistNotFoundException.class)
    public ResponseEntity<Map<String, String>> handlePricelistNotFound(PricelistNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(PricelistConflictException.class)
    public ResponseEntity<Map<String, String>> handlePricelistConflict(PricelistConflictException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(PricelistLockedException.class)
    public ResponseEntity<Map<String, String>> handlePricelistLocked(PricelistLockedException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(PricelistStartDateInPastException.class)
    public ResponseEntity<Map<String, String>> handlePricelistStartDateInPast(PricelistStartDateInPastException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(PricelistSubmissionValidationException.class)
    public ResponseEntity<Map<String, String>> handlePricelistSubmissionValidation(PricelistSubmissionValidationException exception) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .filter(errorMessage -> errorMessage != null && !errorMessage.isBlank())
                .findFirst()
                .orElse("Validation failed");

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("error", message));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(DataIntegrityViolationException exception) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Conflict"));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Incorrect username or password."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class, PropertyReferenceException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(Exception exception) {
        return ResponseEntity.badRequest().body(Map.of("error", exception.getMessage()));
    }
}
