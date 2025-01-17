package com.aws.cqrs.infrastructure.persistence;

import com.aws.cqrs.infrastructure.exceptions.AggregateNotFoundException;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.exceptions.TransactionFailedException;
import com.aws.cqrs.infrastructure.messaging.Event;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class DynamoDbEventStore implements EventStore {
  public static String ID_ATTRIBUTE = "id";
  public static String VERSION_ATTRIBUTE = "version";
  public static String EVENT_ATTRIBUTE = "event";
  public static String KIND_ATTRIBUTE = "kind";

  private final String tableName;
  private final DynamoDbAsyncClient ddbClient;
  private final Gson gson;

  public DynamoDbEventStore(String tableName, DynamoDbAsyncClient ddbClient, Gson gson) {
    this.tableName = tableName;
    this.ddbClient = ddbClient;
    this.gson = gson;
  }

  @Override
  public CompletableFuture<Void> saveEvents(
      UUID aggregateId, long expectedVersion, List<Event> events) {

    List<TransactWriteItem> transactWriteItems = new ArrayList<>();
    TransactWriteItemsRequest.Builder requestBuilder = TransactWriteItemsRequest.builder();

    for (Event event : events) {
      expectedVersion++;

      Map<String, AttributeValue> propertyMap = new HashMap<>();
      propertyMap.put(ID_ATTRIBUTE, AttributeValue.builder().s(aggregateId.toString()).build());
      propertyMap.put(
          VERSION_ATTRIBUTE, AttributeValue.builder().n(String.valueOf(expectedVersion)).build());
      propertyMap.put(EVENT_ATTRIBUTE, AttributeValue.builder().s(gson.toJson(event)).build());
      propertyMap.put(
          KIND_ATTRIBUTE, AttributeValue.builder().s(event.getClass().getName()).build());

      // Create a new request
      Put put = Put.builder().item(propertyMap).tableName(tableName).build();

      transactWriteItems.add(TransactWriteItem.builder().put(put).build());
    }

    return ddbClient
        .transactWriteItems(requestBuilder.transactItems(transactWriteItems).build())
        .exceptionally(
            exception -> {
              throw new TransactionFailedException(exception, aggregateId);
            })
        .thenAccept(x -> {});
  }

  @Override
  public CompletableFuture<List<Event>> getEvents(UUID aggregateId)
      throws HydrationException, AggregateNotFoundException {
    // Get the events by the aggregate id.
    return getAggregateEvents(aggregateId)
        .thenApply(
            events -> {
              // Deserialize the json from each domain event.
              return getDomainEvents(aggregateId, events);
            });
  }

  /**
   * Get all the records for a specific aggregate id.
   *
   * @param aggregateId The aggregate id.
   * @return All the events for a specific aggregate id.
   * @throws AggregateNotFoundException
   * @throws HydrationException
   */
  private CompletableFuture<List<Map<String, AttributeValue>>> getAggregateEvents(UUID aggregateId)
      throws AggregateNotFoundException, HydrationException {

    QueryRequest queryRequest =
        QueryRequest.builder()
            .consistentRead(true)
            .tableName(tableName)
            .keyConditionExpression("#id = :id")
            .expressionAttributeNames(Collections.singletonMap("#id", ID_ATTRIBUTE))
            .expressionAttributeValues(
                Collections.singletonMap(
                    ":id", AttributeValue.builder().s(aggregateId.toString()).build()))
            .build();

    return ddbClient
        .query(queryRequest)
        .exceptionally(
            x -> {
              throw new HydrationException(x, aggregateId);
            })
        .thenApply(
            response -> {
              if (response.hasItems()) {
                return response.items().stream().collect(Collectors.toUnmodifiableList());
              }
              throw new AggregateNotFoundException(aggregateId);
            });
  }

  /**
   * Loop through all the events and deserialize the json into their respective types.
   *
   * @param aggregateId The aggregate's id.
   * @param eventModels The list of events.
   * @return
   * @throws HydrationException
   */
  private List<Event> getDomainEvents(
      UUID aggregateId, List<Map<String, AttributeValue>> eventModels) throws HydrationException {

    return eventModels.stream()
        .map(
            attributeValueMap -> {
              try {
                Class<?> aClass = Class.forName(attributeValueMap.get(KIND_ATTRIBUTE).s());
                return (Event) gson.fromJson(attributeValueMap.get(EVENT_ATTRIBUTE).s(), aClass);
              } catch (JsonSyntaxException | ClassNotFoundException e) {
                /*
                 * Throw a hydration exception along with the aggregate id and the message
                 */
                throw new HydrationException(e, aggregateId);
              }
            })
        .collect(Collectors.toUnmodifiableList());
  }
}
