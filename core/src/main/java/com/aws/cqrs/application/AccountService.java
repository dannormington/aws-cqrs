package com.aws.cqrs.application;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.aws.cqrs.infrastructure.exceptions.AggregateNotFoundException;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.exceptions.TransactionFailedException;
import com.aws.cqrs.infrastructure.persistence.EventRepository;
import com.aws.cqrs.infrastructure.persistence.Repository;
import com.aws.cqrs.domain.Account;

/**
 * The purpose of this service is to handle account based transactions.
 */
public class AccountService {

    private static final String EVENT_STORE_TABLE = "AccountEventStore";

    private final Repository<Account> repository = new EventRepository<>(Account.class, EVENT_STORE_TABLE);

    /**
     * Create a new account.
     *
     * @param firstName The first name.
     * @param lastName  The last name.
     * @throws TransactionFailedException
     * @throws HydrationException
     */
    public CompletableFuture<Void> create(UUID accountId, String firstName, String lastName) throws TransactionFailedException, HydrationException {
        return repository.save(Account.create(accountId, firstName, lastName));
    }

    /**
     * Make a deposit.
     *
     * @param accountId The account id.
     * @param amount    The amount to deposit.
     * @throws HydrationException
     * @throws AggregateNotFoundException
     * @throws TransactionFailedException
     */
    public CompletableFuture<Void> deposit(UUID accountId, BigDecimal amount)
            throws HydrationException, AggregateNotFoundException, TransactionFailedException {
        return repository.getById(accountId).thenCompose(account -> {
            if (account != null) {
                account.deposit(amount);
                return repository.save(account);
            }
            return CompletableFuture.completedFuture(null);
        });
    }

    /**
     * Make a withdrawal.
     *
     * @param accountId The account id.
     * @param amount    The amount to withdraw.
     * @throws HydrationException
     * @throws AggregateNotFoundException
     * @throws TransactionFailedException
     */
    public CompletableFuture<Void> withdraw(UUID accountId, BigDecimal amount)
            throws HydrationException, AggregateNotFoundException, TransactionFailedException {
        return repository.getById(accountId).thenCompose(account -> {
            account.withdraw(amount);
            return repository.save(account);
        });
    }
}
