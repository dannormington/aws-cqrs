package com.aws.cqrs.sample.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.aws.cqrs.core.messaging.Event;

/**
 * Base class for transactions.
 */

public abstract class Transaction implements Event, Serializable {

	private UUID accountId;
	private UUID transactionId;
	private BigDecimal amount;
	private OffsetDateTime date;

	/**
	 * Default Constructor for serialization.
	 */
	protected Transaction() {
	}

	/**
	 * Constructor
	 * 
	 * @param accountId
	 * @param amount
	 */
	protected Transaction(UUID accountId, BigDecimal amount) {
		this.accountId = accountId;
		this.transactionId = UUID.randomUUID();
		this.date = OffsetDateTime.now();
		this.amount = amount;
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
	 * Get the Transaction Id.
	 * 
	 * @return
	 */
	public UUID getTransactionId() {
		return this.transactionId;
	}

	/**
	 * Get the amount deposited.
	 * 
	 * @return
	 */
	public BigDecimal getAmount() {
		return this.amount;
	}

	/**
	 * Get the transaction date.
	 * 
	 * @return
	 */
	public OffsetDateTime getDate() {
		return this.date;
	}
}
