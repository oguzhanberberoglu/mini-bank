package com.main.mini_bank.controller;

import java.util.List;
import java.util.UUID;

import com.main.mini_bank.enums.TransactionStatus;
import com.main.mini_bank.model.dto.response.TransactionResponse;
import com.main.mini_bank.model.dto.request.TransferRequest;
import com.main.mini_bank.model.dto.response.TransferResponse;
import com.main.mini_bank.service.TransactionService;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Money transfer and transaction history")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    @Operation(
        summary = "Transfer money",
        description = "Transfers money from one account to another. The sender account must belong to the authenticated user."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transfer completed"),
        @ApiResponse(responseCode = "400", description = "Validation or business rule error",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class)))
    })
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transactionService.transfer(request);
        HttpStatus status = response.getStatus() == TransactionStatus.SUCCESS
            ? HttpStatus.OK
            : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/account/{accountId}")
    @Operation(
        summary = "Transaction history",
        description = "Returns the transfer history for the specified account. Access is restricted to the account owner."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transactions returned"),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Account not found",
            content = @Content(schema = @Schema(implementation = com.main.mini_bank.exception.ErrorResponse.class)))
    })
    public List<TransactionResponse> history(@PathVariable UUID accountId) {
        return transactionService.history(accountId);
    }
}
