package com.nikita.linkservice.exception;

public class ShortLinkNotFoundException extends RuntimeException {

    public ShortLinkNotFoundException(String message) {
        super(message);
    }
}