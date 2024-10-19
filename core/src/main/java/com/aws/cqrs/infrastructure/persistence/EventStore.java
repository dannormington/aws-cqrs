package com.aws.cqrs.infrastructure.persistence;

import com.aws.cqrs.infrastructure.exceptions.AggregateNotFoundException;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.exceptions.TransactionFailedException;
import com.aws.cqrs.infrastructure.messaging.Event;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/** Interface to support basic event store functionality */
public interface EventStore {

  /**
   * Persist the changes
   *
   * @param aggregateId The aggregate id.
   * @param expectedVersion The expected version when persisting state.
   * @param events The list of events to persist.
   * @throws TransactionFailedException
   */
  CompletableFuture<Void> saveEvents(UUID aggregateId, long expectedVersion, List<Event> events)
      throws TransactionFailedException;

  /**
   * Retrieves the events
   *
   * @param aggregateId The aggregate id.
   * @return The list of events.
   * @throws HydrationException
   * @throws AggregateNotFoundException
   */
  CompletableFuture<List<Event>> getEvents(UUID aggregateId)
      throws HydrationException, AggregateNotFoundException;
}
