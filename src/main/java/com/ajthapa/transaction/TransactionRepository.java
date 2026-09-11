package com.ajthapa.transaction;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountAppUserId(Long appUserId);

    List<Transaction> findByAccountAppUserIdAndTransactionDateTimeGreaterThanEqualAndTransactionDateTimeLessThan(
            Long appUserId,
            LocalDateTime start,
            LocalDateTime end
    );

    List<Transaction> findByAccountAppUserIdAndTypeAndTransactionDateTimeGreaterThanEqualAndTransactionDateTimeLessThan(
            Long appUserId,
            TransactionType type,
            LocalDateTime start,
            LocalDateTime end
    );

    Optional<Transaction> findByIdAndAccountAppUserId(
            Long transactionId,
            Long userId
    );
}
