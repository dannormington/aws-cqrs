package com.aws.cqrs.domain;

import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.messaging.Event;
import com.google.gson.Gson;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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
            Account.class.getConstructor().newInstance();
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

    @ParameterizedTest
    @MethodSource("invalidAccountParameters")
    void when_invalidAccountCreated_expect_IllegalArgumentException(UUID accountId, String firstName, String lastName) {
        assertThrows(IllegalArgumentException.class, () -> {
            Account.create(accountId, firstName, lastName);
        });
    }

    private static Stream<Arguments> invalidAccountParameters() {
        return Stream.of(
                Arguments.of(null, "John", "Smith"),
                Arguments.of(UUID.randomUUID(), null, "Smith"),
                Arguments.of(UUID.randomUUID(), "John", null)
        );
    }

    @Test
    void when_deposit_invalid_amount_expect_IllegalArgumentException() {
        Account account = new Account();

        assertThrows(IllegalArgumentException.class, () -> {
            account.deposit(BigDecimal.ZERO);
        });
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

        Assertions.assertEquals(1, numberOfChanges);
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
    void when_withdraw_invalid_amount_expect_IllegalArgumentException() {
        Account account = new Account();

        assertThrows(IllegalArgumentException.class, () -> {
            account.withdraw(BigDecimal.ZERO);
        });
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

    @Test
    void when_getHashcode_expect_match() {
        UUID accountId = UUID.randomUUID();
        int expectedHash = Objects.hash(accountId, 0, 0);
        Account account = Account.create(accountId, "John", "Smith");
        assertEquals(expectedHash, account.hashCode());
    }

    @ParameterizedTest
    @MethodSource("equalsParameters")
    void when_equals_expect_success(Account account1, Account account2) {
        assertTrue(account1.equals(account2));
    }

    @ParameterizedTest
    @MethodSource("notEqualsParameters")
    void when_equals_expect_not_equal(Account account1, Account account2) {
        assertFalse(account1.equals(account2));
    }

    @Test
    void when_equals_compare_null_expect_not_equal() {
        Account account = Account.create(accountId, "John", "Smith");
        assertFalse(account.equals(null));
    }

    @Test
    void when_equals_compare_diff_class_expect_not_equal() {
        Account account = Account.create(accountId, "John", "Smith");
        AccountCreated accountCreated = new AccountCreated(accountId, "John", "Smith");
        assertFalse(account.equals(accountCreated));
    }

    private static Stream<Arguments> equalsParameters() {
        UUID accountId = UUID.randomUUID();
        Account account1 = Account.create(accountId, "John", "Smith");
        Account account2 = Account.create(accountId, "Amy", "Smith");

        return Stream.of(
                Arguments.of(account1, account2),
                Arguments.of(account1, account1)
        );
    }

    private static Stream<Arguments> notEqualsParameters() {
        UUID account1Id = UUID.randomUUID();
        UUID account2Id = UUID.randomUUID();

        Account account1 = Account.create(account1Id, "John", "Smith");
        Account account2 = Account.create(account2Id, "Amy", "Smith");

        // Load from history to ensure the expected version is different
        AccountCreated accountCreated = new AccountCreated(account1Id, "John", "Smith");
        Account account3 = new Account();
        account3.loadFromHistory(List.of(accountCreated));

        // Make another variation of the first account, but make a deposit to change the balance
        Account account4 = Account.create(account1Id, "John", "Smith");
        account4.deposit(new BigDecimal(50));

        return Stream.of(
                Arguments.of(account1, account2),
                Arguments.of(account1, account3),
                Arguments.of(account1, account4)
        );
    }
}