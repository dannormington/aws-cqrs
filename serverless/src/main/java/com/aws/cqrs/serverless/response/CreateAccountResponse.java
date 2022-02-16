package com.aws.cqrs.serverless.response;

import java.util.UUID;

public class CreateAccountResponse {
	private UUID accountId;

	public CreateAccountResponse(UUID accountId) {
		this.accountId = accountId;
	}

	public UUID getAccountId() {
		return accountId;
	}
}
