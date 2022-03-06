package com.aws.cqrs.infrastructure.persistence;

import java.util.*;

import com.aws.cqrs.infrastructure.exceptions.AggregateNotFoundException;
import com.aws.cqrs.infrastructure.exceptions.EventCollisionException;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.exceptions.TransactionFailedException;
import com.aws.cqrs.infrastructure.messaging.Event;
import com.aws.cqrs.infrastructure.messaging.SqsEventBus;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.sqs.model.*;

public class DynamoDbEventStore implements EventStore {
    private final String tableName;
    private final DynamoDbEnhancedClient enhancedClient;
    private final Gson gson = new Gson();
    private final SqsEventBus eventBus;
    private static final TableSchema<EventModel> eventModelSchema = TableSchema.fromBean(EventModel.class);

    public DynamoDbEventStore(String tableName, String queueName) {
        this.tableName = tableName;
        enhancedClient = DynamoDbEnhancedClient.create();
        eventBus = new SqsEventBus(queueName);
    }

    @Override
    public void saveEvents(UUID aggregateId, long expectedVersion, Iterable<Event> events)
            throws TransactionFailedException {

        DynamoDbTable<EventModel> eventStoreTable = enhancedClient.table(tableName, eventModelSchema);
        TransactWriteItemsEnhancedRequest.Builder requestBuilder = TransactWriteItemsEnhancedRequest.builder();

        // Add each event to the batch.
        List<SendMessageBatchRequestEntry> entries = new ArrayList<>();
        String groupId = UUID.randomUUID().toString();

        for (Event event : events) {
            expectedVersion++;

            EventModel eventModel = new EventModel();
            eventModel.setId(aggregateId.toString());
            eventModel.setVersion(expectedVersion);
            eventModel.setEvent(gson.toJson(event));
            eventModel.setKind(event.getClass().getName());

            // Create a new request
            PutItemEnhancedRequest<EventModel> request = PutItemEnhancedRequest.builder(EventModel.class)
                    .item(eventModel)
                    .conditionExpression(Expression.builder()
                            .expression("attribute_not_exists(#id)")
                            .putExpressionName("#id", "Id")
                            .build())
                    .build();

            requestBuilder.addPutItem(eventStoreTable, request);


            Map<String, MessageAttributeValue> attributes = new HashMap<>();
            attributes.put("messageType", MessageAttributeValue.builder().dataType("String").stringValue(eventModel.getKind()).build());

            // Create an SQS message and add it to the list.
            entries.add(SendMessageBatchRequestEntry.builder()
                    .id(UUID.randomUUID().toString())
                    .messageDeduplicationId(UUID.randomUUID().toString())
                    .messageGroupId(groupId)
                    .messageAttributes(attributes)
                    .messageBody(eventModel.getEvent()).build());
        }

        try {
            // Persist to the event store
            enhancedClient.transactWriteItems(requestBuilder.build());

            // Publish the events to the queue
            eventBus.publishEvents(entries);
        } catch (ConditionalCheckFailedException e) {
            throw new EventCollisionException(e, aggregateId);
        } catch (TransactionCanceledException e) {
            if (e.hasCancellationReasons()) {
                if (e.cancellationReasons().stream().anyMatch(x -> x.code().equals("ConditionalCheckFailed"))) {
                    throw new EventCollisionException(e, aggregateId);
                }

                //TODO: Add cases for other reasons.
                throw new TransactionFailedException(e, e.cancellationReasons().get(0).code(), aggregateId);
            }

            // Since there isn't a reason simply throw a transaction failed.
            throw new TransactionFailedException(e, aggregateId);
        }
    }

    @Override
    public Iterable<Event> getEvents(UUID aggregateId) throws HydrationException, AggregateNotFoundException {
        // Get the events by the aggregate id.
        Iterator<EventModel> events = getAggregateEvents(aggregateId);

        // Deserialize the json from each domain event.
        return getDomainEvents(aggregateId, events);
    }

    /**
     * Get all the records for a specific aggregate id.
     *
     * @param aggregateId The aggregate id.
     * @return All the events for a specific aggregate id.
     * @throws AggregateNotFoundException
     * @throws HydrationException
     */
    private Iterator<EventModel> getAggregateEvents(UUID aggregateId)
            throws AggregateNotFoundException, HydrationException {

        DynamoDbTable<EventModel> eventStoreTable = enhancedClient.table(tableName, eventModelSchema);

        QueryConditional queryConditional = QueryConditional
                .keyEqualTo(Key.builder().partitionValue(aggregateId.toString()).build());

        QueryEnhancedRequest request = QueryEnhancedRequest.builder()
                .queryConditional(queryConditional).consistentRead(true).build();

        try {
            PageIterable<EventModel> results = eventStoreTable.query(request);

            if (results.items().stream().findFirst().isPresent()) {
                return results.items().iterator();
            } else {
                throw new AggregateNotFoundException(aggregateId);
            }
        } catch (AggregateNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new HydrationException(e, aggregateId);
        }
    }

    /**
     * Loop through all the events and deserialize the json into their respective
     * types.
     *
     * @param aggregateId The aggregate's id.
     * @param eventModels The list of events.
     * @return
     * @throws HydrationException
     */
    private List<Event> getDomainEvents(UUID aggregateId, Iterator<EventModel> eventModels)
            throws HydrationException {

        List<Event> history = new ArrayList<>();

        while (eventModels.hasNext()) {
            EventModel eventModel = eventModels.next();

            try {
                Event event = (Event) gson.fromJson(eventModel.getEvent(), Class.forName(eventModel.getKind()));
                history.add(event);
            } catch (JsonSyntaxException | ClassNotFoundException e) {
                /*
                 * Throw a hydration exception along with the aggregate id and the message
                 */
                throw new HydrationException(e, aggregateId);
            }
        }

        return history;
    }
}
