package com.main.mini_bank.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.main.mini_bank.model.entity.Account;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    boolean existsByNumber(String number);

    boolean existsByName(String name);

    Optional<Account> findByNumber(String number);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.number = :number")
    Optional<Account> findByNumberForUpdate(@Param("number") String number);

    Optional<Account> findByIdAndUserId(UUID id, UUID userId);

    List<Account> findByUserId(UUID userId);
}
