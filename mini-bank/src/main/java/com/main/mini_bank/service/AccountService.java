package com.main.mini_bank.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.main.mini_bank.model.dto.request.AccountCreateRequest;
import com.main.mini_bank.model.dto.response.AccountResponse;
import com.main.mini_bank.model.dto.request.AccountUpdateRequest;
import com.main.mini_bank.model.entity.Account;
import com.main.mini_bank.model.entity.User;
import com.main.mini_bank.repository.AccountRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CurrentUserService currentUserService;

    public AccountService(AccountRepository accountRepository, CurrentUserService currentUserService) {
        this.accountRepository = accountRepository;
        this.currentUserService = currentUserService;
    }

    public AccountResponse create(AccountCreateRequest request) {
        User user = currentUserService.getCurrentUser();
        String number = request.getNumber().trim();
        String name = request.getName().trim();

        if (accountRepository.existsByNumber(number)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account number already exists");
        }
        if (accountRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account name already exists");
        }

        BigDecimal balance = request.getInitialBalance() == null ? BigDecimal.ZERO : request.getInitialBalance();
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Initial balance must be non-negative");
        }

        Account account = new Account();
        account.setNumber(number);
        account.setName(name);
        account.setBalance(balance);
        account.setUser(user);

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    public List<AccountResponse> search(String number, String name) {
        User user = currentUserService.getCurrentUser();
        String numberQuery = normalizeQueryParam(number);
        String nameQuery = normalizeQueryParam(name);
        String numberFilter = numberQuery == null ? null : numberQuery.toLowerCase(Locale.ROOT);
        String nameFilter = nameQuery == null ? null : nameQuery.toLowerCase(Locale.ROOT);

        // Apply case-insensitive filters in memory for consistent matching.
        return accountRepository.findByUserId(user.getId()).stream()
            .filter(account -> matchesFilter(account.getNumber(), numberFilter))
            .filter(account -> matchesFilter(account.getName(), nameFilter))
            .map(this::toResponse)
            .toList();
    }

    public AccountResponse getById(UUID id) {
        User user = currentUserService.getCurrentUser();
        Account account = accountRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
        return toResponse(account);
    }

    public AccountResponse update(UUID id, AccountUpdateRequest request) {
        User user = currentUserService.getCurrentUser();
        Account account = accountRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        String number = request.getNumber().trim();
        String name = request.getName().trim();

        if (!number.equals(account.getNumber()) && accountRepository.existsByNumber(number)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account number already exists");
        }
        if (!name.equals(account.getName()) && accountRepository.existsByName(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Account name already exists");
        }

        account.setNumber(number);
        account.setName(name);

        Account saved = accountRepository.save(account);
        return toResponse(saved);
    }

    public void delete(UUID id) {
        User user = currentUserService.getCurrentUser();
        Account account = accountRepository.findByIdAndUserId(id, user.getId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));

        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account balance must be zero to delete");
        }

        accountRepository.delete(account);
    }

    private String normalizeQueryParam(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean matchesFilter(String value, String filter) {
        if (filter == null) {
            return true;
        }
        return value != null && value.toLowerCase(Locale.ROOT).contains(filter);
    }

    private AccountResponse toResponse(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getNumber(),
            account.getName(),
            account.getBalance(),
            account.getCreatedAt(),
            account.getUpdatedAt()
        );
    }
}
