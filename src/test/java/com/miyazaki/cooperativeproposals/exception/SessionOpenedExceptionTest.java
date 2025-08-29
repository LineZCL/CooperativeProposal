package com.miyazaki.cooperativeproposals.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SessionOpenedExceptionTest {

    @Test
    void constructor_ShouldCreateExceptionWithMessage_WhenValidMessageProvided() {
        final String errorMessage = "Session already opened";

        final SessionOpenedException exception = new SessionOpenedException(errorMessage);

        assertNotNull(exception);
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void constructor_ShouldCreateExceptionWithNullMessage_WhenNullMessageProvided() {
        final SessionOpenedException exception = new SessionOpenedException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }


    @Test
    void exception_ShouldBeInstanceOfRuntimeException_WhenCreated() {
        final SessionOpenedException exception = new SessionOpenedException("Test message");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void exception_ShouldBeThrowable_WhenThrown() {
        final String errorMessage = "Session is already opened for this proposal";

        final SessionOpenedException thrown = assertThrows(SessionOpenedException.class, () -> {
            throw new SessionOpenedException(errorMessage);
        });

        assertEquals(errorMessage, thrown.getMessage());
    }

}
