package com.main.mini_bank.model.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.main.mini_bank.enums.TransactionStatus;

public class TransferResponse {

    private final Long transactionId;
    private final TransactionStatus status;
    private final String message;
    private final UUID fromAccountId;
    private final String fromAccountNumber;
    private final UUID toAccountId;
    private final String toAccountNumber;
    private final BigDecimal amount;
    private final LocalDateTime transactionDate;

    public TransferResponse(
        Long transactionId,
        TransactionStatus status,
        String message,
        UUID fromAccountId,
        String fromAccountNumber,
        UUID toAccountId,
        String toAccountNumber,
        BigDecimal amount,
        LocalDateTime transactionDate
    ) {
        this.transactionId = transactionId;
        this.status = status;
        this.message = message;
        this.fromAccountId = fromAccountId;
        this.fromAccountNumber = fromAccountNumber;
        this.toAccountId = toAccountId;
        this.toAccountNumber = toAccountNumber;
        this.amount = amount;
        this.transactionDate = transactionDate;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
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
}
