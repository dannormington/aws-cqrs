package com.aws.cqrs.ddbconsumer;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

class DdbEventHandlerTest {

    @Test
    void when_handleRequest_no_events_expect_success() {
        // Arrange
        EventBus eventBus = mock(EventBus.class);
        DdbEventHandler ddbEventHandler = new DdbEventHandler(eventBus);
        DynamodbEvent dynamodbEvent = new DynamodbEvent();
        dynamodbEvent.setRecords(new ArrayList<>());

        // Act
        ddbEventHandler.handleRequest(dynamodbEvent, null);

        // Assert
        verifyNoInteractions(eventBus);
    }
}