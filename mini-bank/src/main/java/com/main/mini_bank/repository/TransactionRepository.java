package com.main.mini_bank.repository;

import java.util.List;
import java.util.UUID;

import com.main.mini_bank.model.entity.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query("""
        select t from Transaction t
        join fetch t.fromAccount fa
        join fetch t.toAccount ta
        where fa.id = :accountId
           or ta.id = :accountId
        order by t.transactionDate desc
        """)
    List<Transaction> findHistoryWithAccounts(@Param("accountId") UUID accountId);
}
