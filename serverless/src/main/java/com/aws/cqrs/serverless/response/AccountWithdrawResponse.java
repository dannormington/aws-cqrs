package com.aws.cqrs.serverless.response;

import java.util.UUID;

public class AccountWithdrawResponse {
    private UUID accountId;

    public AccountWithdrawResponse(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }
}
