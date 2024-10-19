package com.aws.cqrs.infrastructure.persistence;

import com.aws.cqrs.domain.AggregateRoot;
import com.aws.cqrs.infrastructure.exceptions.AggregateNotFoundException;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.exceptions.TransactionFailedException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Interface for a repository implementation.
 *
 * @param <T>
 */
public interface Repository<T extends AggregateRoot> {

  /**
   * Persists the aggregate
   *
   * @param aggregate The aggregate to save.
   * @throws TransactionFailedException
   */
  CompletableFuture<Void> save(T aggregate) throws TransactionFailedException;

  /**
   * Get the aggregate
   *
   * @param id The id of the aggregate.
   * @return
   * @throws HydrationException
   * @throws AggregateNotFoundException
   */
  CompletableFuture<T> getById(UUID id) throws HydrationException, AggregateNotFoundException;
}
