package com.aws.cqrs.ddbconsumer;

import com.aws.cqrs.application.OffsetDateTimeDeserializer;
import com.aws.cqrs.ddbconsumer.eventhandlers.AccountCreatedEventHandler;
import com.aws.cqrs.ddbconsumer.eventhandlers.DepositedEventHandler;
import com.aws.cqrs.ddbconsumer.eventhandlers.OverdrawnEventHandler;
import com.aws.cqrs.ddbconsumer.eventhandlers.WithdrewEventHandler;
import com.aws.cqrs.domain.AccountCreated;
import com.aws.cqrs.domain.Deposited;
import com.aws.cqrs.domain.Overdrawn;
import com.aws.cqrs.domain.Withdrew;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dagger.Module;
import dagger.Provides;
import java.time.OffsetDateTime;
import javax.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

@Module
public class DdbConsumerModule {

  @Provides
  @Singleton
  public DynamoDbAsyncClient provideDynamoDbAsyncClient() {
    return DynamoDbAsyncClient.create();
  }

  @Provides
  @Singleton
  public Gson provideGson() {
    return new GsonBuilder()
        .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeDeserializer())
        .create();
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
