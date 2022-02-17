package com.aws.cqrs.serverless.response;

import java.io.Serializable;
import java.util.UUID;

public class AccountWithdrawResponse implements Serializable {
    private UUID accountId;

    public AccountWithdrawResponse(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }
}
