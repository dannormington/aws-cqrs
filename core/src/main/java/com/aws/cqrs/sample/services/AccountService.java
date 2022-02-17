package com.aws.cqrs.sample.services;

import java.math.BigDecimal;
import java.util.UUID;

import com.aws.cqrs.core.exceptions.AggregateNotFoundException;
import com.aws.cqrs.core.exceptions.EventCollisionException;
import com.aws.cqrs.core.exceptions.HydrationException;
import com.aws.cqrs.core.persistence.EventRepository;
import com.aws.cqrs.core.persistence.Repository;
import com.aws.cqrs.sample.domain.Account;

/**
 * 
 * The purpose of this service is to handle account based transactions.
 *
 */
public class AccountService {

	private static final String EVENT_STORE_TABLE = "AccountTransactions";
	private static final String SQS_QUEUE = "AccountQueue.fifo";

	/**
	 * Create a new account.
	 *
	 * @param firstName
	 * @param lastName
	 * 
	 * @return The newly created Account Id.
	 * @throws EventCollisionException
	 * @throws HydrationException
	 */
	public UUID create(String firstName, String lastName) throws EventCollisionException, HydrationException {
		UUID accountId = UUID.randomUUID();
		Repository<Account> repository = new EventRepository<Account>(Account.class, EVENT_STORE_TABLE, SQS_QUEUE);
		repository.save(Account.create(accountId, firstName, lastName));
		return accountId;
	}

	/**
	 * Make a deposit.
	 * 
	 * @param accountId
	 * @param amount
	 * @throws HydrationException
	 * @throws AggregateNotFoundException
	 * @throws EventCollisionException
	 */
	public void deposit(UUID accountId, BigDecimal amount)
			throws HydrationException, AggregateNotFoundException, EventCollisionException {
		Repository<Account> repository = new EventRepository<Account>(Account.class, EVENT_STORE_TABLE, SQS_QUEUE);
		Account account = repository.getById(accountId);
		account.deposit(amount);
		repository.save(account);
	}

	/**
	 * Make a withdrawal.
	 * 
	 * @param accountId
	 * @param amount
	 * @throws HydrationException
	 * @throws AggregateNotFoundException
	 * @throws EventCollisionException
	 */
	public void withdrawal(UUID accountId, BigDecimal amount)
			throws HydrationException, AggregateNotFoundException, EventCollisionException {
		Repository<Account> repository = new EventRepository<Account>(Account.class, EVENT_STORE_TABLE, SQS_QUEUE);
		Account account = repository.getById(accountId);
		account.withdrawal(amount);
		repository.save(account);
	}
}
