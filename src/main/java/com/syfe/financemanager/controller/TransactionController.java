package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.TransactionRequest;
import com.syfe.financemanager.dto.TransactionResponse;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST endpoints for transaction CRUD and filtering.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Creates a transaction for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@AuthenticationPrincipal User user,
                                                                 @Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.createTransaction(user, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Lists transactions newest-first with optional filters.
     */
    @GetMapping
    public ResponseEntity<?> getTransactions(@AuthenticationPrincipal User user,
                                             @RequestParam(required = false) String startDate,
                                             @RequestParam(required = false) String endDate,
                                             @RequestParam(required = false) String category,
                                             @RequestParam(required = false) Long categoryId,
                                             @RequestParam(required = false) String type) {
        List<TransactionResponse> transactions = transactionService.getTransactions(
                user, startDate, endDate, category, categoryId, type);
        Map<String, Object> response = new HashMap<>();
        response.put("transactions", transactions);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a transaction. Date cannot be changed.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(@AuthenticationPrincipal User user,
                                                                 @PathVariable Long id,
                                                                 @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.updateTransaction(user, id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a transaction permanently.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@AuthenticationPrincipal User user,
                                               @PathVariable Long id) {
        transactionService.deleteTransaction(user, id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Transaction deleted successfully");
        return ResponseEntity.ok(response);
    }
}
