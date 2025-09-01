package com.miyazaki.cooperativeproposals.exception;

public class DuplicateVoteException extends RuntimeException {
    public DuplicateVoteException(final String message) {
        super(message);
    }
}
