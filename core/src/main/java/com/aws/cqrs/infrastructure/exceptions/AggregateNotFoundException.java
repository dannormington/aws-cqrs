package com.aws.cqrs.infrastructure.exceptions;

import java.util.UUID;

/**
 * Exception that is thrown when the specified aggregate id is not found
 *
 */
public class AggregateNotFoundException extends AggregateException {

	private static final String ERROR_TEXT = "The aggregate you requested cannot be found.";

	/**
	 * Constructor
	 * 
	 * @param aggregateId The aggregate id.
	 */
	public AggregateNotFoundException(UUID aggregateId) {
		super(aggregateId, ERROR_TEXT);
	}

	/**
	 * Constructor
	 * 
	 * @param source The source of the exception.
	 * @param aggregateId The aggregate id.
	 */
	public AggregateNotFoundException(Throwable source, UUID aggregateId) {
		super(source, aggregateId, ERROR_TEXT);
	}
}
