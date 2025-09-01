package com.miyazaki.cooperativeproposals.exception;


public class SessionOpenedException extends RuntimeException {
    public SessionOpenedException(final String msg) {
        super(msg);
    }
}
