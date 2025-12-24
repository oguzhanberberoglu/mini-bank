package com.main.mini_bank.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.main.mini_bank.model.entity.Account;
import com.main.mini_bank.model.dto.response.TransactionResponse;
import com.main.mini_bank.model.dto.request.TransferRequest;
import com.main.mini_bank.model.dto.response.TransferResponse;
import com.main.mini_bank.enums.TransactionStatus;
import com.main.mini_bank.model.entity.Transaction;
import com.main.mini_bank.model.entity.User;
import com.main.mini_bank.repository.AccountRepository;
import com.main.mini_bank.repository.TransactionRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CurrentUserService currentUserService;

    public TransactionService(
        AccountRepository accountRepository,
        TransactionRepository transactionRepository,
        CurrentUserService currentUserService
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        String fromNumber = request.getFromAccountNumber().trim();
        String toNumber = request.getToAccountNumber().trim();

        if (fromNumber.equals(toNumber)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer accounts must be different");
        }

        // Lock accounts in a stable order to avoid deadlocks during concurrent transfers.
        String firstLock = fromNumber.compareTo(toNumber) <= 0 ? fromNumber : toNumber;
        String secondLock = fromNumber.compareTo(toNumber) <= 0 ? toNumber : fromNumber;

        Account firstAccount = lockAccount(firstLock);
        Account secondAccount = lockAccount(secondLock);

        Account fromAccount = fromNumber.equals(firstAccount.getNumber()) ? firstAccount : secondAccount;
        Account toAccount = toNumber.equals(firstAccount.getNumber()) ? firstAccount : secondAccount;

        User user = currentUserService.getCurrentUser();
        if (!fromAccount.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot transfer from this account");
        }

        BigDecimal amount = request.getAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }

        LocalDateTime now = LocalDateTime.now();

        if (fromAccount.getBalance().compareTo(amount) < 0) {
            Transaction failed = buildTransaction(fromAccount, toAccount, amount, now, TransactionStatus.FAILED);
            transactionRepository.save(failed);
            return new TransferResponse(
                failed.getId(),
                failed.getStatus(),
                "Insufficient funds",
                fromAccount.getId(),
                fromNumber,
                toAccount.getId(),
                toNumber,
                amount,
                now
            );
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));

        Transaction success = buildTransaction(fromAccount, toAccount, amount, now, TransactionStatus.SUCCESS);
        transactionRepository.save(success);

        return new TransferResponse(
            success.getId(),
            success.getStatus(),
            "Transfer successful",
            fromAccount.getId(),
            fromNumber,
            toAccount.getId(),
            toNumber,
            amount,
            now
        );
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> history(UUID accountId) {
        User user = currentUserService.getCurrentUser();
        accountRepository.findByIdAndUserId(accountId, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        return transactionRepository.findHistoryWithAccounts(accountId).stream()
            .map(this::toResponse)
            .toList();
    }

    private Account lockAccount(String number) {
        return accountRepository.findByNumberForUpdate(number)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }

    private Transaction buildTransaction(
        Account fromAccount,
        Account toAccount,
        BigDecimal amount,
        LocalDateTime now,
        TransactionStatus status
    ) {
        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setTransactionDate(now);
        transaction.setStatus(status);
        return transaction;
    }

    private TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
            transaction.getId(),
            transaction.getFromAccount().getId(),
            transaction.getFromAccount().getNumber(),
            transaction.getToAccount().getId(),
            transaction.getToAccount().getNumber(),
            transaction.getAmount(),
            transaction.getTransactionDate(),
            transaction.getStatus()
        );
    }
}
