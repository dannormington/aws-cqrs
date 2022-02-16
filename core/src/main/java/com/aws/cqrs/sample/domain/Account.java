package com.aws.cqrs.sample.domain;

import java.math.BigDecimal;
import java.util.UUID;

import com.aws.cqrs.core.domain.AggregateRootBase;
import com.aws.cqrs.core.exceptions.HydrationException;

/**
 * Class that represents an account.
 */
public class Account extends AggregateRootBase {

	/**
	 * The amount to charge the customer for an overdraft.
	 */
	private static final BigDecimal OVERDRAFT_CHARGE = new BigDecimal(10);

	/**
	 * The account balance
	 */
	private BigDecimal balance = BigDecimal.ZERO;

	public Account() {
	}

	/**
	 * Constructor used when creating a new account
	 * 
	 * @param accountId
	 * @throws HydrationException
	 */
	private Account(UUID accountId, String firstName, String lastName) throws HydrationException {
		applyChange(new AccountCreated(accountId, firstName, lastName));
	}

	/**
	 * Make a deposit into the account.
	 * 
	 * @param amount
	 * @throws IllegalArgumentException
	 * @throws HydrationException
	 */
	public void deposit(BigDecimal amount) throws HydrationException {

		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Amount must be greater than zero.");
		}

		applyChange(new Deposited(this.getId(), amount));
	}

	/**
	 * Make a withdrawal from the account.
	 * 
	 * @param amount
	 * @throws IllegalArgumentException
	 * @throws HydrationException
	 */
	public void withdrawal(BigDecimal amount) throws HydrationException {
		if (amount.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Amount must be greater than zero.");
		}

		Withdrew withdrew = new Withdrew(this.getId(), amount);
		applyChange(withdrew);

		if (balance.compareTo(BigDecimal.ZERO) < 0) {
			applyChange(new Overdrawn(this.getId(), withdrew.getTransactionId(), OVERDRAFT_CHARGE));
		}
	}

	/**
	 * Create a new account.
	 * 
	 * @param accountId
	 * @param firstName
	 * @param lastName
	 * 
	 * @return
	 * @throws HydrationException
	 * @throws IllegalArgumentException
	 */
	public static Account create(UUID accountId, String firstName, String lastName) throws HydrationException {
		if (accountId == null || firstName == null || lastName == null) {
			throw new IllegalArgumentException("Account Id, first name, and last name are required.");
		}

		return new Account(accountId, firstName, lastName);
	}

	/**
	 * Apply the state change for the {@link AccountCreated} event.
	 * 
	 * @param event
	 */
	@SuppressWarnings("unused")
	private void apply(AccountCreated event) {
		id = event.getAccountId();
	}

	/**
	 * Apply the state change for the {@link Deposited} event.
	 * 
	 * @param event
	 */
	@SuppressWarnings("unused")
	private void apply(Deposited event) {
		balance = balance.add(event.getAmount());
	}

	/**
	 * Apply the state change for the {@link Withdrew} event.
	 * 
	 * @param event
	 */
	@SuppressWarnings("unused")
	private void apply(Withdrew event) {
		balance = balance.subtract(event.getAmount());
	}

	/**
	 * Apply the state change for the {@link AccountCreated} event.
	 * 
	 * @param event
	 */
	@SuppressWarnings("unused")
	private void apply(Overdrawn event) {
		balance = balance.subtract(event.getServiceCharge());
	}
}
