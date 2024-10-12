package com.aws.cqrs.domain;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Event that is published when an account has been withdrawn from.
 */

public class Withdrew extends Transaction {

    /**
     * Default Constructor for serialization.
     */
    public Withdrew() {
        super();
    }

    /**
     * Constructor
     *
     * @param accountId The account id.
     * @param amount    The amount withdrawn.
     * @param balance   The account balance.
     */
    public Withdrew(UUID accountId, BigDecimal amount, BigDecimal balance) {
        super(accountId, amount, balance);
    }
}
