package com.aws.cqrs.application;

import com.aws.cqrs.infrastructure.messaging.Event;

import java.util.concurrent.CompletableFuture;

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
    CompletableFuture<Void> handle(T event);
}
