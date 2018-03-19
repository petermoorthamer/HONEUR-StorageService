package com.jnj.honeur.storage.exception;

/**
 * RuntimeException that can be thrown when changes cannot be written to an existing notebook file
 * @author Peter Moorthamer
 */
public class NotebookFileWriteException extends RuntimeException {

    public NotebookFileWriteException(String message) {
        super(message);
    }

    public NotebookFileWriteException(String message, Throwable cause) {
        super(message, cause);
    }

}
