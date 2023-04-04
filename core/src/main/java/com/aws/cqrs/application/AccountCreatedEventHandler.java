package com.aws.cqrs.application;

import com.aws.cqrs.domain.AccountCreated;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AccountCreatedEventHandler implements EventHandler<AccountCreated> {

    @Override
    public CompletableFuture<Void> handle(AccountCreated event) {
        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put(":AccountId", AttributeValue.builder().s(event.getAccountId().toString()).build());
        attributeValues.put(":FirstName", AttributeValue.builder().s(event.getFirstName()).build());
        attributeValues.put(":LastName", AttributeValue.builder().s(event.getLastName()).build());

        Map<String, String> attributeNames = new HashMap<>();
        attributeNames.put("AccountId", "#AccountId");
        attributeNames.put("FirstName", "#FirstName");
        attributeNames.put("LastName", "#LastName");

        UpdateItemRequest updateItemRequest = UpdateItemRequest.builder()
                .tableName("Account")
                .key(Collections.singletonMap("AccountId",AttributeValue.builder().s(event.getAccountId().toString()).build()))
                .updateExpression("SET #AccountId = :AccountId, #FirstName = :FirstName, #LastName = :LastName")
                .expressionAttributeNames(attributeNames)
                .expressionAttributeValues(attributeValues)
                .build();

        DynamoDbAsyncClient dynamoDbAsyncClient = DynamoDbAsyncClient.create();
        return dynamoDbAsyncClient.updateItem(updateItemRequest).thenAccept(x -> {});
    }
}
