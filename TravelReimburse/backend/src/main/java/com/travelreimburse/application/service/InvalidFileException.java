package com.travelreimburse.application.service;

/**
 * Exception für ungültige Datei-Uploads.
 */
public class InvalidFileException extends RuntimeException {

    public InvalidFileException(String message) {
        super(message);
    }

    public InvalidFileException(String message, Throwable cause) {
        super(message, cause);
    }
}

