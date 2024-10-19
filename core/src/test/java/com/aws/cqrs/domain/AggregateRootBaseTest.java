package com.aws.cqrs.domain;

import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.messaging.Event;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AggregateRootBaseTest {
    @Test
    void when_loadFromHistory_with_no_apply_expect_ignore() {
        // Arrange
        AggregateRootBase aggregateRootBase = new AggregateRootBase() {};
        AccountCreated accountCreated = new AccountCreated(UUID.randomUUID(), "John", "Smith");

        // Act
        aggregateRootBase.loadFromHistory(List.of(accountCreated));
    }

    @Test
    void when_loadFromHistory_expect_no_changes() {
        // Arrange
        AggregateRootBase aggregateRootBase = new AggregateRootBase() {};
        AccountCreated accountCreated = new AccountCreated(UUID.randomUUID(), "John", "Smith");

        // Act
        aggregateRootBase.loadFromHistory(List.of(accountCreated));

        // Assert
        assertTrue(aggregateRootBase.getUncommittedChanges().isEmpty());
    }

    @Test
    void when_loadFromHistory_expect_version() {
        // Arrange
        AggregateRootBase aggregateRootBase = new AggregateRootBase() {};
        AccountCreated accountCreated = new AccountCreated(UUID.randomUUID(), "John", "Smith");

        // Act
        aggregateRootBase.loadFromHistory(List.of(accountCreated));

        // Assert
        assertEquals(1, aggregateRootBase.getExpectedVersion());
    }

    @Test
    void when_loadFromHistory_with_null_expect_success() {
        // Arrange
        AggregateRootBase aggregateRootBase = new AggregateRootBase() {};

        // Act
        aggregateRootBase.loadFromHistory(null);

        // Assert
        assertEquals(0, aggregateRootBase.getExpectedVersion());
    }

    @Test
    void when_markChangesAsCommitted_expect_no_changes() {
        // Arrange
        AggregateRootBase aggregateRootBase = new AggregateRootBase() {};
        AccountCreated accountCreated = new AccountCreated(UUID.randomUUID(), "John", "Smith");
        aggregateRootBase.applyChange(accountCreated);

        // Act
        aggregateRootBase.markChangesAsCommitted();

        // Assert
        assertTrue(aggregateRootBase.getUncommittedChanges().isEmpty());
    }

    @Test
    void when_getExpectedVersion_expect_zero() {
        // Arrange
        AggregateRootBase aggregateRootBase = new AggregateRootBase() {};

        // Act & Assert
        assertEquals(0, aggregateRootBase.getExpectedVersion());
    }

    @Test
    void when_getUncommittedChanges_expect_none() {
        // Arrange
        AggregateRootBase aggregateRootBase = new AggregateRootBase() {};

        // Act
        List<Event> changes = aggregateRootBase.getUncommittedChanges();

        // Assert
        assertTrue(changes.isEmpty());
    }

    @Test
    void when_getUncommittedChanges_expect_one() {
        // Arrange
        AggregateRootBase aggregateRootBase = new AggregateRootBase() {};
        AccountCreated accountCreated = new AccountCreated(UUID.randomUUID(), "John", "Smith");
        aggregateRootBase.applyChange(accountCreated);

        // Act
        List<Event> changes = aggregateRootBase.getUncommittedChanges();

        // Assert
        assertEquals(1, changes.size());
    }

    @Test
    void when_apply_expect_HydrationException () throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        AggregateRootBase aggregateRootBase = new AggregateRootBase() {
            private void apply(AccountCreated event) throws IllegalAccessException {
                throw new IllegalAccessException("Mocked IllegalAccessException");
            }
        };

        assertThrows(HydrationException.class, () -> {
            aggregateRootBase.applyChange(new AccountCreated(UUID.randomUUID(), "John", "Smith"));
        });
    }
}