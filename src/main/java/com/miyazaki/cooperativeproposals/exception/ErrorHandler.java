package com.miyazaki.cooperativeproposals.exception;

import com.miyazaki.cooperativeproposals.controller.dto.response.DefaultErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DefaultErrorResponse> handleValidError(MethodArgumentNotValidException ex){
        final String details = ex.getBindingResult().getFieldErrors().stream()
                .sorted(java.util.Comparator.comparing(org.springframework.validation.FieldError::getField))
                .map(fe -> fe.getField() + ": " + String.valueOf(fe.getDefaultMessage()))
                .collect(java.util.stream.Collectors.joining("; ")); // <- String final

        final var errorResponse = DefaultErrorResponse.builder()
                .message("Parâmetros inválidos")
                .details(details)
                .build();
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<DefaultErrorResponse> notFoundHandler(NotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(DefaultErrorResponse.builder().message(ex.getMessage()).build());
    }

    @ExceptionHandler(SessionOpenedException.class)
    public ResponseEntity<DefaultErrorResponse> sessionOpenedHandler(SessionOpenedException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(DefaultErrorResponse.builder().message(ex.getMessage()).build());
    }
}
