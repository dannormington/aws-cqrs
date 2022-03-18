package com.aws.cqrs.application;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.math.BigDecimal;

/**
 * Simple event that is stored
 */
@DynamoDbBean
public class AccountModel {
    private String accountId;
    private String firstName;
    private String lastName;
    private BigDecimal balance;

    public AccountModel() {
    }

    @DynamoDbPartitionKey
    @DynamoDbAttribute("AccountId")
    public String getAccountId() {
        return this.accountId;
    }

    public void setAccountId(String accountId) { this.accountId = accountId; }

    @DynamoDbAttribute("FirstName")
    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @DynamoDbAttribute("LastName")
    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @DynamoDbAttribute("Balance")
    public BigDecimal getBalance() { return this.balance; }

    public void setBalance(BigDecimal balance) { this.balance = balance; }
}