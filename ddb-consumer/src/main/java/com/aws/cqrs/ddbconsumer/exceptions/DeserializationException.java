package com.aws.cqrs.ddbconsumer.exceptions;

/**
 * Exception that is thrown when a deserialization issue occurs.
 */
public class DeserializationException extends RuntimeException {
    /**
     * Default constructor.
     *
     * @param cause The cause.
     */
    public DeserializationException(Throwable cause) {
        super(cause);
    }
}
