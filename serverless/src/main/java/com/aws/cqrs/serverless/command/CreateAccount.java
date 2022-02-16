package com.aws.cqrs.serverless.command;

import com.aws.cqrs.core.messaging.Command;

public class CreateAccount implements Command {
	private String firstName;
	private String lastName;

	public CreateAccount() {
	}

	public CreateAccount(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
	}

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