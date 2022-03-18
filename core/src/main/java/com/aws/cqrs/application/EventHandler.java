package com.aws.cqrs.application;

import com.aws.cqrs.infrastructure.messaging.Event;

/**
 * Interface for an event handler.
 *
 * @param <T>
 */
public interface EventHandler<T extends Event> {
    /**
     * Handle an event.
     *
     * @param event
     */
    void handle(T event);
}
