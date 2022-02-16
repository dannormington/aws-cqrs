package com.aws.cqrs.serverless.response;

import java.util.UUID;

public class AccountDepositResponse {
    private UUID accountId;

    public AccountDepositResponse(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }
}
