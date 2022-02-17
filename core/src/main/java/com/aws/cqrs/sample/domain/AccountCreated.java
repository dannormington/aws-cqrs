package com.aws.cqrs.sample.domain;

import java.io.Serializable;
import java.util.UUID;

import com.aws.cqrs.core.messaging.Event;

/**
 * Event that is published when an account is created.
 */

public class AccountCreated implements Event, Serializable {

	private UUID accountId;
	private String firstName;
	private String lastName;

	/**
	 * Default Constructor for serialization.
	 */
	public AccountCreated() {
	}

	/**
	 * Constructor
	 * 
	 * @param accountId
	 */
	public AccountCreated(UUID accountId, String firstName, String lastName) {
		this.accountId = accountId;
		this.firstName = firstName;
		this.lastName = lastName;
	}

	/**
	 * Get the Account Id.
	 * 
	 * @return
	 */
	public UUID getAccountId() {
		return this.accountId;
	}

	/**
	 * Get the first name.
	 * 
	 * @return
	 */
	public String getFirstName() {
		return firstName;
	}

	/**
	 * Get the last name.
	 * 
	 * @return
	 */
	public String getLastName() {
		return lastName;
	}
}
