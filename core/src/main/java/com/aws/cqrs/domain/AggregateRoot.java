package com.aws.cqrs.domain;

import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.messaging.Event;
import java.util.List;
import java.util.UUID;

/** Simple interface to an aggregate root */
public interface AggregateRoot {

  /**
   * get the id.
   *
   * @return The aggregate id.
   */
  UUID getId();

  /**
   * Gets an immutable list of all change events since the original hydration. If there are no
   * changes then null is returned
   *
   * @return The list of uncommitted changes.
   */
  List<Event> getUncommittedChanges();

  /** Mark all changes a committed */
  void markChangesAsCommitted();

  /**
   * load the aggregate root
   *
   * @param history The list of state changes from the event store.
   * @throws HydrationException
   */
  void loadFromHistory(Iterable<Event> history) throws HydrationException;

  /**
   * Returns the version of the aggregate when it was hydrated
   *
   * @return The expected version of the aggregate.
   */
  int getExpectedVersion();
}
