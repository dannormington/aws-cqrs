package com.aws.cqrs.infrastructure.persistence;

import java.util.UUID;

import com.aws.cqrs.infrastructure.exceptions.AggregateNotFoundException;
import com.aws.cqrs.infrastructure.exceptions.EventCollisionException;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.domain.AggregateRoot;

/**
 * Interface for a repository implementation
 * 
 * @param <T>
 */
public interface Repository<T extends AggregateRoot> {

	/**
	 * Persists the aggregate
	 * 
	 * @param aggregate The aggregate to save.
	 * @throws EventCollisionException
	 */
	void save(T aggregate) throws EventCollisionException;

	/**
	 * Get the aggregate
	 * 
	 * @param id The id of the aggregate.
	 * @return
	 * @throws HydrationException
	 * @throws AggregateNotFoundException
	 */
	T getById(UUID id) throws HydrationException, AggregateNotFoundException;
}
