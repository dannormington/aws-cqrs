package com.aws.cqrs.core.exceptions;

import java.util.UUID;

/**
 * Exception that is thrown when the specified aggregate Id is not found
 *
 */
public class AggregateNotFoundException extends AggregateException {

	private static final String ERROR_TEXT = "The aggregate you requested cannot be found.";

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param aggregateId
	 */
	public AggregateNotFoundException(UUID aggregateId) {
		super(aggregateId, ERROR_TEXT);
	}

	/**
	 * Constructor
	 * 
	 * @param source
	 * @param aggregateId
	 */
	public AggregateNotFoundException(Throwable source, UUID aggregateId) {
		super(source, aggregateId, ERROR_TEXT);
	}
}
