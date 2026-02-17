package com.example.bankcards.exception;

public class NotfoundUserException extends RuntimeException {
    public NotfoundUserException(String message) {
        super("User not found: " + message);
    }
}
