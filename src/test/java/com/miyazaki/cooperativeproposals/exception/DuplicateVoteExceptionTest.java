package com.miyazaki.cooperativeproposals.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DuplicateVoteExceptionTest {

    @Test
    void constructor_ShouldCreateExceptionWithNullMessage_WhenNullMessageProvided() {
        final DuplicateVoteException exception = new DuplicateVoteException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void exception_ShouldBeInstanceOfRuntimeException_WhenCreated() {
        final DuplicateVoteException exception = new DuplicateVoteException("Test message");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void exception_ShouldHaveCorrectMessage_WhenThrownWithSpecificMessage() {
        final String specificMessage = "Associate UUID-123 has already voted on proposal UUID-456";

        final DuplicateVoteException thrown = assertThrows(DuplicateVoteException.class, () -> {
            throw new DuplicateVoteException(specificMessage);
        });

        assertEquals(specificMessage, thrown.getMessage());
    }
}
