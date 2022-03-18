package com.aws.cqrs.serverless.function;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.amazonaws.services.lambda.runtime.events.SQSEvent.SQSMessage;
import com.aws.cqrs.application.*;
import com.aws.cqrs.domain.AccountCreated;
import com.aws.cqrs.domain.Deposited;
import com.aws.cqrs.domain.Overdrawn;
import com.aws.cqrs.domain.Withdrew;
import com.aws.cqrs.infrastructure.messaging.Event;
import com.google.gson.Gson;

public class PostMonitorAccountQueue implements RequestHandler<SQSEvent, Void> {

    @Override
    public Void handleRequest(SQSEvent event, Context context) {
        Gson gson = new Gson();

        for (SQSMessage msg : event.getRecords()) {

            try {
                String classType = msg.getMessageAttributes().get("messageType").getStringValue();
                Event domainEvent = (Event) gson.fromJson(msg.getBody(), Class.forName(classType));

                if (domainEvent instanceof AccountCreated) {
                    EventHandler<AccountCreated> handler = new AccountCreatedEventHandler();
                    handler.handle((AccountCreated) domainEvent);
                } else if(domainEvent instanceof Deposited) {
                    EventHandler<Deposited> handler = new DepositedEventHandler();
                    handler.handle((Deposited) domainEvent);
                } else if (domainEvent instanceof Withdrew) {
                    EventHandler<Withdrew> handler = new WithdrewEventHandler();
                    handler.handle((Withdrew) domainEvent);
                } else if (domainEvent instanceof Overdrawn) {
                    EventHandler<Overdrawn> handler = new OverdrawnEventHandler();
                    handler.handle((Overdrawn) domainEvent);
                }
            } catch (ClassNotFoundException e) {
                // TODO: Push to DLQ
            }
        }

        return null;
    }
}
