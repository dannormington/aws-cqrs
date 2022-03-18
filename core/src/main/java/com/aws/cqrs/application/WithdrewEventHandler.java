package com.aws.cqrs.application;

import com.aws.cqrs.domain.Deposited;
import com.aws.cqrs.domain.Withdrew;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;

import java.util.List;

public class WithdrewEventHandler implements EventHandler<Withdrew> {

    @Override
    public void handle(Withdrew event) {
        DynamoDbEnhancedClient enhancedClient = DynamoDbEnhancedClient.create();
        DynamoDbTable<AccountModel> accountTable = enhancedClient.table("CustomerAccount", TableSchema.fromBean(AccountModel.class));

        GetItemEnhancedRequest getItemEnhancedRequest = GetItemEnhancedRequest.builder()
                .consistentRead(true)
                .key(Key.builder().partitionValue(event.getAccountId().toString()).build()).build();

        TransactGetItemsEnhancedRequest getRequest = TransactGetItemsEnhancedRequest.builder()
                .addGetItem(accountTable, getItemEnhancedRequest).build();

        List<Document> results = enhancedClient.transactGetItems(getRequest);

        if(results.isEmpty()) return;

        AccountModel account = results.get(0).getItem(accountTable);
        account.setBalance(account.getBalance().subtract(event.getAmount()));

        TransactWriteItemsEnhancedRequest writeRequest = TransactWriteItemsEnhancedRequest.builder()
                .addPutItem(accountTable, account).build();

        enhancedClient.transactWriteItems(writeRequest);
    }
}
