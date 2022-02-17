package com.aws.cqrs.serverless.response;

import java.io.Serializable;
import java.util.UUID;

public class AccountDepositResponse implements Serializable {
    private UUID accountId;

    public AccountDepositResponse(UUID accountId) {
        this.accountId = accountId;
    }

    public UUID getAccountId() {
        return accountId;
    }
}
