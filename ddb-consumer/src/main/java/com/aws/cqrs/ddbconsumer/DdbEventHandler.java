package com.aws.cqrs.ddbconsumer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.aws.cqrs.ddbconsumer.exceptions.DeserializationException;
import com.aws.cqrs.infrastructure.messaging.Event;
import com.google.gson.Gson;

import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class DdbEventHandler implements RequestHandler<DynamodbEvent, Void> {
    private final Gson gson = new Gson();
    private final EventBus eventBus;

    @Inject
    public DdbEventHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public Void handleRequest(DynamodbEvent dynamodbEvent, Context context) {

        // Group the events by the aggregate id.
        Map<String, List<DynamodbEvent.DynamodbStreamRecord>> groupedRecords = dynamodbEvent.getRecords().stream().collect(Collectors.groupingBy(record -> record.getDynamodb().getKeys().get("Id").getS()));

        // Process each aggregate asynchronously while maintaining order within the aggregate.
        List<CompletableFuture<Void>> futures = groupedRecords.entrySet().stream().map(entry -> processEvents(entry.getKey(), entry.getValue())).collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private CompletableFuture<Void> processEvents(String id, List<DynamodbEvent.DynamodbStreamRecord> records) {
        return CompletableFuture.supplyAsync(() -> {
            records.forEach(record -> {
                Map<String, AttributeValue> attributes = record.getDynamodb().getNewImage();
                String kind = attributes.get("Kind").getS();
                String eventData = attributes.get("Event").getS();

                try {
                    Event event = (Event)gson.fromJson(eventData, Class.forName(kind));
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
