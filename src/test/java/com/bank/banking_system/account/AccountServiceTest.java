package com.bank.banking_system.account;

import com.bank.banking_system.account.dto.TransactionResponse;
import com.bank.banking_system.customer.Customer;
import com.bank.banking_system.customer.CustomerRepository;
import com.bank.banking_system.exception.*;
import com.bank.banking_system.transaction.Transaction;
import com.bank.banking_system.transaction.TransactionRepository;
import com.bank.banking_system.transaction.TransactionType;
import com.bank.banking_system.user.Role;
import com.bank.banking_system.user.User;
import com.bank.banking_system.user.UserRepository;
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

    @Mock
    private UserRepository userRepository;

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
        Customer customer = new Customer(1L, "Jan", "Kowalski", "77777777777");
        User user = new User(1L, "Kowal", "123456", Role.USER, customer);
        Account account = new Account("PL123", AccountType.CHECKING, customer);
        account.setBalance(new BigDecimal("150.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(userRepository.findByUsername("Kowal")).thenReturn(Optional.of(user));

        Account result = accountService.withdraw(1L, new BigDecimal("50.00"), user.getUsername());

        assertThat(result.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void withdraw_shouldThrow_whenAccountBlocked() {
        Customer customer = new Customer(1L, "Jan", "Kowalski", "77777777777");
        User user = new User(1L, "Kowal", "123456", Role.USER, customer);
        Account account = new Account("PL123", AccountType.CHECKING, customer);
        account.setIsBlocked(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(userRepository.findByUsername("Kowal")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> accountService.withdraw(1L, new BigDecimal("100.00"), user.getUsername()))
                .isInstanceOf(AccountBlockedException.class);

    }

    @Test
    void withdraw_shouldThrow_whenInsufficientFunds() {
        Customer customer = new Customer(1L, "Jan", "Kowalski", "77777777777");
        User user = new User(1L, "Kowal", "123456", Role.USER, customer);
        Account account = new Account("PL123", AccountType.CHECKING, customer);
        account.setBalance(new BigDecimal("100.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(userRepository.findByUsername("Kowal")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> accountService.withdraw(1L, new BigDecimal("150.00"), user.getUsername()))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void withdraw_shouldThrow_whenSomeoneElseAcc() {
        Customer customer1 = new Customer(1L, "Jan", "Kowalski", "77777777777");
        Customer customer2 = new Customer(2L, "Janusz", "Nowak", "55555555555");
//        User user1 = new User(1L, "Kowal", "123456", Role.USER, customer1);
        User user2 = new User(2L, "Nowak", "123456", Role.USER, customer2);
        Account account = new Account("PL123", AccountType.CHECKING, customer1);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(userRepository.findByUsername("Nowak")).thenReturn(Optional.of(user2));

        assertThatThrownBy(() -> accountService.withdraw(1L, new BigDecimal("50.00"), "Nowak"))
                .isInstanceOf(SomeoneElseAccountException.class);

    }

    @Test
    void transfer_shouldTransferMoney_whenValidRequest() {
        Customer customer = new Customer(1L, "Jan", "Kowalski", "77777777777");
        User user = new User(1L, "Kowal", "123456", Role.USER, customer);
        Account sourceAccount = new Account("PL123", AccountType.CHECKING, customer);
        Account targetAccount = new Account("PL124", AccountType.CHECKING, new Customer());
        sourceAccount.setBalance(new BigDecimal("200.00"));
        targetAccount.setBalance(new BigDecimal("50.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));
        when(userRepository.findByUsername("Kowal")).thenReturn(Optional.of(user));

        accountService.transfer(1L, 2L, new BigDecimal("80.00"), user.getUsername());

        assertThat(sourceAccount.getBalance()).isEqualByComparingTo("120.00");
        assertThat(targetAccount.getBalance()).isEqualByComparingTo("130.00");
    }

    @Test
    void transfer_shouldThrow_whenSameAccount() {
        assertThatThrownBy(() -> accountService.transfer(1L, 1L, new BigDecimal("40.00"), "Kowal"))
                .isInstanceOf(SameAccountTransferException.class);

    }

    @Test
    void transfer_shouldThrow_whenInsufficientFunds() {
        Customer customer = new Customer(1L, "Jan", "Kowalski", "77777777777");
        User user = new User(1L, "Kowal", "123456", Role.USER, customer);
        Account sourceAccount = new Account("PL123", AccountType.CHECKING, customer);
        Account targetAccount = new Account("PL124", AccountType.CHECKING, new Customer());
        sourceAccount.setBalance(new BigDecimal("200.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));
        when(userRepository.findByUsername("Kowal")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> accountService.transfer(1L, 2L, new BigDecimal("999.00"), user.getUsername()))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void transfer_shouldThrow_whenSourceIsBlocked() {
        Customer customer = new Customer(1L, "Jan", "Kowalski", "77777777777");
        User user = new User(1L, "Kowal", "123456", Role.USER, customer);
        Account sourceAccount = new Account("PL123", AccountType.CHECKING, customer);
        Account targetAccount = new Account("PL124", AccountType.CHECKING, new Customer());
        sourceAccount.setBalance(new BigDecimal("1.00"));
        sourceAccount.setIsBlocked(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));
        when(userRepository.findByUsername("Kowal")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> accountService.transfer(1L, 2L, new BigDecimal("1.00"), user.getUsername()))
                .isInstanceOf(AccountBlockedException.class);
    }

    @Test
    void transfer_shouldThrow_whenSourceIsNotYours() {
        Customer customer1 = new Customer(1L, "Jan", "Kowalski", "77777777777");
        Customer customer2 = new Customer(2L, "Janusz", "Nowak", "55555555555");
        User user2 = new User(2L, "Nowak", "123456", Role.USER, customer2);
        Account account = new Account("PL123", AccountType.CHECKING, customer1);
        Account targetAccount = new Account("PL124", AccountType.CHECKING, customer2);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));
        when(userRepository.findByUsername("Nowak")).thenReturn(Optional.of(user2));

        assertThatThrownBy(() -> accountService.transfer(
                1L,
                2L,
                new BigDecimal("10.0"),
                "Nowak"))
                .isInstanceOf(SomeoneElseAccountException.class);
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
        Customer customer = new Customer(1L, "Jan", "Kowalski", "77777777777");
        User user = new User(1L, "Kowal", "123456", Role.USER, customer);
        Account sourceAccount = new Account("PL123", AccountType.CHECKING, customer);
        Account targetAccount = new Account("PL124", AccountType.CHECKING, new Customer());
        Transaction transaction = new Transaction(
                TransactionType.TRANSFER, new BigDecimal("100.00"), sourceAccount, targetAccount);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(userRepository.findByUsername("Kowal")).thenReturn(Optional.of(user));
        when(transactionRepository
                .findBySourceAccountIdOrTargetAccountId(1L, 1L))
                .thenReturn(List.of(transaction));

        List<TransactionResponse> result = accountService.getAllTransactionsForAcc(1L, "Kowal");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void getAllTransactionsForAcc_shouldThrow_whenSomeoneElseAcc() {
        Customer customer = new Customer(1L, "Jan", "Kowalski", "77777777777");
        Customer customer2 = new Customer(2L, "Janusz", "Nowak", "55555555555");
        User user = new User(2L, "Nowak", "123456", Role.USER, customer2);
        Account sourceAccount = new Account("PL123", AccountType.CHECKING, customer);
        Account targetAccount = new Account("PL124", AccountType.CHECKING, customer2);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(userRepository.findByUsername("Nowak")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> accountService.getAllTransactionsForAcc(1L, "Nowak"))
                .isInstanceOf(SomeoneElseAccountException.class);
    }



}