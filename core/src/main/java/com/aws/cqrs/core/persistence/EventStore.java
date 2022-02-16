package com.aws.cqrs.core.persistence;

import java.util.UUID;

import com.aws.cqrs.core.exceptions.AggregateNotFoundException;
import com.aws.cqrs.core.exceptions.EventCollisionException;
import com.aws.cqrs.core.exceptions.HydrationException;
import com.aws.cqrs.core.messaging.Event;

/**
 * Interface to support basic event store functionality
 */
interface EventStore {

	/**
	 * Persist the changes
	 * 
	 * @param aggregateId
	 * @param expectedVersion
	 * @param events
	 * @throws EventCollisionException
	 */
	void saveEvents(UUID aggregateId, long expectedVersion, Iterable<Event> events) throws EventCollisionException;

	/**
	 * Retrieves the events
	 * 
	 * @param aggregateId
	 * @return
	 * @throws HydrationException
	 * @throws AggregateNotFoundException
	 */
	Iterable<Event> getEvents(UUID aggregateId) throws HydrationException, AggregateNotFoundException;
}
