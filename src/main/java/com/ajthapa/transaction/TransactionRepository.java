package com.ajthapa.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByTypeAndTransactionDateTimeBetween(
            TransactionType type, LocalDateTime start, LocalDateTime end);

    List<Transaction> findByAccountAppUserId(Long appUserId);
}
