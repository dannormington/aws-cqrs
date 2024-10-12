package com.aws.cqrs.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.aws.cqrs.infrastructure.messaging.Event;

/**
 * Base class for transactions.
 */
public abstract class Transaction implements Event, Serializable {

    private UUID accountId;
    private UUID transactionId;
    private BigDecimal amount;
    private OffsetDateTime date;
    private BigDecimal newBalance;

    /**
     * Default Constructor for serialization.
     */
    protected Transaction() {
    }

    /**
     * Constructor
     *
     * @param accountId The account id.
     * @param amount    The amount of the transaction.
     * @param newBalance   The account new balance.
     */
    protected Transaction(UUID accountId, BigDecimal amount, BigDecimal newBalance) {
        this.accountId = accountId;
        this.transactionId = UUID.randomUUID();
        this.date = OffsetDateTime.now();
        this.amount = amount;
        this.newBalance = newBalance;
    }

    /**
     * @return The account id.
     */
    public UUID getAccountId() {
        return this.accountId;
    }

    /**
     * @return The transaction id.
     */
    public UUID getTransactionId() {
        return this.transactionId;
    }

    /**
     * @return The amount deposited.
     */
    public BigDecimal getAmount() {
        return this.amount;
    }

    /**
     * @return The account balance.
     */
    public BigDecimal getNewBalance() { return this.newBalance; }

    /**
     * @return The transaction date.
     */
    public OffsetDateTime getDate() {
        return this.date;
    }
}
