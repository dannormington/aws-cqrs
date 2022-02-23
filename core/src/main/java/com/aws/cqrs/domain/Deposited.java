package com.aws.cqrs.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event that is published when an account has been deposited to.
 */
public class Deposited extends Transaction {

    /**
     * Default Constructor for serialization.
     */
    public Deposited() {
        super();
    }

    /**
     * Constructor
     *
     * @param accountId The account id.
     * @param amount    The amount deposited.
     */
    public Deposited(UUID accountId, BigDecimal amount) {
        super(accountId, amount);
    }
}