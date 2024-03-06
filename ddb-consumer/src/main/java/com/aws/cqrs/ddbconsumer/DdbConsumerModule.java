package com.aws.cqrs.ddbconsumer;

import com.aws.cqrs.ddbconsumer.eventhandlers.AccountCreatedEventHandler;
import com.aws.cqrs.ddbconsumer.eventhandlers.DepositedEventHandler;
import com.aws.cqrs.ddbconsumer.eventhandlers.OverdrawnEventHandler;
import com.aws.cqrs.ddbconsumer.eventhandlers.WithdrewEventHandler;
import com.aws.cqrs.domain.AccountCreated;
import com.aws.cqrs.domain.Deposited;
import com.aws.cqrs.domain.Overdrawn;
import com.aws.cqrs.domain.Withdrew;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import javax.inject.Singleton;

@Module
public class DdbConsumerModule {

    @Provides
    @Singleton
    public DynamoDbAsyncClient provideDynamoDbAsyncClient() {
        return DynamoDbAsyncClient.create();
    }

    @Provides
    @Singleton
    public EventBus provideDomainEventHandler(DynamoDbAsyncClient dynamoDbAsyncClient) {
        EventBus eventBus = new EventBus();
        eventBus.register(AccountCreated.class, new AccountCreatedEventHandler(dynamoDbAsyncClient));
        eventBus.register(Deposited.class, new DepositedEventHandler(dynamoDbAsyncClient));
        eventBus.register(Withdrew.class, new WithdrewEventHandler(dynamoDbAsyncClient));
        eventBus.register(Overdrawn.class, new OverdrawnEventHandler(dynamoDbAsyncClient));
        return eventBus;
    }
}
