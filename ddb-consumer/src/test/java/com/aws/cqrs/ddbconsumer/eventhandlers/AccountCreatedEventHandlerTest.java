package com.aws.cqrs.ddbconsumer.eventhandlers;

import static com.aws.cqrs.ddbconsumer.eventhandlers.AccountAttributes.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.aws.cqrs.domain.AccountCreated;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

class AccountCreatedEventHandlerTest {

  @Test
  void when_handle_expect_success() {
    // Arrange
    DynamoDbAsyncClient dynamoDbAsyncClient = mock(DynamoDbAsyncClient.class);
    AccountCreatedEventHandler eventHandler = new AccountCreatedEventHandler(dynamoDbAsyncClient);
    AccountCreated event = new AccountCreated(UUID.randomUUID(), "John", "Smith");

    ArgumentCaptor<UpdateItemRequest> updateItemRequestArgumentCaptor =
        ArgumentCaptor.forClass(UpdateItemRequest.class);
    when(dynamoDbAsyncClient.updateItem(any(UpdateItemRequest.class)))
        .thenReturn(CompletableFuture.completedFuture(UpdateItemResponse.builder().build()));

    // Act
    eventHandler.handle(event).join();

    // Assert
    verify(dynamoDbAsyncClient, times(1)).updateItem(updateItemRequestArgumentCaptor.capture());
    UpdateItemRequest updateItemRequest = updateItemRequestArgumentCaptor.getValue();
    assertEquals("Account", updateItemRequest.tableName());
    assertEquals(1, updateItemRequest.key().size());
    assertEquals(event.getAccountId().toString(), updateItemRequest.key().get(ID_ATTRIBUTE).s());
    assertEquals(
        "SET #id = :id, #firstName = :firstName, #lastName = :lastName",
        updateItemRequest.updateExpression());

    Map<String, String> attributeNames = updateItemRequest.expressionAttributeNames();
    assertTrue(attributeNames.containsKey(FIRST_NAME_ATTRIBUTE));
    assertTrue(attributeNames.containsKey(LAST_NAME_ATTRIBUTE));
    assertTrue(attributeNames.containsKey(ID_ATTRIBUTE));
    assertEquals("#firstName", attributeNames.get(FIRST_NAME_ATTRIBUTE));
    assertEquals("#lastName", attributeNames.get(LAST_NAME_ATTRIBUTE));
    assertEquals("#id", attributeNames.get(ID_ATTRIBUTE));

    Map<String, AttributeValue> attributeValues = updateItemRequest.expressionAttributeValues();
    assertTrue(attributeValues.containsKey(":firstName"));
    assertTrue(attributeValues.containsKey(":lastName"));
    assertTrue(attributeValues.containsKey(":id"));
    assertEquals(event.getFirstName(), attributeValues.get(":firstName").s());
    assertEquals(event.getLastName(), attributeValues.get(":lastName").s());
    assertEquals(event.getAccountId().toString(), attributeValues.get(":id").s());
  }
}
