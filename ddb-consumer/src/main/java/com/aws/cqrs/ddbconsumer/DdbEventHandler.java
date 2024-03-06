package com.aws.cqrs.ddbconsumer;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue;
import com.aws.cqrs.infrastructure.messaging.Event;
import com.google.gson.Gson;

import javax.inject.Inject;
import java.util.ArrayList;
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

        Map<String, List<DynamodbEvent.DynamodbStreamRecord>> groupedRecords = dynamodbEvent.getRecords().stream().collect(Collectors.groupingBy(record -> record.getDynamodb().getKeys().get("Id").getS()));

        List<CompletableFuture<Void>> cfs = new ArrayList<>();

        groupedRecords.entrySet().forEach(entry -> cfs.add(processEvents(entry.getKey(), entry.getValue())));

        return CompletableFuture.allOf(cfs.toArray(new CompletableFuture[0])).join();
    }

    private CompletableFuture<Void> processEvents(String id, List<DynamodbEvent.DynamodbStreamRecord> records) {
        return CompletableFuture.supplyAsync(() -> {
            records.forEach(record -> {
                Map<String, AttributeValue> attributes = record.getDynamodb().getNewImage();
                String kind = attributes.get("Kind").getS();
                String eventData = attributes.get("Event").getS();

                try {
                    Event event = (Event)gson.fromJson(eventData, Class.forName(kind));
                    eventBus.handle(event).join();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });

            return null;
        });
    }
}
