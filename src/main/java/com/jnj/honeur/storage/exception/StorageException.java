package com.jnj.honeur.storage.exception;

/**
 * General runtime exception when something goes wrong in the {@link com.jnj.honeur.storage.service.StorageService}
 * @author Peter Moorthamer
 */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }

}
