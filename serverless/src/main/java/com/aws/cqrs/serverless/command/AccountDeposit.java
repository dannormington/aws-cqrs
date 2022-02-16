package com.aws.cqrs.serverless.command;

import java.math.*;
import java.util.UUID;

import com.aws.cqrs.core.messaging.Command;

public class AccountDeposit implements Command {
	private BigDecimal amount;
	private UUID accountId;

	public AccountDeposit() {
	}

	public AccountDeposit(UUID accountId, BigDecimal amount) {
		this.accountId = accountId;
		this.amount = amount;
	}

	public UUID getAccountId() {
		return accountId;
	}

	public void setAccountId(UUID accountId) {
		this.accountId = accountId;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
}