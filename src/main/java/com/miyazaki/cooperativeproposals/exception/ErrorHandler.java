package com.miyazaki.cooperativeproposals.exception;

import com.miyazaki.cooperativeproposals.controller.dto.response.DefaultErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DefaultErrorResponse> handleValidError(MethodArgumentNotValidException ex){
        log.warn("Validation error occurred: {}", ex.getMessage());
        
        final String details = ex.getBindingResult().getFieldErrors().stream()
                .sorted(java.util.Comparator.comparing(org.springframework.validation.FieldError::getField))
                .map(fe -> fe.getField() + ": " + String.valueOf(fe.getDefaultMessage()))
                .collect(java.util.stream.Collectors.joining("; ")); // <- String final

        log.debug("Validation error details: {}", details);

        final var errorResponse = DefaultErrorResponse.builder()
                .message("Parâmetros inválidos")
                .details(details)
                .build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<DefaultErrorResponse> notFoundHandler(NotFoundException ex){
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(DefaultErrorResponse.builder().message(ex.getMessage()).build());
    }

    @ExceptionHandler(SessionOpenedException.class)
    public ResponseEntity<DefaultErrorResponse> sessionOpenedHandler(SessionOpenedException ex){
        log.warn("Session already opened: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(DefaultErrorResponse.builder().message(ex.getMessage()).build());
    }
}
