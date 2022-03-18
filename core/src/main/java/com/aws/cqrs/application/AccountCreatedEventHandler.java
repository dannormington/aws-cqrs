package com.aws.cqrs.application;

import com.aws.cqrs.domain.AccountCreated;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.TransactWriteItemsEnhancedRequest;

import java.math.BigDecimal;

public class AccountCreatedEventHandler implements EventHandler<AccountCreated> {

    @Override
    public void handle(AccountCreated event) {
        // Create the new account read model.
        AccountModel account = new AccountModel();
        account.setAccountId(event.getAccountId().toString());
        account.setFirstName(event.getFirstName());
        account.setLastName(event.getLastName());
        account.setBalance(BigDecimal.ZERO);

        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.create();
        DynamoDbTable<AccountModel> accountTable = enhancedClient.table("CustomerAccount", TableSchema.fromBean(AccountModel.class));
        TransactWriteItemsEnhancedRequest.Builder requestBuilder = TransactWriteItemsEnhancedRequest.builder();

        PutItemEnhancedRequest<AccountModel> request = PutItemEnhancedRequest.builder(AccountModel.class)
                .item(account)
                .conditionExpression(Expression.builder()
                        .expression("attribute_not_exists(#id)")
                        .putExpressionName("#id", "AccountId")
                        .build())
                .build();

        requestBuilder.addPutItem(accountTable, request);

        enhancedClient.transactWriteItems(requestBuilder.build());
    }
}
