package com.miyazaki.cooperativeproposals.exception;

import com.miyazaki.cooperativeproposals.controller.dto.response.DefaultErrorResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorHandlerTest {

    @InjectMocks
    private ErrorHandler errorHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    @Test
    void handleValidError_ShouldReturnBadRequest_WhenSingleFieldError() {
        FieldError fieldError = new FieldError("createProposalRequest", "title", "must not be blank");
        List<FieldError> fieldErrors = List.of(fieldError);

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<DefaultErrorResponse> response = errorHandler.handleValidError(methodArgumentNotValidException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Parâmetros inválidos", response.getBody().getMessage());
        assertEquals("title: must not be blank", response.getBody().getDetails());
    }

    @Test
    void handleValidError_ShouldReturnBadRequest_WhenMultipleFieldErrors() {
        FieldError fieldError1 = new FieldError("createProposalRequest", "title", "must not be blank");
        FieldError fieldError2 = new FieldError("createProposalRequest", "description", "size must be between 1 and 500");
        List<FieldError> fieldErrors = Arrays.asList(fieldError2, fieldError1); // Intentionally unordered

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        ResponseEntity<DefaultErrorResponse> response = errorHandler.handleValidError(methodArgumentNotValidException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Parâmetros inválidos", response.getBody().getMessage());
        assertEquals("description: size must be between 1 and 500; title: must not be blank", response.getBody().getDetails());
    }

    @Test
    void notFoundHandler_ShouldReturnNotFound_WhenNotFoundExceptionThrown() {
        final String errorMessage = "Proposal not found!";
        final NotFoundException notFoundException = new NotFoundException(errorMessage);

        final ResponseEntity<DefaultErrorResponse> response = errorHandler.notFoundHandler(notFoundException);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getDetails());
    }

    @Test
    void notFoundHandler_ShouldReturnNotFoundWithNullMessage_WhenNotFoundExceptionHasNullMessage() {
        final NotFoundException notFoundException = new NotFoundException(null);
        final ResponseEntity<DefaultErrorResponse> response = errorHandler.notFoundHandler(notFoundException);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getMessage());
        assertNull(response.getBody().getDetails());
    }

    @Test
    void sessionOpenedHandler_ShouldReturnConflict_WhenSessionOpenedExceptionThrown() {
        final String errorMessage = "Session voting to proposal already opened";
        final SessionOpenedException sessionOpenedException = new SessionOpenedException(errorMessage);

        final ResponseEntity<DefaultErrorResponse> response = errorHandler.sessionOpenedHandler(sessionOpenedException);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getDetails());
    }

    @Test
    void sessionOpenedHandler_ShouldReturnConflictWithNullMessage_WhenSessionOpenedExceptionHasNullMessage() {
        final SessionOpenedException sessionOpenedException = new SessionOpenedException(null);
        final ResponseEntity<DefaultErrorResponse> response = errorHandler.sessionOpenedHandler(sessionOpenedException);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getMessage());
        assertNull(response.getBody().getDetails());
    }

    @Test
    void duplicateVoteHandler_ShouldReturnConflict_WhenDuplicateVoteExceptionThrown() {
        final String errorMessage = "Associate has already voted on proposal";
        final DuplicateVoteException duplicateVoteException = new DuplicateVoteException(errorMessage);

        final ResponseEntity<DefaultErrorResponse> response = errorHandler.duplicateVoteHandler(duplicateVoteException);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getDetails());
    }

    @Test
    void duplicateVoteHandler_ShouldReturnConflictWithNullMessage_WhenDuplicateVoteExceptionHasNullMessage() {
        final SessionOpenedException sessionOpenedException = new SessionOpenedException(null);
        final ResponseEntity<DefaultErrorResponse> response = errorHandler.sessionOpenedHandler(sessionOpenedException);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getMessage());
        assertNull(response.getBody().getDetails());
    }

    @Test
    void associateNotPermissionVoteHandler_ShouldReturnForbidden_WhenAssociateNotPermissionVoteHandler() {
        final String errorMessage = "Associate has not permission to vote";
        final AssociatePermissionVoteException associatePermissionVoteException = new AssociatePermissionVoteException(errorMessage);

        final ResponseEntity<DefaultErrorResponse> response = errorHandler.associateNotPermissionVoteHandler(associatePermissionVoteException);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(errorMessage, response.getBody().getMessage());
        assertNull(response.getBody().getDetails());
    }

    @Test
    void associateNotPermissionVoteHandler_ShouldReturnConflictWithNullMessage_WhenAssociateNotPermissionVoteHandler() {
        final AssociatePermissionVoteException associatePermissionVoteException = new AssociatePermissionVoteException(null);
        final ResponseEntity<DefaultErrorResponse> response = errorHandler.associateNotPermissionVoteHandler(associatePermissionVoteException);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNull(response.getBody().getMessage());
        assertNull(response.getBody().getDetails());
    }


}
