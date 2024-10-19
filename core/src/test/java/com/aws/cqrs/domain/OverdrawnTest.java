package com.aws.cqrs.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OverdrawnTest {
    @Test
    void when_create_with_default_constructor_expect_success() {
        UUID accountId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        Overdrawn event = new Overdrawn(accountId, transactionId, new BigDecimal(100), new BigDecimal(-150));

        assertEquals(accountId, event.getAccountId());
        assertEquals(new BigDecimal(100), event.getServiceCharge());
        assertEquals(new BigDecimal(-150), event.getNewBalance());
        assertNotNull(event.getTransactionId());
    }

    @Test
    void when_create_with_empty_constructor_expect_null_values() {
        Overdrawn event = new Overdrawn();

        assertNull(event.getAccountId());
        assertNull(event.getServiceCharge());
        assertNull(event.getNewBalance());
        assertNull(event.getTransactionId());
    }
}