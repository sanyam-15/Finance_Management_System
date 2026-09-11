package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.TransactionRequest;
import com.syfe.financemanager.dto.TransactionResponse;
import com.syfe.financemanager.exception.BadRequestException;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.model.Category;
import com.syfe.financemanager.model.CategoryType;
import com.syfe.financemanager.model.Transaction;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.repository.CategoryRepository;
import com.syfe.financemanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for creating, reading, updating, and deleting financial transactions.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Creates a new transaction for the authenticated user.
     *
     * @param user    the authenticated user
     * @param request transaction payload
     * @return created transaction response
     */
    @Transactional
    public TransactionResponse createTransaction(User user, TransactionRequest request) {
        validateTransactionRequest(request);

        LocalDate date = LocalDate.parse(request.getDate());
        if (date.isAfter(LocalDate.now())) {
            throw new BadRequestException("Date cannot be in the future");
        }

        Category category = getCategoryForUser(request.getCategory(), user);

        Transaction transaction = Transaction.builder()
                .amount(BigDecimal.valueOf(request.getAmount()))
                .date(date)
                .category(category)
                .description(request.getDescription())
                .type(category.getType())
                .user(user)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    /**
     * Returns the user's transactions newest-first, optionally filtered by date range,
     * category name, category id, and/or transaction type.
     *
     * @param user         the authenticated user
     * @param startDateStr optional start date (YYYY-MM-DD)
     * @param endDateStr   optional end date (YYYY-MM-DD)
     * @param category     optional category name
     * @param categoryId   optional category id
     * @param type         optional transaction type (INCOME/EXPENSE)
     * @return filtered transaction list
     */
    public List<TransactionResponse> getTransactions(User user, String startDateStr, String endDateStr,
                                                     String category, Long categoryId, String type) {
        List<Transaction> transactions;
        if (startDateStr != null && endDateStr != null) {
            LocalDate startDate = LocalDate.parse(startDateStr);
            LocalDate endDate = LocalDate.parse(endDateStr);
            transactions = transactionRepository.findByUserAndDateBetween(user, startDate, endDate);
        } else {
            transactions = transactionRepository.findByUserOrderByDateDesc(user);
        }

        if (category != null && !category.trim().isEmpty()) {
            transactions = transactions.stream()
                    .filter(t -> t.getCategory().getName().equalsIgnoreCase(category))
                    .collect(Collectors.toList());
        }

        if (categoryId != null) {
            transactions = transactions.stream()
                    .filter(t -> t.getCategory().getId().equals(categoryId))
                    .collect(Collectors.toList());
        }

        if (type != null && !type.trim().isEmpty()) {
            CategoryType categoryType;
            try {
                categoryType = CategoryType.valueOf(type.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Invalid transaction type");
            }
            transactions = transactions.stream()
                    .filter(t -> t.getType() == categoryType)
                    .collect(Collectors.toList());
        }

        return transactions.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Updates a transaction. The date field cannot be modified.
     *
     * @param user    the authenticated user
     * @param id      transaction id
     * @param request fields to update
     * @return updated transaction response
     */
    @Transactional
    public TransactionResponse updateTransaction(User user, Long id, TransactionRequest request) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        if (request.getAmount() != null) {
            if (request.getAmount() <= 0) throw new BadRequestException("Amount must be positive");
            transaction.setAmount(BigDecimal.valueOf(request.getAmount()));
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            Category category = getCategoryForUser(request.getCategory(), user);
            transaction.setCategory(category);
            transaction.setType(category.getType());
        }

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    /**
     * Permanently deletes a transaction so it no longer affects goals or reports.
     *
     * @param user the authenticated user
     * @param id   transaction id
     */
    @Transactional
    public void deleteTransaction(User user, Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ResourceNotFoundException("Transaction not found");
        }

        transactionRepository.delete(transaction);
    }

    private Category getCategoryForUser(String categoryName, User user) {
        return categoryRepository.findByNameAndUser(categoryName, user)
                .orElseGet(() -> categoryRepository.findByNameAndUserIsNull(categoryName)
                        .orElseThrow(() -> new BadRequestException("Category not found")));
    }

    private void validateTransactionRequest(TransactionRequest request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BadRequestException("Amount must be positive");
        }
        if (request.getDate() == null) {
            throw new BadRequestException("Date is required");
        }
        if (request.getCategory() == null) {
            throw new BadRequestException("Category is required");
        }
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount().doubleValue())
                .date(transaction.getDate().toString())
                .category(transaction.getCategory().getName())
                .description(transaction.getDescription())
                .type(transaction.getType().name())
                .build();
    }
}
