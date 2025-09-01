package com.miyazaki.cooperativeproposals.exception;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssociatePermissionVoteExceptionTest {
    @Test
    void constructor_ShouldCreateExceptionWithNullMessage_WhenNullMessageProvided() {
        final AssociatePermissionVoteException exception = new AssociatePermissionVoteException(null);

        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    void exception_ShouldBeInstanceOfRuntimeException_WhenCreated() {
        final AssociatePermissionVoteException exception = new AssociatePermissionVoteException("Test message");

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    void exception_ShouldHaveCorrectMessage_WhenThrownWithSpecificMessage() {
        final String specificMessage = "Associate not have permission to vote";

        final AssociatePermissionVoteException thrown = assertThrows(AssociatePermissionVoteException.class, () -> {
            throw new AssociatePermissionVoteException(specificMessage);
        });

        assertEquals(specificMessage, thrown.getMessage());
    }

}
