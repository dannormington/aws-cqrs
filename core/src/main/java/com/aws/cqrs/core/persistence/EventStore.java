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
	 * @param aggregateId The aggregate id.
	 * @param expectedVersion The expected version when persisting state.
	 * @param events The list of events to persist.
	 * @throws EventCollisionException
	 */
	void saveEvents(UUID aggregateId, long expectedVersion, Iterable<Event> events) throws EventCollisionException;

	/**
	 * Retrieves the events
	 * 
	 * @param aggregateId The aggregate id.
	 * @return The list of events.
	 * @throws HydrationException
	 * @throws AggregateNotFoundException
	 */
	Iterable<Event> getEvents(UUID aggregateId) throws HydrationException, AggregateNotFoundException;
}
