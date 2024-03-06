package com.aws.cqrs.application;

import com.aws.cqrs.domain.Account;
import com.aws.cqrs.infrastructure.persistence.Repository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Test
    void when_createAccount_expect_repository_save() {
        // Arrange
        Repository<Account> accountRepository = mock(Repository.class);
        Account account = Account.create(UUID.randomUUID(), "John", "Smith");
        AccountService accountService = new AccountService(accountRepository);
        when(accountRepository.save(account)).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        accountService.create(account.getId(), "John", "Smith").join();

        // Assert
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void when_deposit_expect_repository_get_save_and_deposit() {
        // Arrange
        Repository<Account> accountRepository = mock(Repository.class);
        Account account = mock(Account.class);
        AccountService accountService = new AccountService(accountRepository);
        when(accountRepository.getById(account.getId())).thenReturn(CompletableFuture.completedFuture(account));
        when(accountRepository.save(account)).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        accountService.deposit(account.getId(), new BigDecimal(100)).join();

        // Assert
        verify(accountRepository, times(1)).getById(account.getId());
        verify(accountRepository, times(1)).save(account);
        verify(account, times(1)).deposit(new BigDecimal(100));
    }

    @Test
    void when_withdraw_expect_repository_get_save_and_withdraw() {
        // Arrange
        Repository<Account> accountRepository = mock(Repository.class);
        Account account = mock(Account.class);
        AccountService accountService = new AccountService(accountRepository);
        when(accountRepository.getById(account.getId())).thenReturn(CompletableFuture.completedFuture(account));
        when(accountRepository.save(account)).thenReturn(CompletableFuture.completedFuture(null));

        // Act
        accountService.withdraw(account.getId(), new BigDecimal(100)).join();

        // Assert
        verify(accountRepository, times(1)).getById(account.getId());
        verify(accountRepository, times(1)).save(account);
        verify(account, times(1)).withdraw(new BigDecimal(100));
    }
}