package com.aws.cqrs.ddbconsumer;

import static com.aws.cqrs.infrastructure.persistence.DynamoDbEventStore.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord;
import com.aws.cqrs.application.OffsetDateTimeDeserializer;
import com.aws.cqrs.ddbconsumer.exceptions.DeserializationException;
import com.aws.cqrs.domain.AccountCreated;
import com.aws.cqrs.infrastructure.messaging.Event;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;

class DdbEventHandlerTest {

  private final Gson gson =
      new GsonBuilder()
          .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer())
          .create();

  @Test
  void when_handleRequest_no_events_expect_success() {
    // Arrange
    EventBus eventBus = mock(EventBus.class);
    DdbEventHandler ddbEventHandler = new DdbEventHandler(eventBus, gson);
    DynamodbEvent dynamodbEvent = new DynamodbEvent();
    dynamodbEvent.setRecords(new ArrayList<>());

    // Act
    ddbEventHandler.handleRequest(dynamodbEvent, null);

    // Assert
    verifyNoInteractions(eventBus);
  }

  @Test
  void when_handleRequest_expect_success() {
    // Arrange
    EventBus eventBus = mock(EventBus.class);
    DdbEventHandler ddbEventHandler = new DdbEventHandler(eventBus, gson);
    DynamodbEvent dynamodbEvent = new DynamodbEvent();

    Map<String, AttributeValue> attributes = new HashMap<>();
    AccountCreated accountCreated = new AccountCreated(UUID.randomUUID(), "John", "Smith");
    attributes.put(KIND_ATTRIBUTE, new AttributeValue().withS(accountCreated.getClass().getName()));
    attributes.put(EVENT_ATTRIBUTE, new AttributeValue().withS(gson.toJson(accountCreated)));

    List<DynamodbEvent.DynamodbStreamRecord> records = new ArrayList<>();
    DynamodbEvent.DynamodbStreamRecord dynamodbStreamRecord =
        new DynamodbEvent.DynamodbStreamRecord();
    dynamodbStreamRecord.setDynamodb(new StreamRecord().withNewImage(attributes));
    dynamodbStreamRecord
        .getDynamodb()
        .setKeys(Map.of(ID_ATTRIBUTE, new AttributeValue().withS(UUID.randomUUID().toString())));
    records.add(dynamodbStreamRecord);
    dynamodbEvent.setRecords(records);

    doReturn(CompletableFuture.completedFuture(true)).when(eventBus).handle(any(Event.class));

    // Act
    ddbEventHandler.handleRequest(dynamodbEvent, null);

    // Assert
    verify(eventBus, times(1)).handle(any(Event.class));
  }

  @Test
  void when_handleRequest_expect_DeserializationException() {
    // Arrange
    EventBus eventBus = mock(EventBus.class);
    DdbEventHandler ddbEventHandler = new DdbEventHandler(eventBus, gson);
    DynamodbEvent dynamodbEvent = new DynamodbEvent();

    Map<String, AttributeValue> attributes = new HashMap<>();
    AccountCreated accountCreated = new AccountCreated(UUID.randomUUID(), "John", "Smith");
    attributes.put(KIND_ATTRIBUTE, new AttributeValue().withS("invalid-class-name"));
    attributes.put(EVENT_ATTRIBUTE, new AttributeValue().withS(gson.toJson(accountCreated)));

    List<DynamodbEvent.DynamodbStreamRecord> records = new ArrayList<>();
    DynamodbEvent.DynamodbStreamRecord dynamodbStreamRecord =
        new DynamodbEvent.DynamodbStreamRecord();
    dynamodbStreamRecord.setDynamodb(new StreamRecord().withNewImage(attributes));
    dynamodbStreamRecord
        .getDynamodb()
        .setKeys(Map.of(ID_ATTRIBUTE, new AttributeValue().withS(UUID.randomUUID().toString())));
    records.add(dynamodbStreamRecord);
    dynamodbEvent.setRecords(records);

    // Act & Assert
    assertThrows(
        DeserializationException.class,
        () -> {
          try {
            ddbEventHandler.handleRequest(dynamodbEvent, null);
          } catch (CompletionException x) {
            throw x.getCause();
          }
        });

    // Assert
    verifyNoInteractions(eventBus);
  }
}
