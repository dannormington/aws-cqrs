package com.aws.cqrs.core.exceptions;

import java.util.UUID;

/**
 * Exception that is thrown when the hydration of an aggregate fails. This can
 * occur during the loading from a repository as well as during the "apply"
 * methods when there is a reflection based exception.
 */
public class HydrationException extends AggregateException {

	private static final String ERROR_TEXT = "Loading the data failed";

	/**
	 * Default constructor
	 * 
	 * @param aggregateId The aggregate id.
	 */
	public HydrationException(UUID aggregateId) {
		super(aggregateId, ERROR_TEXT);
	}

	/**
	 * Constructor
	 * 
	 * @param source The source of the exception.
	 * @param aggregateId The aggregate id.
	 */
	public HydrationException(Throwable source, UUID aggregateId) {
		super(source, aggregateId, ERROR_TEXT);
	}
}
