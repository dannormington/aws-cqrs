package com.aws.cqrs.infrastructure.messaging;

import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;

import java.util.List;

/**
 * Simple implementation to publish events to an SQS.
 */
public class SqsEventBus {
    private final String queueName;
    private final SqsClient client;
    private String queueUrl;

    public SqsEventBus(String queueName) {
        this.queueName = queueName;
        client = SqsClient.builder().build();
    }

    /**
     * Publish the events to an SQS.
     *
     * @param entries The list of messages to publish.
     */
    public void publishEvents(List<SendMessageBatchRequestEntry> entries) {
        if(entries.isEmpty()) return;

        if(queueUrl == null) {
            GetQueueUrlResponse getQueueUrlResponse = client
                    .getQueueUrl(GetQueueUrlRequest.builder().queueName(queueName).build());

            queueUrl = getQueueUrlResponse.queueUrl();
        }

        client.sendMessageBatch(SendMessageBatchRequest.builder().queueUrl(queueUrl).entries(entries).build());
    }
}
