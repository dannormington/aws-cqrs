package com.aws.cqrs.ddbconsumer;

import com.aws.cqrs.application.EventHandler;
import com.aws.cqrs.infrastructure.messaging.Event;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Simple event bus implementation.
 */
public class EventBus {

    private final Map<Class<? extends  Event>, EventHandler<? extends Event>> handlers = new HashMap<>();

    /**
     * Register an event handler.
     *
     * @param eventType The event type.
     * @param eventHandler The event handler.
     * @param <T> The type of event.
     * @return Returns true if the handler is successfully registered.
     */
    public <T extends Event> boolean register(Class<T> eventType, EventHandler<T> eventHandler) {
        if(!handlers.containsKey(eventType)) {
            handlers.put(eventType, eventHandler);
            return true;
        }
        return false;
    }

    /**
     * Handle an event.
     *
     * @param event The event.
     * @param <T> The type of event.
     * @return Returns true if the handler is found and executed.
     */
    public <T extends Event> CompletableFuture<Boolean> handle(T event) {
        if(handlers.containsKey(event.getClass())) {
            EventHandler<T> eventHandler =  (EventHandler<T>)handlers.get(event.getClass());
            return eventHandler.handle(event).thenApply(x -> true);
        }

        return CompletableFuture.completedFuture(false);
    }
}
