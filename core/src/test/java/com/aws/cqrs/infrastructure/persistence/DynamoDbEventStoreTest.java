package com.aws.cqrs.infrastructure.persistence;

import com.aws.cqrs.application.OffsetDateTimeDeserializer;
import com.aws.cqrs.domain.AccountCreated;
import com.aws.cqrs.domain.Deposited;
import com.aws.cqrs.infrastructure.exceptions.AggregateNotFoundException;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.exceptions.TransactionFailedException;
import com.aws.cqrs.infrastructure.messaging.Event;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DynamoDbEventStoreTest {

    private static final String TABLE_NAME = "tableName";
    private  final Gson gson = new GsonBuilder()
           .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer())
           .create();

    @Test
    void when_saveEvents_expect_success() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        List<Event> events = List.of(new AccountCreated(accountId, "John", "Doe"));
        DynamoDbAsyncClient dynamoDbAsyncClient = mock(DynamoDbAsyncClient.class);
        DynamoDbEventStore eventStore = new DynamoDbEventStore(TABLE_NAME, dynamoDbAsyncClient, gson);

        Map<String, AttributeValue> propertyMap = new HashMap<>();
        propertyMap.put("Id", AttributeValue.builder().s(accountId.toString()).build());
        propertyMap.put("Version", AttributeValue.builder().n(String.valueOf(1)).build());
        propertyMap.put("Event", AttributeValue.builder().s(gson.toJson(events.get(0))).build());
        propertyMap.put("Kind", AttributeValue.builder().s(events.get(0).getClass().getName()).build());

        Put put = Put.builder().item(propertyMap)
                .tableName(TABLE_NAME)
                .build();

        TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder()
                .transactItems(List.of(TransactWriteItem.builder().put(put).build()))
                .build();

        when(dynamoDbAsyncClient.transactWriteItems(any(TransactWriteItemsRequest.class))).thenReturn(CompletableFuture.completedFuture(TransactWriteItemsResponse.builder().build()));

        // Act
        eventStore.saveEvents(accountId, 0,events).join();

        // Assert
        verify(dynamoDbAsyncClient, times(1)).transactWriteItems(transactWriteItemsRequest);
    }

    @Test
    void when_saveEvents_expect_TransactionFailedException() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        List<Event> events = List.of(new AccountCreated(accountId, "John", "Doe"));
        DynamoDbAsyncClient dynamoDbAsyncClient = mock(DynamoDbAsyncClient.class);
        DynamoDbEventStore eventStore = new DynamoDbEventStore(TABLE_NAME, dynamoDbAsyncClient, gson);

        Map<String, AttributeValue> propertyMap = new HashMap<>();
        propertyMap.put("Id", AttributeValue.builder().s(accountId.toString()).build());
        propertyMap.put("Version", AttributeValue.builder().n(String.valueOf(1)).build());
        propertyMap.put("Event", AttributeValue.builder().s(gson.toJson(events.get(0))).build());
        propertyMap.put("Kind", AttributeValue.builder().s(events.get(0).getClass().getName()).build());

        Put put = Put.builder().item(propertyMap)
                .tableName(TABLE_NAME)
                .build();

        TransactWriteItemsRequest transactWriteItemsRequest = TransactWriteItemsRequest.builder()
                .transactItems(List.of(TransactWriteItem.builder().put(put).build()))
                .build();

        when(dynamoDbAsyncClient.transactWriteItems(any(TransactWriteItemsRequest.class))).thenReturn(CompletableFuture.failedFuture(ProvisionedThroughputExceededException.builder().build()));

        // Act
        CompletableFuture<Void> result = eventStore.saveEvents(accountId, 0,events);

        // Assert
        assertThrows(TransactionFailedException.class, () -> {
            try {
                result.join();
            } catch (CompletionException x) {
                throw x.getCause();
            }
        });
        verify(dynamoDbAsyncClient, times(1)).transactWriteItems(transactWriteItemsRequest);
    }

    @Test
    void when_getEvents_expect_success() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        AccountCreated accountCreated = new AccountCreated(accountId, "John", "Doe");
        Deposited deposited = new Deposited(accountId,new BigDecimal(100), new BigDecimal(100));
        DynamoDbAsyncClient dynamoDbAsyncClient = mock(DynamoDbAsyncClient.class);
        DynamoDbEventStore eventStore = new DynamoDbEventStore(TABLE_NAME, dynamoDbAsyncClient, gson);

        Map<String, AttributeValue> accountCreatedMap = new HashMap<>();
        accountCreatedMap.put("Id", AttributeValue.builder().s(accountId.toString()).build());
        accountCreatedMap.put("Version", AttributeValue.builder().n(String.valueOf(1)).build());
        accountCreatedMap.put("Event", AttributeValue.builder().s(gson.toJson(accountCreated)).build());
        accountCreatedMap.put("Kind", AttributeValue.builder().s(AccountCreated.class.getName()).build());

        Map<String, AttributeValue> depositedMap = new HashMap<>();
        depositedMap.put("Id", AttributeValue.builder().s(accountId.toString()).build());
        depositedMap.put("Version", AttributeValue.builder().n(String.valueOf(2)).build());
        depositedMap.put("Event", AttributeValue.builder().s(gson.toJson(deposited)).build());
        depositedMap.put("Kind", AttributeValue.builder().s(Deposited.class.getName()).build());

        QueryResponse queryResponse = QueryResponse.builder()
                .items(accountCreatedMap, depositedMap)
                .build();
        when(dynamoDbAsyncClient.query(any(QueryRequest.class))).thenReturn(CompletableFuture.completedFuture(queryResponse));

        // Act
        List<Event> events = eventStore.getEvents(accountId).join();

        // Assert
        assertEquals(2, events.size());
        assertEquals(AccountCreated.class, events.get(0).getClass());
        assertEquals(Deposited.class, events.get(1).getClass());
    }

    @Test
    void when_getEvents_expect_AggregateNotFoundException() {
        // Arrange
        DynamoDbAsyncClient dynamoDbAsyncClient = mock(DynamoDbAsyncClient.class);
        DynamoDbEventStore eventStore = new DynamoDbEventStore(TABLE_NAME, dynamoDbAsyncClient, gson);
        when(dynamoDbAsyncClient.query(any(QueryRequest.class))).thenReturn(CompletableFuture.completedFuture(QueryResponse.builder().build()));

        // Act & Assert
        assertThrows(AggregateNotFoundException.class, () -> {
            try {
                eventStore.getEvents(UUID.randomUUID()).join();
            } catch (CompletionException x) {
                throw x.getCause();
            }
        });
    }

    @Test
    void when_getEvents_expect_HydrationException() {
        // Arrange
        DynamoDbAsyncClient dynamoDbAsyncClient = mock(DynamoDbAsyncClient.class);
        DynamoDbEventStore eventStore = new DynamoDbEventStore(TABLE_NAME, dynamoDbAsyncClient, gson);

        CompletableFuture badCf = new CompletableFuture();
        badCf.completeExceptionally(new CompletionException(ProvisionedThroughputExceededException.builder().build()));
        doReturn(badCf).when(dynamoDbAsyncClient).query(any(QueryRequest.class));

        // Act & Assert
        assertThrows(HydrationException.class, () -> {
            try {
                eventStore.getEvents(UUID.randomUUID()).join();
            } catch (CompletionException x) {
                throw x.getCause();
            }
        });
    }

    @Test
    void when_getEvents_ClassNotFoundException_expect_HydrationException() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        AccountCreated accountCreated = new AccountCreated(accountId, "John", "Doe");
        DynamoDbAsyncClient dynamoDbAsyncClient = mock(DynamoDbAsyncClient.class);
        DynamoDbEventStore eventStore = new DynamoDbEventStore(TABLE_NAME, dynamoDbAsyncClient, gson);

        Map<String, AttributeValue> accountCreatedMap = new HashMap<>();
        accountCreatedMap.put("Id", AttributeValue.builder().s(accountId.toString()).build());
        accountCreatedMap.put("Version", AttributeValue.builder().n(String.valueOf(1)).build());
        accountCreatedMap.put("Event", AttributeValue.builder().s(gson.toJson(accountCreated)).build());
        accountCreatedMap.put("Kind", AttributeValue.builder().s("class-not-found").build());

        QueryResponse queryResponse = QueryResponse.builder()
                .items(accountCreatedMap)
                .build();
        when(dynamoDbAsyncClient.query(any(QueryRequest.class))).thenReturn(CompletableFuture.completedFuture(queryResponse));

        assertThrows(HydrationException.class, () -> {
            try {
                eventStore.getEvents(UUID.randomUUID()).join();
            } catch (CompletionException x) {
                throw x.getCause();
            }
        });
    }

    @Test
    void when_getEvents_JsonException_expect_HydrationException() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        DynamoDbAsyncClient dynamoDbAsyncClient = mock(DynamoDbAsyncClient.class);
        DynamoDbEventStore eventStore = new DynamoDbEventStore(TABLE_NAME, dynamoDbAsyncClient, gson);

        Map<String, AttributeValue> accountCreatedMap = new HashMap<>();
        accountCreatedMap.put("Id", AttributeValue.builder().s(accountId.toString()).build());
        accountCreatedMap.put("Version", AttributeValue.builder().n(String.valueOf(1)).build());
        accountCreatedMap.put("Event", AttributeValue.builder().s("{invalid-json}}").build());
        accountCreatedMap.put("Kind", AttributeValue.builder().s(AccountCreated.class.getName()).build());

        QueryResponse queryResponse = QueryResponse.builder()
                .items(accountCreatedMap)
                .build();
        when(dynamoDbAsyncClient.query(any(QueryRequest.class))).thenReturn(CompletableFuture.completedFuture(queryResponse));

        assertThrows(HydrationException.class, () -> {
            try {
                eventStore.getEvents(UUID.randomUUID()).join();
            } catch (CompletionException x) {
                throw x.getCause();
            }
        });
    }

}