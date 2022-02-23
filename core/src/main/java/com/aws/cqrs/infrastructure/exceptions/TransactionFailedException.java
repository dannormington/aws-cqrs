package com.aws.cqrs.infrastructure.exceptions;

import java.util.Date;
import java.util.UUID;

/**
 * Exception that is thrown persisting data fails.
 */
public class TransactionFailedException extends AggregateException {

    private final Date dateOccurred;

    /**
     * Constructor
     *
     * @param source      The source of the exception.
     * @param aggregateId The aggregate id.
     */
    public TransactionFailedException(Throwable source, UUID aggregateId) {
        super(source, aggregateId, source.getMessage());
        this.dateOccurred = new Date();
    }

    /**
     * Constructor
     *
     * @param message     The error message.
     * @param aggregateId The aggregate id.
     */
    public TransactionFailedException(Throwable source, String message, UUID aggregateId) {
        super(source, aggregateId, message);
        this.dateOccurred = new Date();
    }

    /**
     * Get the date the exception occurred
     *
     * @return The date the exception occurred.
     */
    public Date getDateOccurred() {
        return dateOccurred;
    }
}
