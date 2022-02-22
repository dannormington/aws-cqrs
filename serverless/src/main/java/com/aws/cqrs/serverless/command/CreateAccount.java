package com.aws.cqrs.serverless.command;

import com.aws.cqrs.infrastructure.messaging.Command;

import java.io.Serializable;
import java.util.UUID;

public class CreateAccount implements Command, Serializable {
	private UUID accountId;
	private String firstName;
	private String lastName;

	public CreateAccount() {
	}

	public CreateAccount(UUID accountId, String firstName, String lastName) {
		this.accountId = accountId;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	public UUID getAccountId() { return accountId; }

	public void setAccountId(UUID accountId) { this.accountId = accountId; }

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
}
