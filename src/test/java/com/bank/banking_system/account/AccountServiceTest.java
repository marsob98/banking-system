package com.bank.banking_system.account;

import com.bank.banking_system.account.dto.TransactionResponse;
import com.bank.banking_system.customer.Customer;
import com.bank.banking_system.customer.CustomerRepository;
import com.bank.banking_system.exception.*;
import com.bank.banking_system.transaction.Transaction;
import com.bank.banking_system.transaction.TransactionRepository;
import com.bank.banking_system.transaction.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void deposit_shouldIncreaseBalance_whenAmountIsValid() {
        Account account = new Account("PL123", AccountType.CHECKING, new Customer());
        account.setBalance(new BigDecimal("100.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.deposit(1L, new BigDecimal("50.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("150.00");
    }

    @Test
    void deposit_shouldThrow_whenAccountBlocked() {
        Account account = new Account("PL123", AccountType.CHECKING, new Customer());
        account.setIsBlocked(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.deposit(1L, new BigDecimal("100.00")))
                .isInstanceOf(AccountBlockedException.class);

    }

    @Test
    void deposit_shouldThrow_whenAmountIsNegative() {
        Account account = new Account("PL123", AccountType.CHECKING, new Customer());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.deposit(1L, new BigDecimal("-1.00")))
                .isInstanceOf(NegativeBalanceException.class);
    }

    @Test
    void withdraw_shouldDecreaseBalance_whenValidAmount() {
        Account account = new Account("PL123", AccountType.CHECKING, new Customer());
        account.setBalance(new BigDecimal("150.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.withdraw(1L, new BigDecimal("50.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void withdraw_shouldThrow_whenAccountBlocked() {
        Account account = new Account("PL123", AccountType.CHECKING, new Customer());
        account.setIsBlocked(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.withdraw(1L, new BigDecimal("100.00")))
                .isInstanceOf(AccountBlockedException.class);

    }

    @Test
    void withdraw_shouldThrow_whenInsufficientFunds() {
        Account account = new Account("PL123", AccountType.CHECKING, new Customer());
        account.setBalance(new BigDecimal("100.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));


        assertThatThrownBy(() -> accountService.withdraw(1L, new BigDecimal("150.00")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void transfer_shouldTransferMoney_whenValidRequest() {
        Account sourceAccount = new Account("PL123", AccountType.CHECKING, new Customer());
        Account targetAccount = new Account("PL124", AccountType.CHECKING, new Customer());
        sourceAccount.setBalance(new BigDecimal("200.00"));
        targetAccount.setBalance(new BigDecimal("50.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));

        accountService.transfer(1L, 2L, new BigDecimal("80.00"));

        assertThat(sourceAccount.getBalance()).isEqualByComparingTo("120.00");
        assertThat(targetAccount.getBalance()).isEqualByComparingTo("130.00");
    }

    @Test
    void transfer_shouldThrow_whenSameAccount() {
        assertThatThrownBy(() -> accountService.transfer(1L, 1L, new BigDecimal("40.00")))
                .isInstanceOf(SameAccountTransferException.class);

    }

    @Test
    void transfer_shouldThrow_whenInsufficientFunds() {
        Account sourceAccount = new Account("PL123", AccountType.CHECKING, new Customer());
        Account targetAccount = new Account("PL124", AccountType.CHECKING, new Customer());
        sourceAccount.setBalance(new BigDecimal("200.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));

        assertThatThrownBy(() -> accountService.transfer(1L, 2L, new BigDecimal("999.00")))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void transfer_shouldThrow_whenSourceIsBlocked() {
        Account sourceAccount = new Account("PL123", AccountType.CHECKING, new Customer());
        Account targetAccount = new Account("PL124", AccountType.CHECKING, new Customer());
        sourceAccount.setBalance(new BigDecimal("1.00"));
        sourceAccount.setIsBlocked(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));

        assertThatThrownBy(() -> accountService.transfer(1L, 2L, new BigDecimal("1.00")))
                .isInstanceOf(AccountBlockedException.class);
    }

    @Test
    void createAccount_shouldThrow_whenCustomerNotFound() {
        assertThatThrownBy(() -> accountService.createAccount(AccountType.CHECKING, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void blockAccount_shouldSetBlockedToTrue() {
        Account account = new Account("PL123", AccountType.CHECKING, new Customer());
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.blockAccount(1L);
        assertThat(result.getIsBlocked()).isTrue();
    }

    @Test
    void unBlockAccount_shouldSetBlockedToFalse() {
        Account account = new Account("PL123", AccountType.CHECKING, new Customer());
        account.setIsBlocked(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.unBlockAccount(1L);

        assertThat(result.getIsBlocked()).isFalse();
    }

    @Test
    void deleteAccount_shouldThrow_whenAccountNotFound() {
        assertThatThrownBy(() -> accountService.deleteAccount(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAccountByNumber_shouldReturnAccount_whenExists() {
        Account account = new Account("PL123", AccountType.CHECKING, new Customer());
        when(accountRepository.findByAccountNumber("PL123")).thenReturn(Optional.of(account));

        Account result = accountService.findAccountByNumber("PL123");

        assertThat(result.getAccountNumber()).isEqualTo("PL123");
    }

    @Test
    void findAccountByNumber_ShouldThrow_whenAccountNotFound() {
        assertThatThrownBy(() -> accountService.findAccountByNumber("PL123"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAllTransactionsForAcc_shouldReturnTransactionList_whenAccountExists() {
        Account sourceAccount = new Account("PL123", AccountType.CHECKING, new Customer());
        Account targetAccount = new Account("PL124", AccountType.CHECKING, new Customer());
        Transaction transaction = new Transaction(
                TransactionType.TRANSFER, new BigDecimal("100.00"), sourceAccount, targetAccount);
        when(transactionRepository
                .findBySourceAccountIdOrTargetAccountId(1L, 1L))
                .thenReturn(List.of(transaction));

        List<TransactionResponse> result = accountService.getAllTransactionsForAcc(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).amount()).isEqualByComparingTo("100.00");
    }

}