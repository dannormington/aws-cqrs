package com.aws.cqrs.core.persistence;

import java.util.UUID;

import com.aws.cqrs.core.domain.AggregateRoot;
import com.aws.cqrs.core.exceptions.*;

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
