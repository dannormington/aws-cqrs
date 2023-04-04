package com.aws.cqrs.application;

import com.aws.cqrs.domain.Withdrew;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class WithdrewEventHandler implements EventHandler<Withdrew> {

    @Override
    public CompletableFuture<Void> handle(Withdrew event) {
        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .tableName("Account")
                .key(Collections.singletonMap("AccountId", AttributeValue.builder().s(event.getAccountId().toString()).build()))
                .updateExpression("ADD #balance :balance")
                .expressionAttributeNames(Collections.singletonMap("Balance", "#balance"))
                .expressionAttributeValues(Collections.singletonMap(":balance", AttributeValue.builder().n(event.getAmount().negate().toString()).build()))
                .build();

        DynamoDbAsyncClient dynamoDbAsyncClient = DynamoDbAsyncClient.create();
        return dynamoDbAsyncClient.updateItem(updateItemRequest).thenAccept(x -> {});
    }
}
