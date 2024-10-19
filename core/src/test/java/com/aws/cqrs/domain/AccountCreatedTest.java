package com.aws.cqrs.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AccountCreatedTest {

  @Test
  void when_create_with_default_constructor_expect_success() {
    UUID accountId = UUID.randomUUID();
    AccountCreated event = new AccountCreated(accountId, "John", "Smith");

    assertEquals(accountId, event.getAccountId());
    assertEquals("John", event.getFirstName());
    assertEquals("Smith", event.getLastName());
  }

  @Test
  void when_create_with_empty_constructor_expect_null_values() {
    AccountCreated accountCreated = new AccountCreated();

    assertNull(accountCreated.getAccountId());
    assertNull(accountCreated.getFirstName());
    assertNull(accountCreated.getLastName());
  }
}
