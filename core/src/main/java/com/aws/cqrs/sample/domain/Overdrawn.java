package com.aws.cqrs.sample.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

import com.aws.cqrs.core.messaging.Event;

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
	 * @param accountId
	 * @param transactionId,
	 * @param serviceCharge,
	 */
	public Overdrawn(UUID accountId, UUID transactionId, BigDecimal serviceCharge) {
		this.accountId = accountId;
		this.transactionId = transactionId;
		this.serviceCharge = serviceCharge;
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
	 * Get the service charge.
	 * 
	 * @return
	 */
	public BigDecimal getServiceCharge() {
		return this.serviceCharge;
	}
}