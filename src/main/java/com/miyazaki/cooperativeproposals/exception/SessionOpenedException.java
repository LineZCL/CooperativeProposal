package com.miyazaki.cooperativeproposals.exception;


public class SessionOpenedException extends RuntimeException {
    public SessionOpenedException(String msg){
        super(msg);
    }
}
