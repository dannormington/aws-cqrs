package com.aws.cqrs.sample.domain;

import com.aws.cqrs.domain.*;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.messaging.Event;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class AccountTest {

    private Gson gson;
    private static final String accountCreatedJson = "{\"accountId\":\"c96b90e6-c3d8-4391-bc82-2bf02882eeba\",\"firstName\":\"John\",\"lastName\":\"Smith\"}";
    private static final String accountCreatedClassName = AccountCreated.class.getName();
    private static final UUID accountId = UUID.fromString("c96b90e6-c3d8-4391-bc82-2bf02882eeba");

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        gson = new Gson();
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        gson = null;
    }

    @Test
    void when_hasDefaultConstructor_expect_success() {
        Assertions.assertDoesNotThrow(() -> {
            Account.class.newInstance();
        });
    }

    @Test
    void when_validAccountCreated_expect_loadHistory() throws ClassNotFoundException, HydrationException {
        Event event = (Event) gson.fromJson(accountCreatedJson, Class.forName(accountCreatedClassName));
        List<Event> events = new ArrayList<>();
        events.add(event);

        Account account = new Account();
        account.loadFromHistory(events);

        Assertions.assertEquals(accountId, account.getId());
    }

    @Test
    void when_deposited_expect_oneEvent() throws HydrationException {
        Account account = new Account();
        account.deposit(new BigDecimal("12.55"));
        Iterable<Event> changes = account.getUncommittedChanges();

        int numberOfChanges = 0;
        for (Event ignored : changes) {
            numberOfChanges++;
        }

        Assertions.assertEquals(numberOfChanges, 1);
    }

    @Test
    void when_deposited_expect_correctAmount() throws HydrationException {
        BigDecimal amount = new BigDecimal("12.55");
        Account account = new Account();

        account.deposit(amount);

        Iterable<Event> changes = account.getUncommittedChanges();

        BigDecimal depositedAmount = new BigDecimal(0);
        for (Event change : changes) {
            Deposited deposited = (Deposited) change;
            depositedAmount = depositedAmount.add(deposited.getAmount());
        }

        Assertions.assertEquals(depositedAmount, amount);
    }

    @Test
    void when_withdraw_expect_correctAmount() throws HydrationException {
        BigDecimal amount = new BigDecimal("12.55");
        Account account = new Account();

        account.withdraw(amount);

        Iterable<Event> changes = account.getUncommittedChanges();

        BigDecimal withdrawalAmount = new BigDecimal(0);
        for (Event event : changes) {
            if (event instanceof Withdrew) {
                Withdrew withdrew = (Withdrew) event;
                withdrawalAmount = withdrawalAmount.add(withdrew.getAmount());
            }
        }

        Assertions.assertEquals(withdrawalAmount, amount);
    }

    @Test
    void when_withdrawFromLowFunds_expect_negativeBalance() throws HydrationException {
        BigDecimal amount = new BigDecimal("12.55");
        Account account = new Account();

        // Withdraw from an account with no balance.
        account.withdraw(amount);

        Iterable<Event> changes = account.getUncommittedChanges();

        BigDecimal balance = new BigDecimal(0);
        for (Event event : changes) {
            if (event instanceof Withdrew) {
                Withdrew withdrew = (Withdrew) event;
                balance = balance.subtract(withdrew.getAmount());
            } else if (event instanceof Overdrawn) {
                Overdrawn overdrawn = (Overdrawn) event;
                balance = balance.subtract(overdrawn.getServiceCharge());
            }
        }
        Assertions.assertTrue(balance.compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void when_withdrawFromAvailableFunds_expect_positiveBalance() throws HydrationException {
        Account account = new Account();

        // Deposit $100
        account.deposit(new BigDecimal(100));
        // Withdraw $12.55
        account.withdraw(new BigDecimal("12.55"));

        Iterable<Event> changes = account.getUncommittedChanges();

        BigDecimal balance = new BigDecimal(0);
        for (Event event : changes) {
            if (event instanceof Deposited) {
                Deposited deposited = (Deposited) event;
                balance = balance.add(deposited.getAmount());
            } else if (event instanceof Withdrew) {
                Withdrew withdrew = (Withdrew) event;
                balance = balance.subtract(withdrew.getAmount());
            } else if (event instanceof Overdrawn) {
                Overdrawn overdrawn = (Overdrawn) event;
                balance = balance.subtract(overdrawn.getServiceCharge());
            }
        }
        Assertions.assertTrue(balance.compareTo(BigDecimal.ZERO) > 0);
    }
}