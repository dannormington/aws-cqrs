package com.aws.cqrs.ddbconsumer.eventhandlers;

import static com.aws.cqrs.ddbconsumer.eventhandlers.AccountAttributes.*;

import com.aws.cqrs.application.EventHandler;
import com.aws.cqrs.domain.AccountCreated;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class AccountCreatedEventHandler implements EventHandler<AccountCreated> {
  private final DynamoDbAsyncClient dynamoDbAsyncClient;

  public AccountCreatedEventHandler(DynamoDbAsyncClient dynamoDbAsyncClient) {
    this.dynamoDbAsyncClient = dynamoDbAsyncClient;
  }

  @Override
  public CompletableFuture<Void> handle(AccountCreated event) {
    Map<String, AttributeValue> attributeValues = new HashMap<>();
    attributeValues.put(":id", AttributeValue.builder().s(event.getAccountId().toString()).build());
    attributeValues.put(":firstName", AttributeValue.builder().s(event.getFirstName()).build());
    attributeValues.put(":lastName", AttributeValue.builder().s(event.getLastName()).build());

    Map<String, String> attributeNames = new HashMap<>();
    attributeNames.put(ID_ATTRIBUTE, "#id");
    attributeNames.put(FIRST_NAME_ATTRIBUTE, "#firstName");
    attributeNames.put(LAST_NAME_ATTRIBUTE, "#lastName");

    UpdateItemRequest updateItemRequest =
        UpdateItemRequest.builder()
            .tableName("Account")
            .key(
                Collections.singletonMap(
                    ID_ATTRIBUTE,
                    AttributeValue.builder().s(event.getAccountId().toString()).build()))
            .updateExpression("SET #id = :id, #firstName = :firstName, #lastName = :lastName")
            .expressionAttributeNames(attributeNames)
            .expressionAttributeValues(attributeValues)
            .build();

    return dynamoDbAsyncClient.updateItem(updateItemRequest).thenAccept(x -> {});
  }
}
