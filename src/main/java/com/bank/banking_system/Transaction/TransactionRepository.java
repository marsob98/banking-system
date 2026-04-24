package com.bank.banking_system.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findBySourceAccountIdOrTargetAccountId(Long sourceId, Long targetId);

}
