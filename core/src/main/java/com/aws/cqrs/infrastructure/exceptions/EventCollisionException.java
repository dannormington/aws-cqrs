package com.aws.cqrs.infrastructure.exceptions;

import java.util.Date;
import java.util.UUID;

/**
 * Exception that is thrown when an event is attempted to be persisted but
 * due to unique constraints the transaction failed.
 */
public class EventCollisionException extends TransactionFailedException {

    private static final String ERROR_TEXT = "Unable to persist state due to unique constraints.";

    /**
     * Constructor
     *
     * @param source      The source of the exception.
     * @param aggregateId The aggregate id.
     */
    public EventCollisionException(Throwable source, UUID aggregateId) {
        super(source, ERROR_TEXT, aggregateId);
    }
}
