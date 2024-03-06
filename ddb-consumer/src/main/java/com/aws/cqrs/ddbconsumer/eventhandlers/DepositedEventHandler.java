package com.aws.cqrs.ddbconsumer.eventhandlers;

import com.aws.cqrs.application.EventHandler;
import com.aws.cqrs.domain.Deposited;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class DepositedEventHandler implements EventHandler<Deposited> {
    private final DynamoDbAsyncClient dynamoDbAsyncClient;

    public DepositedEventHandler(DynamoDbAsyncClient dynamoDbAsyncClient) {
        this.dynamoDbAsyncClient = dynamoDbAsyncClient;
    }

    @Override
    public CompletableFuture<Void> handle(Deposited event) {
        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .tableName("Account")
                .key(Collections.singletonMap("AccountId", AttributeValue.builder().s(event.getAccountId().toString()).build()))
                .updateExpression("ADD #balance :balance")
                .expressionAttributeNames(Collections.singletonMap("Balance", "#balance"))
                .expressionAttributeValues(Collections.singletonMap(":balance", AttributeValue.builder().n(event.getAmount().toString()).build()))
                .build();

        return dynamoDbAsyncClient.updateItem(updateItemRequest).thenAccept(x -> {});
    }
}
