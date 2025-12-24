package com.main.mini_bank.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.main.mini_bank.enums.TransactionStatus;

public class TransactionResponse {

    private final Long id;
    private final UUID fromAccountId;
    private final String fromAccountNumber;
    private final UUID toAccountId;
    private final String toAccountNumber;
    private final BigDecimal amount;
    private final LocalDateTime transactionDate;
    private final TransactionStatus status;

    public TransactionResponse(
        Long id,
        UUID fromAccountId,
        String fromAccountNumber,
        UUID toAccountId,
        String toAccountNumber,
        BigDecimal amount,
        LocalDateTime transactionDate,
        TransactionStatus status
    ) {
        this.id = id;
        this.fromAccountId = fromAccountId;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountId = toAccountId;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public UUID getFromAccountId() {
        return fromAccountId;
    }

    public String getFromAccountNumber() {
        return fromAccountNumber;
    }

    public UUID getToAccountId() {
        return toAccountId;
    }

    public String getToAccountNumber() {
        return toAccountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public TransactionStatus getStatus() {
        return status;
    }
}
