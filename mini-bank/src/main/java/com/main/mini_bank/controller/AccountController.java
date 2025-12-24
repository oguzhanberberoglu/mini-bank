package com.main.mini_bank.controller;

import java.util.List;
import java.util.UUID;

import com.main.mini_bank.model.dto.request.AccountCreateRequest;
import com.main.mini_bank.model.dto.response.AccountResponse;
import com.main.mini_bank.model.dto.request.AccountUpdateRequest;
import com.main.mini_bank.service.AccountService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account management for authenticated users")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @Operation(
        summary = "Create account",
        description = "Creates a new account owned by the authenticated user."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Account created"),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Account number or name already exists",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class)))
    })
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountCreateRequest request) {
        AccountResponse response = accountService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(
        summary = "Search accounts",
        description = "Returns accounts owned by the authenticated user. Filters by number and name using partial matches."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Accounts returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class)))
    })
    public List<AccountResponse> search(
        @Parameter(description = "Account number filter (partial match)")
        @RequestParam(required = false) String number,
        @Parameter(description = "Account name filter (partial match)")
        @RequestParam(required = false) String name
    ) {
        return accountService.search(number, name);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get account details",
        description = "Returns account details for the authenticated owner."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class)))
    })
    public AccountResponse getById(@PathVariable UUID id) {
        return accountService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update account",
        description = "Updates account number and name for the authenticated owner."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account updated"),
        @ApiResponse(responseCode = "400", description = "Validation error",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "409", description = "Account number or name already exists",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class)))
    })
    public AccountResponse update(
        @PathVariable UUID id,
        @Valid @RequestBody AccountUpdateRequest request
    ) {
        return accountService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete account",
        description = "Deletes an account owned by the authenticated user. Only accounts with zero balance can be deleted."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Account deleted"),
        @ApiResponse(responseCode = "400", description = "Balance must be zero",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        accountService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
