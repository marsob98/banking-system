package com.bank.banking_system.account;

import com.bank.banking_system.account.dto.RecipientVerificationRequest;
import com.bank.banking_system.account.dto.TransactionResponse;
import com.bank.banking_system.account.dto.VerificationResult;
import com.bank.banking_system.customer.Customer;
import com.bank.banking_system.exception.AccountBlockedException;
import com.bank.banking_system.exception.InsufficientFundsException;
import com.bank.banking_system.exception.NegativeBalanceException;
import com.bank.banking_system.exception.ResourceNotFoundException;
import com.bank.banking_system.exception.SameAccountTransferException;
import com.bank.banking_system.exception.SomeoneElseAccountException;
import com.bank.banking_system.transaction.Transaction;
import com.bank.banking_system.transaction.TransactionRepository;
import com.bank.banking_system.transaction.TransactionType;
import com.bank.banking_system.user.Role;
import com.bank.banking_system.user.User;
import com.bank.banking_system.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;


    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private Customer customer;
    private User user;
    private Account sourceAccount;
    private Account targetAccount;

    @BeforeEach
    void setUp() {
        customer = new Customer(1L, "Jan", "Kowalski", "77777777777");
        user = new User(1L, "Kowal", "123456", Role.USER, customer);
        sourceAccount = new Account("PL123", AccountType.CHECKING, customer);
        targetAccount = new Account("PL124", AccountType.CHECKING, new Customer());
    }

    @Test
    void deposit_shouldIncreaseBalance_whenAmountIsValid() {
        sourceAccount.setBalance(new BigDecimal("100.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.save(sourceAccount)).thenReturn(sourceAccount);

        Account result = accountService.deposit(1L, new BigDecimal("50.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("150.00");
    }

    @Test
    void deposit_shouldThrow_whenAccountBlocked() {
        sourceAccount.setIsBlocked(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));

        assertThatThrownBy(() -> accountService.deposit(1L, new BigDecimal("100.00")))
                .isInstanceOf(AccountBlockedException.class);

    }

    @Test
    void deposit_shouldThrow_whenAmountIsNegative() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));

        assertThatThrownBy(() -> accountService.deposit(1L, new BigDecimal("-1.00")))
                .isInstanceOf(NegativeBalanceException.class);
    }

    @Test
    void withdraw_shouldDecreaseBalance_whenValidAmount() {
        sourceAccount.setBalance(new BigDecimal("150.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.save(sourceAccount)).thenReturn(sourceAccount);

        Account result = accountService.withdraw(1L, new BigDecimal("50.00"));

        assertThat(result.getBalance()).isEqualByComparingTo("100.00");
    }

    @Test
    void withdraw_shouldThrow_whenAccountBlocked() {
        sourceAccount.setIsBlocked(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));

        assertThatThrownBy(() -> accountService.withdraw(1L, new BigDecimal("100.00")))
                .isInstanceOf(AccountBlockedException.class);

    }

    @Test
    void withdraw_shouldThrow_whenInsufficientFunds() {
        sourceAccount.setBalance(new BigDecimal("100.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));

        assertThatThrownBy(() -> accountService.withdraw(1L, new BigDecimal("150.00")))
                .isInstanceOf(InsufficientFundsException.class);
    }




    @Test
    @DisplayName("Admin without a customer cannot transfer from a customer's account")
    void transfer_shouldThrow_whenUserHasNoCustomer() {
        User admin = new User(3L, "admin", "12345678", Role.ADMIN,null);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> accountService.transfer(1L,
                2L,
                new BigDecimal("100.00"),
                admin.getUsername())).isInstanceOf(SomeoneElseAccountException.class);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void transfer_shouldTransferMoney_whenValidRequest() {
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
        sourceAccount.setBalance(new BigDecimal("200.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(2L)).thenReturn(Optional.of(targetAccount));
        when(userRepository.findByUsername("Kowal")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> accountService.transfer(1L, 2L, new BigDecimal("999.00"), user.getUsername()))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    void transfer_shouldThrow_whenSourceIsBlocked() {
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
        Customer customer2 = new Customer(2L, "Janusz", "Nowak", "55555555555");
        User user2 = new User(2L, "Nowak", "123456", Role.USER, customer2);
        Account targetAccount = new Account("PL124", AccountType.CHECKING, customer2);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
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
    void createAccount_shouldCreateAccountForLoggedUser() {
        when(userRepository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Account result = accountService.createAccount(AccountType.SAVINGS, user.getUsername());

        assertThat(result.getCustomer()).isSameAs(customer);
    }

    @Test
    void createAccount_shouldThrow_whenCustomerNotFound() {
        assertThatThrownBy(() -> accountService.createAccount(AccountType.CHECKING, "someone"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void blockAccount_shouldSetBlockedToTrue() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.save(sourceAccount)).thenReturn(sourceAccount);

        Account result = accountService.blockAccount(1L);
        assertThat(result.getIsBlocked()).isTrue();
    }

    @Test
    void unBlockAccount_shouldSetBlockedToFalse() {
        sourceAccount.setIsBlocked(true);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.save(sourceAccount)).thenReturn(sourceAccount);

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
        when(accountRepository.findByAccountNumber("PL123")).thenReturn(Optional.of(sourceAccount));

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
        Transaction transaction = new Transaction(
                TransactionType.TRANSFER, new BigDecimal("100.00"), sourceAccount, targetAccount);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(userRepository.findByUsername("Kowal")).thenReturn(Optional.of(user));
        when(transactionRepository
                .findBySourceAccountIdOrTargetAccountId(1L, 1L))
                .thenReturn(List.of(transaction));

        List<TransactionResponse> result = accountService.getAllTransactionsForAcc(1L, "Kowal");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().amount()).isEqualByComparingTo("100.00");
    }

    @Test
    void getAllTransactionsForAcc_shouldThrow_whenSomeoneElseAcc() {
        Customer customer2 = new Customer(2L, "Janusz", "Nowak", "55555555555");
        User user = new User(2L, "Nowak", "123456", Role.USER, customer2);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(sourceAccount));
        when(userRepository.findByUsername("Nowak")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> accountService.getAllTransactionsForAcc(1L, "Nowak"))
                .isInstanceOf(SomeoneElseAccountException.class);
    }

    @Test
    void verifyRecipient_shouldReturnMatch_whenValid() {
        RecipientVerificationRequest request = new RecipientVerificationRequest(
                "PL123", "Jan", "Kowalski"
        );
        when(accountRepository.findByAccountNumber("PL123")).thenReturn(Optional.of(sourceAccount));
        assertThat(accountService.verifyRecipient(request)).isEqualTo(VerificationResult.MATCH);
    }

    @Test
    void verifyRecipient_shouldReturnNoMatch_whenAccountDoesNotExist() {
        RecipientVerificationRequest request = new RecipientVerificationRequest(
                "PL124", "Piotr", "Nowak"
        );
        when(accountRepository.findByAccountNumber("PL124")).thenReturn(Optional.empty());
        assertThat(accountService.verifyRecipient(request)).isEqualTo(VerificationResult.NO_MATCH);
    }

    @Test
    void verifyRecipient_shouldReturnNoMatch_whenFirstNameDoesNotMatch() {
        RecipientVerificationRequest request = new RecipientVerificationRequest(
                "PL123", "Piotr", "Kowalski"
        );
        when(accountRepository.findByAccountNumber("PL123")).thenReturn(Optional.of(sourceAccount));
        assertThat(accountService.verifyRecipient(request)).isEqualTo(VerificationResult.NO_MATCH);
    }

    @Test
    void verifyRecipient_shouldReturnMatch_whenNamesDifferOnlyInCaseAndWhiteSpaces() {
        RecipientVerificationRequest request = new RecipientVerificationRequest(
                "PL123", " JAN ", " KoWaLsKi   "
        );
        when(accountRepository.findByAccountNumber("PL123")).thenReturn(Optional.of(sourceAccount));
        assertThat(accountService.verifyRecipient(request)).isEqualTo(VerificationResult.MATCH);
    }



}