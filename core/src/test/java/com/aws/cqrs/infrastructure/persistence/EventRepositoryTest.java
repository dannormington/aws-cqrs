package com.aws.cqrs.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.aws.cqrs.domain.Account;
import com.aws.cqrs.domain.AccountCreated;
import com.aws.cqrs.domain.AggregateRootBase;
import com.aws.cqrs.infrastructure.exceptions.HydrationException;
import com.aws.cqrs.infrastructure.messaging.Event;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.Test;

class EventRepositoryTest {

  @Test
  void when_save_expect_success() {
    // Arrange
    UUID accountId = UUID.randomUUID();
    Account account = mock(Account.class);
    when(account.getId()).thenReturn(accountId);
    when(account.getExpectedVersion()).thenReturn(1);
    List<Event> events = List.of(new AccountCreated(accountId, "John", "Smith"));
    when(account.getUncommittedChanges()).thenReturn(events);
    EventStore eventStore = mock(EventStore.class);
    EventRepository<Account> eventRepository = new EventRepository<>(Account.class, eventStore);
    when(eventStore.saveEvents(account.getId(), 1, events))
        .thenReturn(CompletableFuture.completedFuture(null));

    // Act
    eventRepository.save(account).join();

    // Assert
    verify(account, times(1)).getUncommittedChanges();
    verify(account, times(1)).markChangesAsCommitted();
    verify(eventStore, times(1)).saveEvents(accountId, 1, events);
  }

  @Test
  void when_getById_expect_success() {
    // Arrange
    UUID accountId = UUID.randomUUID();
    List<Event> events = List.of(new AccountCreated(accountId, "John", "Smith"));
    EventStore eventStore = mock(EventStore.class);
    EventRepository<Account> eventRepository = new EventRepository<>(Account.class, eventStore);
    when(eventStore.getEvents(accountId)).thenReturn(CompletableFuture.completedFuture(events));

    // Act
    Account result = eventRepository.getById(accountId).join();

    // Assert
    assertNotNull(result);
    verify(eventStore, times(1)).getEvents(accountId);
  }

  @Test
  void when_getById_missing_constructor_expect_hydrationException() {
    // Arrange
    UUID accountId = UUID.randomUUID();
    List<Event> events = List.of(new AccountCreated(accountId, "John", "Smith"));
    EventStore eventStore = mock(EventStore.class);
    EventRepository<InvalidConstructor> eventRepository =
        new EventRepository<>(InvalidConstructor.class, eventStore);
    when(eventStore.getEvents(accountId)).thenReturn(CompletableFuture.completedFuture(events));

    // Act
    assertThrows(
        HydrationException.class,
        () -> {
          try {
            eventRepository.getById(accountId).join();
          } catch (CompletionException x) {
            throw x.getCause();
          }
        });
  }

  private static class InvalidConstructor extends AggregateRootBase {}
}
