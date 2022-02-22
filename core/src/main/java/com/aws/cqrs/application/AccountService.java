package com.aws.cqrs.application;

import java.math.BigDecimal;
import java.util.UUID;

import com.aws.cqrs.infrastructure.exceptions.AggregateNotFoundException;
import com.aws.cqrs.infrastructure.exceptions.EventCollisionException;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.persistence.EventRepository;
import com.aws.cqrs.infrastructure.persistence.Repository;
import com.aws.cqrs.domain.Account;

/**
 * 
 * The purpose of this service is to handle account based transactions.
 *
 */
public class AccountService {

	private static final String EVENT_STORE_TABLE = "AccountEventStore";
	private static final String SQS_QUEUE = "AccountQueue.fifo";

	/**
	 * Create a new account.
	 *
	 * @param firstName The first name.
	 * @param lastName The last name.
	 *
	 * @throws EventCollisionException
	 * @throws HydrationException
	 */
	public void create(UUID accountId, String firstName, String lastName) throws EventCollisionException, HydrationException {
		Repository<Account> repository = new EventRepository<>(Account.class, EVENT_STORE_TABLE, SQS_QUEUE);
		repository.save(Account.create(accountId, firstName, lastName));
	}

	/**
	 * Make a deposit.
	 * 
	 * @param accountId The account id.
	 * @param amount The amount to deposit.
	 * @throws HydrationException
	 * @throws AggregateNotFoundException
	 * @throws EventCollisionException
	 */
	public void deposit(UUID accountId, BigDecimal amount)
			throws HydrationException, AggregateNotFoundException, EventCollisionException {
		Repository<Account> repository = new EventRepository<>(Account.class, EVENT_STORE_TABLE, SQS_QUEUE);
		Account account = repository.getById(accountId);
		account.deposit(amount);
		repository.save(account);
	}

	/**
	 * Make a withdrawal.
	 * 
	 * @param accountId The account id.
	 * @param amount The amount to withdraw.
	 * @throws HydrationException
	 * @throws AggregateNotFoundException
	 * @throws EventCollisionException
	 */
	public void withdraw(UUID accountId, BigDecimal amount)
			throws HydrationException, AggregateNotFoundException, EventCollisionException {
		Repository<Account> repository = new EventRepository<>(Account.class, EVENT_STORE_TABLE, SQS_QUEUE);
		Account account = repository.getById(accountId);
		account.withdraw(amount);
		repository.save(account);
	}
}
