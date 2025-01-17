package com.aws.cqrs.ddbconsumer;

import static com.aws.cqrs.infrastructure.persistence.DynamoDbEventStore.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.aws.cqrs.ddbconsumer.exceptions.DeserializationException;
import com.aws.cqrs.infrastructure.messaging.Event;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import javax.inject.Inject;

public class DdbEventHandler implements RequestHandler<DynamodbEvent, Void> {
  private final Gson gson;
  private final EventBus eventBus;

  @Inject
  public DdbEventHandler(EventBus eventBus, Gson gson) {
    this.eventBus = eventBus;
    this.gson = gson;
  }

  @Override
  public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {

    // Group the events by the aggregate id.
    Map<String, List<DynamodbEvent.DynamodbStreamRecord>> groupedRecords =
        dynamodbEvent.getRecords().stream()
            .collect(
                Collectors.groupingBy(
                    record -> record.getDynamodb().getKeys().get(ID_ATTRIBUTE).getS()));

    // Process each aggregate asynchronously while maintaining order within the aggregate.
    List<CompletableFuture<Void>> futures =
        groupedRecords.values().stream().map(events -> processEvents(events)).toList();

    return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
  }

  private CompletableFuture<Void> processEvents(List<DynamodbEvent.DynamodbStreamRecord> records) {
    return CompletableFuture.supplyAsync(
        () -> {
          records.forEach(
              record -> {
                Map<String, AttributeValue> attributes = record.getDynamodb().getNewImage();
                String kind = attributes.get(KIND_ATTRIBUTE).getS();
                String eventData = attributes.get(EVENT_ATTRIBUTE).getS();

                try {
                  Event event = (Event) gson.fromJson(eventData, Class.forName(kind));
                  // Purposely handling these events synchronously as order matters
                  eventBus.handle(event).join();
                } catch (ClassNotFoundException e) {
                  throw new DeserializationException(e);
                }
              });

          return null;
        });
  }
}
