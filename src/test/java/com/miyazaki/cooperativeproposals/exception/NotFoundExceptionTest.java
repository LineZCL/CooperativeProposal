package com.miyazaki.cooperativeproposals.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NotFoundExceptionTest {

    @Test
    void constructor_ShouldCreateExceptionWithMessage_WhenValidMessageProvided() {
        final String errorMessage = "Resource not found";

        final NotFoundException exception = new NotFoundException(errorMessage);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void constructor_ShouldCreateExceptionWithNullMessage_WhenNullMessageProvided() {
        final NotFoundException exception = new NotFoundException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void exception_ShouldBeInstanceOfRuntimeException_WhenCreated() {
        final NotFoundException exception = new NotFoundException("Test message");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void exception_ShouldBeThrowable_WhenThrown() {
        final String errorMessage = "Test not found";

        final NotFoundException thrown = assertThrows(NotFoundException.class, () -> {
            throw new NotFoundException(errorMessage);
        });

        assertEquals(errorMessage, thrown.getMessage());
    }

}
