package com.aws.cqrs.ddbconsumer.eventhandlers;

import com.aws.cqrs.application.EventHandler;
import com.aws.cqrs.domain.Overdrawn;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

public class OverdrawnEventHandler implements EventHandler<Overdrawn> {
  private final DynamoDbAsyncClient dynamoDbAsyncClient;

  public OverdrawnEventHandler(DynamoDbAsyncClient dynamoDbAsyncClient) {
    this.dynamoDbAsyncClient = dynamoDbAsyncClient;
  }

  @Override
  public CompletableFuture<Void> handle(Overdrawn event) {
    UpdateItemRequest updateItemRequest =
        UpdateItemRequest.builder()
            .tableName("Account")
            .key(
                Collections.singletonMap(
                    "AccountId",
                    AttributeValue.builder().s(event.getAccountId().toString()).build()))
            .updateExpression("SET #Balance :Balance")
            .expressionAttributeNames(Collections.singletonMap("Balance", "#Balance"))
            .expressionAttributeValues(
                Collections.singletonMap(
                    ":Balance",
                    AttributeValue.builder().n(event.getNewBalance().toString()).build()))
            .build();

    return dynamoDbAsyncClient.updateItem(updateItemRequest).thenAccept(x -> {});
  }
}
