package com.aws.cqrs.infrastructure.persistence;

import com.aws.cqrs.domain.AggregateRoot;
import com.aws.cqrs.infrastructure.exceptions.AggregateNotFoundException;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.exceptions.TransactionFailedException;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of a simple event repository
 *
 * @param <T>
 */
public class EventRepository<T extends AggregateRoot> implements Repository<T> {

  /** Instance of the event store */
  private final EventStore eventStore;

  /** The class type that the repository is working with. */
  private final Class<T> aClass;

  /**
   * Default Constructor
   *
   * @param aClass The type the repository is working with.
   * @param eventStore The event store.
   */
  public EventRepository(Class<T> aClass, EventStore eventStore) {
    this.aClass = aClass;
    this.eventStore = eventStore;
  }

  @Override
  public CompletableFuture<Void> save(T aggregate) throws TransactionFailedException {
    return eventStore
        .saveEvents(
            aggregate.getId(), aggregate.getExpectedVersion(), aggregate.getUncommittedChanges())
        .thenAccept(
            x -> {
              aggregate.markChangesAsCommitted();
            });
  }

  @Override
  public CompletableFuture<T> getById(UUID id)
      throws HydrationException, AggregateNotFoundException {
    /*
     * get the events from the event store
     */
    return eventStore
        .getEvents(id)
        .thenApply(
            history -> {
              /*
               * Create a new instance of the aggregate
               */
              T aggregate;
              try {
                aggregate = aClass.getConstructor().newInstance();
              } catch (InstantiationException
                  | IllegalAccessException
                  | InvocationTargetException
                  | NoSuchMethodException e) {
                throw new HydrationException(id);
              }

              aggregate.loadFromHistory(history);

              return aggregate;
            });
  }
}
