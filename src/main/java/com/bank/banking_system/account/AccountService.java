package com.bank.banking_system.account;

import com.bank.banking_system.account.dto.AccountResponse;
import com.bank.banking_system.account.dto.BankStatsResponse;
import com.bank.banking_system.account.dto.TransactionResponse;
import com.bank.banking_system.account.dto.TransferResponse;
import com.bank.banking_system.customer.Customer;
import com.bank.banking_system.customer.CustomerRepository;
import com.bank.banking_system.exception.AccountBlockedException;
import com.bank.banking_system.exception.InsufficientFundsException;
import com.bank.banking_system.exception.ResourceNotFoundException;
import com.bank.banking_system.transaction.Transaction;
import com.bank.banking_system.transaction.TransactionRepository;
import com.bank.banking_system.transaction.TransactionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;


    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }


    @Transactional
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public Account createAccount(AccountType accountType, Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        String accountNumber = "PL" + System. currentTimeMillis();

        Account account = new Account(accountNumber, accountType, customer);
        return accountRepository.save(account);
    }

    @Transactional
    public List<TransactionResponse> getAllTransactionsForAcc(Long id) {
        return transactionRepository.findBySourceAccountIdOrTargetAccountId(id, id).stream()
                .map(this::toTransactionResponse)
                .toList();
    }

    @Transactional
    public Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

    }


    @Transactional
    public Account deposit(Long accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);
        validatePositiveAmount(amount);
        checkAccountIsBlocked(account);

        account.setBalance(account.getBalance().add(amount));
        Transaction transaction = new Transaction(TransactionType.DEPOSIT, amount, null, account);

        transactionRepository.save(transaction);
        return accountRepository.save(account);
    }

    @Transactional
    public Account withdraw(Long accountId, BigDecimal amount) {
        Account account = findAccountById(accountId);

        validatePositiveAmount(amount);
        checkInsufficientFunds(account, amount);
        checkAccountIsBlocked(account);

        account.setBalance(account.getBalance().subtract(amount));
        Transaction transaction = new Transaction(TransactionType.WITHDRAW, amount, account, null);
        transactionRepository.save(transaction);

        return accountRepository.save(account);
    }

    @Transactional
    public TransferResponse transfer(Long fromAccId, Long toAccId, BigDecimal amount) {
        Account source = findAccountById(fromAccId);
        Account target = findAccountById(toAccId);

        validatePositiveAmount(amount);
        checkInsufficientFunds(source, amount);
        checkAccountIsBlocked(source);
        checkAccountIsBlocked(target);

        source.setBalance(source.getBalance().subtract(amount));
        target.setBalance(target.getBalance().add(amount));
        Transaction transaction = new Transaction(TransactionType.TRANSFER, amount, source, target);

        transactionRepository.save(transaction);
        accountRepository.save(source);
        accountRepository.save(target);

        return new TransferResponse(
                "Transfer successful",
                amount,
                source.getAccountNumber(),
                target.getAccountNumber()
        );
    }

    @Transactional
    public Account blockAccount(Long accountId) {
        Account account = findAccountById(accountId);
        account.setIsBlocked(true);

        return accountRepository.save(account);
    }

    @Transactional
    public Account unBlockAccount(Long accountId) {
        Account account = findAccountById(accountId);
        account.setIsBlocked(false);

        return accountRepository.save(account);
    }

    public AccountResponse toResponse(Account account) {
        String ownerName = account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName();
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getIsBlocked(),
                ownerName);
    }

    public TransactionResponse toTransactionResponse(Transaction transaction) {
        String source = transaction.getSourceAccount() != null ?
                transaction.getSourceAccount().getAccountNumber() : null;
        String target = transaction.getTargetAccount() != null ?
                transaction.getTargetAccount().getAccountNumber() : null;

        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                source,
                target,
                transaction.getTimestamp());
    }

    private void checkAccountIsBlocked(Account account) {
        if (account.getIsBlocked()) {
            throw new AccountBlockedException("Account " + account.getAccountNumber() + " is blocked");
        }
    }

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("More than 0");
        }
    }

    private void checkInsufficientFunds(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
    }

    private Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    public BankStatsResponse bankStatsResponse() {
        BigDecimal totalBalance = accountRepository.findAll().stream()
                .map(Account::getBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new BankStatsResponse(
                customerRepository.count(),
                accountRepository.count(),
                transactionRepository.count(),
                totalBalance);
    }

    @Transactional
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        List<Transaction> transactions = transactionRepository.findBySourceAccountIdOrTargetAccountId(id, id);

        transactionRepository.deleteAll(transactions);
        accountRepository.delete(account);

    }
}
