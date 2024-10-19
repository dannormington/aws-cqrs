package com.aws.cqrs.ddbconsumer.eventhandlers;

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
    assertEquals(event.getAccountId().toString(), updateItemRequest.key().get("AccountId").s());
    assertEquals(
        "SET #AccountId = :AccountId, #FirstName = :FirstName, #LastName = :LastName",
        updateItemRequest.updateExpression());

    Map<String, String> attributeNames = updateItemRequest.expressionAttributeNames();
    assertTrue(attributeNames.containsKey("FirstName"));
    assertTrue(attributeNames.containsKey("LastName"));
    assertTrue(attributeNames.containsKey("AccountId"));
    assertEquals("#FirstName", attributeNames.get("FirstName"));
    assertEquals("#LastName", attributeNames.get("LastName"));
    assertEquals("#AccountId", attributeNames.get("AccountId"));

    Map<String, AttributeValue> attributeValues = updateItemRequest.expressionAttributeValues();
    assertTrue(attributeValues.containsKey(":FirstName"));
    assertTrue(attributeValues.containsKey(":LastName"));
    assertTrue(attributeValues.containsKey(":AccountId"));
    assertEquals(event.getFirstName(), attributeValues.get(":FirstName").s());
    assertEquals(event.getLastName(), attributeValues.get(":LastName").s());
    assertEquals(event.getAccountId().toString(), attributeValues.get(":AccountId").s());
  }
}
