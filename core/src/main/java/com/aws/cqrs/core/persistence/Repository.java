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
	 * @param aggregate
	 * @throws EventCollisionException
	 */
	void save(T aggregate) throws EventCollisionException;

	/**
	 * Get the aggregate
	 * 
	 * @param id
	 * @return
	 * @throws HydrationException
	 * @throws AggregateNotFoundException
	 */
	T getById(UUID id) throws HydrationException, AggregateNotFoundException;
}
