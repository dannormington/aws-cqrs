package com.aws.cqrs.api.command;

import java.io.Serializable;
import java.util.UUID;

public class CreateAccountCommand implements Serializable {
    private final UUID accountId;
    private final String firstName;
    private final String lastName;

    public CreateAccountCommand(UUID accountId, String firstName, String lastName) {
        this.accountId = accountId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
