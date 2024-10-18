package com.aws.cqrs.ddbconsumer.eventhandlers;

import com.aws.cqrs.domain.Overdrawn;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class OverdrawnEventHandlerTest {
    @Test
    void when_handle_expect_success() {
        // Arrange
        DynamoDbAsyncClient dynamoDbAsyncClient = mock(DynamoDbAsyncClient.class);
        OverdrawnEventHandler eventHandler = new OverdrawnEventHandler(dynamoDbAsyncClient);
        Overdrawn event = new Overdrawn(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal(10), new BigDecimal(-20));

        ArgumentCaptor<UpdateItemRequest> updateItemRequestArgumentCaptor = ArgumentCaptor.forClass(UpdateItemRequest.class);
        when(dynamoDbAsyncClient.updateItem(any(UpdateItemRequest.class))).thenReturn(CompletableFuture.completedFuture(UpdateItemResponse.builder().build()));

        // Act
        eventHandler.handle(event).join();

        // Assert
        verify(dynamoDbAsyncClient, times(1)).updateItem(updateItemRequestArgumentCaptor.capture());
        UpdateItemRequest updateItemRequest = updateItemRequestArgumentCaptor.getValue();
        assertEquals("Account", updateItemRequest.tableName());
        assertEquals(1, updateItemRequest.key().size());
        assertEquals(event.getAccountId().toString(), updateItemRequest.key().get("AccountId").s());
        assertEquals("SET #Balance :Balance", updateItemRequest.updateExpression());

        Map<String, String> attributeNames = updateItemRequest.expressionAttributeNames();
        assertTrue(attributeNames.containsKey("Balance"));
        assertEquals("#Balance", attributeNames.get("Balance"));

        Map<String, AttributeValue> attributeValues = updateItemRequest.expressionAttributeValues();
        assertTrue(attributeValues.containsKey(":Balance"));
        assertEquals(event.getNewBalance(), new BigDecimal(attributeValues.get(":Balance").n()));
    }
}