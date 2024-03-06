package com.aws.cqrs.ddbconsumer;

import com.aws.cqrs.ddbconsumer.eventhandlers.DepositedEventHandler;
import com.aws.cqrs.ddbconsumer.eventhandlers.WithdrewEventHandler;
import com.aws.cqrs.domain.Deposited;
import com.aws.cqrs.domain.Withdrew;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventBusTest {

    private final DynamoDbAsyncClient dynamoDbAsyncClient = mock(DynamoDbAsyncClient.class);

    @Test
    void when_register_expect_success() {
        // Arrange
        EventBus eventBus = new EventBus();

        // Act
        boolean success = eventBus.register(Withdrew.class, new WithdrewEventHandler(dynamoDbAsyncClient));

        // Assert
        assertTrue(success);
    }

    @Test
    void when_register_duplicate_expect_failure() {
        // Arrange
        EventBus eventBus = new EventBus();
        eventBus.register(Withdrew.class, new WithdrewEventHandler(dynamoDbAsyncClient));

        // Act
        boolean failure = eventBus.register(Withdrew.class, new WithdrewEventHandler(dynamoDbAsyncClient));

        // Assert
        assertFalse(failure);
    }

    @Test
    void when_handle_expect_success() {
        // Arrange
        EventBus eventBus = new EventBus();
        DepositedEventHandler depositedEventHandler = mock(DepositedEventHandler.class);
        eventBus.register(Deposited.class, depositedEventHandler);
        Deposited deposited = new Deposited(UUID.randomUUID(), new BigDecimal(100));
        when(depositedEventHandler.handle(deposited)).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        Boolean handled = eventBus.handle(deposited).join();

        // Assert
        assertTrue(handled);
    }

    @Test
    void when_handle_not_registered_expect_failure() {
        // Arrange
        EventBus eventBus = new EventBus();
        Deposited deposited = new Deposited(UUID.randomUUID(), new BigDecimal(100));

        // Act
        Boolean handled = eventBus.handle(deposited).join();

        // Assert
        assertFalse(handled);
    }
}