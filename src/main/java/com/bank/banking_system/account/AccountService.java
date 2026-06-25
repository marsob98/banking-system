package com.bank.banking_system.account;

import com.bank.banking_system.account.dto.AccountResponse;
import com.bank.banking_system.account.dto.BankStatsResponse;
import com.bank.banking_system.account.dto.TransactionResponse;
import com.bank.banking_system.account.dto.TransferResponse;
import com.bank.banking_system.customer.Customer;
import com.bank.banking_system.customer.CustomerRepository;
import com.bank.banking_system.exception.*;
import com.bank.banking_system.transaction.Transaction;
import com.bank.banking_system.transaction.TransactionRepository;
import com.bank.banking_system.transaction.TransactionType;
import com.bank.banking_system.user.User;
import com.bank.banking_system.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;


    public AccountService(AccountRepository accountRepository,
                          CustomerRepository customerRepository,
                          TransactionRepository transactionRepository,
                          UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
    }


    @Transactional
    public List<AccountResponse> getAllAccounts() {
        return accountRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public List<TransactionResponse> getAllTransactionsForAcc(Long id, String username) {
        Account account = findAccountById(id);
        checkOwnership(account, username);
        return transactionRepository.findBySourceAccountIdOrTargetAccountId(id, id).stream()
                .map(this::toTransactionResponse)
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
    public Account withdraw(Long accountId, BigDecimal amount, String username) {
        Account account = findAccountById(accountId);
        checkOwnership(account, username);
        validatePositiveAmount(amount);
        checkAccountIsBlocked(account);
        checkInsufficientFunds(account, amount);

        account.setBalance(account.getBalance().subtract(amount));
        Transaction transaction = new Transaction(TransactionType.WITHDRAW, amount, account, null);
        transactionRepository.save(transaction);

        return accountRepository.save(account);
    }

    @Transactional
    public TransferResponse transfer(Long fromAccId, Long toAccId, BigDecimal amount, String username) {
        if (fromAccId.equals(toAccId)) {
            throw new SameAccountTransferException("Cannot transfer to the same account");
        }

        Account source = findAccountById(fromAccId);
        Account target = findAccountById(toAccId);
        checkOwnership(source, username);
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
    public List<AccountResponse> getAccountsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Customer customer = user.getCustomer();

        if (customer == null) {
            throw new ResourceNotFoundException("No customer linked to this user");
        }

        return accountRepository.findByCustomerId(customer.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    private void checkOwnership(Account account, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getCustomer().getId().equals(account.getCustomer().getId())) {
            throw new SomeoneElseAccountException("Not your account");
        }
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
            throw new NegativeBalanceException("More than 0!");
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
    @Transactional
    public BigDecimal sumAllBalances() {
       return accountRepository.sumAllBalances();
    }


}
