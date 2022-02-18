package com.aws.cqrs.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import com.aws.cqrs.infrastructure.messaging.Event;

/**
 * Event that is published when an account has been overdrawn.
 */
public class Overdrawn implements Event, Serializable {

	private UUID accountId;
	private UUID transactionId;
	private BigDecimal serviceCharge;

	/**
	 * Default Constructor for serialization.
	 */
	public Overdrawn() {
	}

	/**
	 * Constructor
	 * 
	 * @param accountId The account id.
	 * @param transactionId The transaction id.
	 * @param serviceCharge The service charge.
	 */
	public Overdrawn(UUID accountId, UUID transactionId, BigDecimal serviceCharge) {
		this.accountId = accountId;
		this.transactionId = transactionId;
		this.serviceCharge = serviceCharge;
	}

	/**
	 * Get the Account id.
	 * 
	 * @return The account id.
	 */
	public UUID getAccountId() {
		return this.accountId;
	}

	/**
	 * Get the Transaction id.
	 * 
	 * @return The transaction id.
	 */
	public UUID getTransactionId() {
		return this.transactionId;
	}

	/**
	 * Get the service charge.
	 * 
	 * @return The service charge.
	 */
	public BigDecimal getServiceCharge() {
		return this.serviceCharge;
	}
}