package com.aws.cqrs.infrastructure.persistence;

import java.util.UUID;

import com.aws.cqrs.domain.AggregateRoot;
import com.aws.cqrs.infrastructure.exceptions.AggregateNotFoundException;
import com.aws.cqrs.infrastructure.exceptions.EventCollisionException;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.exceptions.TransactionFailedException;
import com.aws.cqrs.infrastructure.messaging.Event;

/**
 * Implementation of a simple event repository
 *
 * @param <T>
 */
public class EventRepository<T extends AggregateRoot> implements Repository<T> {

    /**
     * Instance of the event store
     */
    private final EventStore eventStore;

    /**
     * The class type that the repository is working with.
     */
    private final Class<T> aClass;

    /**
     * Default Constructor
     *
     * @param aClass    The type the repository is working with.
     * @param tableName The name of the table.
     * @param queueName The name of the queue to publish messages to.
     */
    public EventRepository(Class<T> aClass, String tableName, String queueName) {
        this.aClass = aClass;
        eventStore = new DynamoDbEventStore(tableName, queueName);
    }

    @Override
    public void save(T aggregate) throws TransactionFailedException {
        eventStore.saveEvents(aggregate.getId(), aggregate.getExpectedVersion(), aggregate.getUncommittedChanges());
        aggregate.markChangesAsCommitted();
    }

    @Override
    public T getById(UUID id) throws HydrationException, AggregateNotFoundException {

        /*
         * get the events from the event store
         */
        Iterable<Event> history = eventStore.getEvents(id);

        /*
         * Create a new instance of the aggregate
         */
        T aggregate;
        try {
            aggregate = this.aClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new HydrationException(id);
        }

        aggregate.loadFromHistory(history);

        return aggregate;
    }
}
