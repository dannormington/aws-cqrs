package com.aws.cqrs.domain;

import java.io.Serializable;
import java.util.UUID;

import com.aws.cqrs.infrastructure.messaging.Event;

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
     * @param accountId The account id.
     * @param firstName The first name.
     * @param lastName  The last name.
     */
    public AccountCreated(UUID accountId, String firstName, String lastName) {
        this.accountId = accountId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * @return The account id.
     */
    public UUID getAccountId() {
        return this.accountId;
    }

    /**
     * @return The first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * @return The last name.
     */
    public String getLastName() {
        return lastName;
    }
}
