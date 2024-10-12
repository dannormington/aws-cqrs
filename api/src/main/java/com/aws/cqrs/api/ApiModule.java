package com.aws.cqrs.api;

import com.aws.cqrs.application.AccountService;
import com.aws.cqrs.domain.Account;
import com.aws.cqrs.infrastructure.persistence.DynamoDbEventStore;
import com.aws.cqrs.infrastructure.persistence.EventRepository;
import com.aws.cqrs.infrastructure.persistence.EventStore;
import com.aws.cqrs.infrastructure.persistence.Repository;
import com.google.gson.Gson;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;

import javax.inject.Singleton;

@Module
public class ApiModule {

    @Singleton
    @Provides
    public Gson provideGson() {
        return new Gson();
    }

    @Singleton
    @Provides
    public DynamoDbAsyncClient provideDynamoDbAsyncClient() {
        return DynamoDbAsyncClient.create();
    }

    @Singleton
    @Provides
    public AccountService provideAccountService(DynamoDbAsyncClient dynamoDbAsyncClient, Gson gson) {
        String eventStoreTable = System.getenv("eventStoreTable");
        EventStore eventStore = new DynamoDbEventStore(eventStoreTable, dynamoDbAsyncClient, gson);
        Repository<Account> accountRepository = new EventRepository<>(Account.class, eventStore);
        return new AccountService(accountRepository);
    }
}
