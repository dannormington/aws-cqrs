package com.aws.cqrs.infrastructure.exceptions;

import java.util.Date;
import java.util.UUID;

/**
 * Exception that is thrown when an event is attempted to be persisted but the
 * current event history has changed
 */
public class EventCollisionException extends AggregateException {

	private static final String ERROR_TEXT = "Data has been changed between loading and state changes.";

	private final long expectedVersion;
	private final Date dateOccurred;

	/**
	 * Constructor
	 * 
	 * @param source The source of the exception.
	 * @param aggregateId The aggregate id.
	 * @param expectedVersion The expected version when persisting state.
	 * 
	 */
	public EventCollisionException(Throwable source, UUID aggregateId, long expectedVersion) {
		super(source, aggregateId, source.getCause().getMessage());
		this.expectedVersion = expectedVersion;
		this.dateOccurred = new Date();
	}

	/**
	 * Get the expected version when the collision occurred
	 * 
	 * @return The expected version when persisting state.
	 */
	public long getExpectedVersion() {
		return expectedVersion;
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
