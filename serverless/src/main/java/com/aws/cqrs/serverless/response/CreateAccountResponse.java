package com.aws.cqrs.serverless.response;

import java.io.Serializable;
import java.util.UUID;

public class CreateAccountResponse implements Serializable {
	private UUID accountId;

	public CreateAccountResponse(UUID accountId) {
		this.accountId = accountId;
	}

	public UUID getAccountId() {
		return accountId;
	}
}
