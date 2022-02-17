package com.aws.cqrs.core.persistence;

import java.util.*;

import com.aws.cqrs.core.exceptions.AggregateNotFoundException;
import com.aws.cqrs.core.exceptions.EventCollisionException;
import com.aws.cqrs.core.exceptions.HydrationException;
import com.aws.cqrs.core.messaging.Event;
import com.aws.cqrs.core.messaging.SqsEventBus;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.ConditionCheck;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
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
			throws EventCollisionException {

		DynamoDbTable<EventModel> eventStoreTable = enhancedClient.table(tableName, eventModelSchema);

		// Ensure that the Id and Version are unique
		//Expression expression = Expression.builder().expression("(attribute_not_exists(Id) and attribute_not_exists(Version))").build();
		//ConditionCheck<EventModel> conditionCheck = ConditionCheck.builder().conditionExpression(expression).build();
		//TransactWriteItemsEnhancedRequest.Builder requestBuilder = TransactWriteItemsEnhancedRequest.builder().addConditionCheck(eventStoreTable,conditionCheck);

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

			// Put the item in the request
			requestBuilder.addPutItem(eventStoreTable, eventModel);

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

		// Persist to the event store
		enhancedClient.transactWriteItems(requestBuilder.build());

		// Publish the events to the queue
		eventBus.publishEvents(entries);
	}

	@Override
	public Iterable<Event> getEvents(UUID aggregateId) throws HydrationException, AggregateNotFoundException {
		// Get the events by the aggregate Id.
		Iterator<EventModel> events = getAggregateEvents(aggregateId);

		// Deserialize the json from each domain event.
		return getDomainEvents(aggregateId, events);
	}

	/**
	 * Get all the records for a specific aggregate id.
	 * 
	 * @param aggregateId The aggregate Id.
	 * @return All the events for a specific aggregate Id.
	 * @throws AggregateNotFoundException
	 * @throws HydrationException
	 */
	private Iterator<EventModel> getAggregateEvents(UUID aggregateId)
			throws AggregateNotFoundException, HydrationException {

		DynamoDbTable<EventModel> eventStoreTable = enhancedClient.table(tableName, eventModelSchema);

		QueryConditional queryConditional = QueryConditional
				.keyEqualTo(Key.builder().partitionValue(aggregateId.toString()).build());

		try {
			return eventStoreTable.query(queryConditional).items().iterator();
		} catch (ResourceNotFoundException e) {
			throw new AggregateNotFoundException(e, aggregateId);
		} catch (DynamoDbException e) {
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
				 * Throw a hydration exception along with the aggregate Id and the message
				 */
				throw new HydrationException(e, aggregateId);
			}
		}

		return history;
	}
}
