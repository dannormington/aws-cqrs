package com.aws.cqrs.ddbconsumer.eventhandlers;

import static com.aws.cqrs.ddbconsumer.eventhandlers.AccountAttributes.BALANCE_ATTRIBUTE;
import static com.aws.cqrs.ddbconsumer.eventhandlers.AccountAttributes.ID_ATTRIBUTE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import com.aws.cqrs.domain.Withdrew;
import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

class WithdrewEventHandlerTest {
  @Test
  void when_handle_expect_success() {
    // Arrange
    DynamoDbAsyncClient dynamoDbAsyncClient = mock(DynamoDbAsyncClient.class);
    WithdrewEventHandler eventHandler = new WithdrewEventHandler(dynamoDbAsyncClient);
    Withdrew event = new Withdrew(UUID.randomUUID(), new BigDecimal(100), new BigDecimal(0));

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
    assertEquals("SET #balance :balance", updateItemRequest.updateExpression());

    Map<String, String> attributeNames = updateItemRequest.expressionAttributeNames();
    assertTrue(attributeNames.containsKey(BALANCE_ATTRIBUTE));
    assertEquals("#balance", attributeNames.get(BALANCE_ATTRIBUTE));

    Map<String, AttributeValue> attributeValues = updateItemRequest.expressionAttributeValues();
    assertTrue(attributeValues.containsKey(":balance"));
    assertEquals(event.getNewBalance(), new BigDecimal(attributeValues.get(":balance").n()));
  }
}
