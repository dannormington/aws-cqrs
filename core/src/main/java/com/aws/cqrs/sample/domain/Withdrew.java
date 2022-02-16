package com.aws.cqrs.sample.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event that is published when an account has been withdrawn from.
 */

public class Withdrew extends Transaction {

	private static final long serialVersionUID = 1L;

	/**
	 * Default Constructor for serialization.
	 */
	public Withdrew() {
		super();
	}

	/**
	 * Constructor
	 * 
	 * @param accountId
	 * @param amount
	 */
	public Withdrew(UUID accountId, BigDecimal amount) {
		super(accountId, amount);
	}
}
