package com.aws.cqrs.core.exceptions;

import java.util.Date;
import java.util.UUID;

/**
 * Exception that is thrown when an event is attempted to be persisted but the
 * current event history has changed
 */
public class EventCollisionException extends AggregateException {

	private static final String ERROR_TEXT = "Data has been changed between loading and state changes.";

	private static final long serialVersionUID = 1L;

	private int expectedVersion;
	private Date dateOccurred;

	/**
	 * Constructor
	 * 
	 * @param aggregateId
	 * @param expectedVersion
	 */
	public EventCollisionException(UUID aggregateId, int expectedVersion) {
		this(null, aggregateId, expectedVersion);
	}

	/**
	 * Constructor
	 * 
	 * @param source
	 * @param aggregateId
	 * @param expectedVersion
	 * 
	 */
	public EventCollisionException(Throwable source, UUID aggregateId, int expectedVersion) {
		super(source, aggregateId, ERROR_TEXT);
		this.expectedVersion = expectedVersion;
		this.dateOccurred = new Date();
	}

	/**
	 * Get the expected version when the collision occurred
	 * 
	 * @return
	 */
	public int getExpectedVersion() {
		return expectedVersion;
	}

	/**
	 * Get the date the exception occurred
	 * 
	 * @return
	 */
	public Date getDateOccurred() {
		return dateOccurred;
	}
}
