package com.bank.banking_system.Account;

import com.bank.banking_system.Account.dto.AccountResponse;
import com.bank.banking_system.Account.dto.BankStatsResponse;
import com.bank.banking_system.Account.dto.TransactionResponse;
import com.bank.banking_system.Account.dto.TransferResponse;
import com.bank.banking_system.Customer.Customer;
import com.bank.banking_system.Customer.CustomerRepository;
import com.bank.banking_system.Exception.AccountBlockedException;
import com.bank.banking_system.Exception.InsufficientFundsException;
import com.bank.banking_system.Exception.ResourceNotFoundException;
import com.bank.banking_system.Transaction.Transaction;
import com.bank.banking_system.Transaction.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Account createAccount(String accountType, Long customerId) {
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
    public Account deposit(Long accountId, Double amount) {
        Account account = findAccountById(accountId);
        if (amount <= 0) {
            throw new IllegalArgumentException("More than 0");
        }
        checkAccountIsBlocked(account);

        account.setBalance(account.getBalance() + amount);
        Transaction transaction = new Transaction("DEPOSIT", amount, null, account);

        transactionRepository.save(transaction);
        return accountRepository.save(account);
    }

    @Transactional
    public Account withdraw(Long accountId, Double amount) {
        Account account = findAccountById(accountId);

        checkInsufficientFunds(account, amount);
        checkAccountIsBlocked(account);

        account.setBalance(account.getBalance() - amount);
        Transaction transaction = new Transaction("WITHDRAW", amount, account, null);

        transactionRepository.save(transaction);
        return accountRepository.save(account);
    }

    @Transactional
    public TransferResponse transfer(Long fromAccId, Long toAccId, Double amount) {
        Account source = findAccountById(fromAccId);
        Account target = findAccountById(toAccId);

        checkInsufficientFunds(source, amount);
        checkAccountIsBlocked(source);
        checkAccountIsBlocked(target);

        source.setBalance(source.getBalance() - amount);
        target.setBalance(target.getBalance() + amount);
        Transaction transaction = new Transaction("TRANSFER", amount, source, target);

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
        account.setBlocked(true);
        return accountRepository.save(account);
    }

    @Transactional
    public Account unBlockAccount(Long accountId) {
        Account account = findAccountById(accountId);
        account.setBlocked(false);
        return accountRepository.save(account);
    }

    public AccountResponse toResponse(Account account) {
        String ownerName = account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName();
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getBlocked(),
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
                transaction.getTimestamp().toString());
    }

    private void checkAccountIsBlocked(Account account) {
        if (account.getBlocked()) {
            throw new AccountBlockedException("Account " + account.getAccountNumber() + " is blocked");
        }
    }

    private void checkInsufficientFunds(Account account, Double amount) {
        if (account.getBalance() - amount < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }
    }

    private Account findAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
    }

    public BankStatsResponse bankStatsResponse() {
        return new BankStatsResponse(
                customerRepository.count(),
                accountRepository.count(),
                transactionRepository.count(),
                accountRepository.findAll()
                        .stream().mapToDouble(Account::getBalance).sum());
    }

    @Transactional
    public void deleteAccount(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));
        accountRepository.delete(account);
    }
}
