package com.jnj.honeur.storage.exception;

/**
 * RuntimeException that can be thrown when a notebook JSON file cannot be parsed
 * @author Peter Moorthamer
 */

public class NotebookFileParseException extends RuntimeException {

    public NotebookFileParseException(String message) {
        super(message);
    }

    public NotebookFileParseException(String message, Throwable cause) {
        super(message, cause);
    }

}
