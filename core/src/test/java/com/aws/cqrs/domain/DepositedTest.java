package com.aws.cqrs.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DepositedTest {
    @Test
    void when_create_with_default_constructor_expect_success() {
        UUID accountId = UUID.randomUUID();
        Deposited event = new Deposited(accountId, new BigDecimal(100), new BigDecimal(200));

        assertEquals(accountId, event.getAccountId());
        assertEquals(new BigDecimal(100), event.getAmount());
        assertEquals(new BigDecimal(200), event.getNewBalance());
        assertNotNull(event.getTransactionId());
        assertNotNull(event.getDate());
    }

    @Test
    void when_create_with_empty_constructor_expect_null_values() {
        Deposited event = new Deposited();

        assertNull(event.getAccountId());
        assertNull(event.getAmount());
        assertNull(event.getNewBalance());
        assertNull(event.getTransactionId());
        assertNull(event.getDate());
    }
}