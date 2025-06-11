package com.library.management.exception;

public class BookRequestNotFoundException extends RuntimeException {
    public BookRequestNotFoundException(String message) {
        super(message);
    }
}
