package com.aws.cqrs.core.persistence;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.aws.cqrs.core.exceptions.AggregateNotFoundException;
import com.aws.cqrs.core.exceptions.EventCollisionException;
import com.aws.cqrs.core.exceptions.HydrationException;
import com.aws.cqrs.core.messaging.Event;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;

public class DynamoDbEventStore implements EventStore {

	/**
	 * The name of the table to persist the events.
	 */
	private final String tableName;

	/**
	 * the name of the queue to publish events to.
	 */
	private final String queueName;

	/**
	 * The enhanced DB client
	 */
	private final DynamoDbEnhancedClient enhancedClient;

	/**
	 * The gson instance.
	 */
	private final Gson gson;

	public DynamoDbEventStore(String tableName, String queueName) {
		this.tableName = tableName;
		this.queueName = queueName;
		this.gson = new Gson();
		enhancedClient = DynamoDbEnhancedClient.create();
	}

	@Override
	public void saveEvents(UUID aggregateId, long expectedVersion, Iterable<Event> events)
			throws EventCollisionException {

		DynamoDbTable<EventModel> eventStoreTable = enhancedClient.table(tableName,
				TableSchema.fromBean(EventModel.class));

		//Expression expression = Expression.builder().expression("(attribute_not_exists(Id) and attribute_not_exists(Version))").build();
		//ConditionCheck<EventModel> conditionCheck = ConditionCheck.builder().conditionExpression(expression).build();
		//TransactWriteItemsEnhancedRequest.Builder requestBuilder = TransactWriteItemsEnhancedRequest.builder().addConditionCheck(eventStoreTable,conditionCheck);
		TransactWriteItemsEnhancedRequest.Builder requestBuilder = TransactWriteItemsEnhancedRequest.builder();

		// Add each event to the batch.
		List<SendMessageBatchRequestEntry> entries = new ArrayList<SendMessageBatchRequestEntry>();
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

			// Create an SQS message and add it to the list.
			entries.add(SendMessageBatchRequestEntry.builder()
					.id(UUID.randomUUID().toString())
					.messageDeduplicationId(UUID.randomUUID().toString())
					.messageGroupId(groupId)
					.messageBody(eventModel.getEvent()).build());
		}

		enhancedClient.transactWriteItems(requestBuilder.build());

		publishEvents(entries);
	}

	@Override
	public Iterable<Event> getEvents(UUID aggregateId) throws HydrationException, AggregateNotFoundException {
		// Get the events by the aggregate Id.
		Iterator<EventModel> events = getAggregateEvents(aggregateId);

		// Deserialize the json from each domain event.
		return hydrateDomainEvents(aggregateId, events);
	}

	/**
	 * Get all of the records for a specific aggregate id.
	 * 
	 * @param aggregateId
	 * @return
	 * @throws AggregateNotFoundException
	 * @throws HydrationException
	 */
	private Iterator<EventModel> getAggregateEvents(UUID aggregateId)
			throws AggregateNotFoundException, HydrationException {

		DynamoDbTable<EventModel> eventStoreTable = enhancedClient.table(tableName,
				TableSchema.fromBean(EventModel.class));

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
	 * Loop through all of the events and deserialize the json into their respective
	 * types.
	 * 
	 * @params aggregateId
	 * @param eventModels
	 * @return
	 * @throws HydrationException
	 */
	private List<Event> hydrateDomainEvents(UUID aggregateId, Iterator<EventModel> eventModels)
			throws HydrationException {

		List<Event> history = new ArrayList<Event>();

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

	/**
	 * Publish the events to the SQS.
	 * 
	 * @param entries
	 */
	private void publishEvents(List<SendMessageBatchRequestEntry> entries) {
		if(entries.isEmpty()) return;

		// Create the client.
		SqsClient client = SqsClient.builder().build();

		// Get the queue URL.
		GetQueueUrlResponse getQueueUrlResponse = client
				.getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());

		String queueUrl = getQueueUrlResponse.queueUrl();

		// Send the batch.
		client.sendMessageBatch(SendMessageBatchRequest.builder().queueUrl(queueUrl).entries(entries).build());
	}
}
