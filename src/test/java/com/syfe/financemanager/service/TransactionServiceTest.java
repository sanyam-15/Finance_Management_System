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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Category salary;
    private Category food;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("user@example.com").build();
        salary = Category.builder().id(1L).name("Salary").type(CategoryType.INCOME).isCustom(false).build();
        food = Category.builder().id(2L).name("Food").type(CategoryType.EXPENSE).isCustom(false).build();
    }

    @Test
    void createTransaction_success() {
        TransactionRequest request = TransactionRequest.builder()
                .amount(5000.0)
                .date("2024-01-15")
                .category("Salary")
                .description("Pay")
                .build();
        when(categoryRepository.findByNameAndUser("Salary", user)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("Salary")).thenReturn(Optional.of(salary));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> {
            Transaction t = inv.getArgument(0);
            t.setId(1L);
            return t;
        });

        TransactionResponse response = transactionService.createTransaction(user, request);

        assertEquals(1L, response.getId());
        assertEquals("INCOME", response.getType());
        assertEquals("Salary", response.getCategory());
    }

    @Test
    void createTransaction_futureDate_throwsBadRequest() {
        TransactionRequest request = TransactionRequest.builder()
                .amount(100.0)
                .date(LocalDate.now().plusDays(2).toString())
                .category("Food")
                .build();

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(user, request));
    }

    @Test
    void createTransaction_invalidAmount_throwsBadRequest() {
        TransactionRequest request = TransactionRequest.builder()
                .amount(-1.0)
                .date("2024-01-01")
                .category("Food")
                .build();

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(user, request));
    }

    @Test
    void getTransactions_filtersByCategoryTypeAndId() {
        Transaction income = Transaction.builder()
                .id(1L).amount(BigDecimal.valueOf(100)).date(LocalDate.of(2024, 1, 20))
                .category(salary).type(CategoryType.INCOME).user(user).build();
        Transaction expense = Transaction.builder()
                .id(2L).amount(BigDecimal.valueOf(40)).date(LocalDate.of(2024, 1, 10))
                .category(food).type(CategoryType.EXPENSE).user(user).build();
        when(transactionRepository.findByUserAndDateBetween(eq(user), any(), any()))
                .thenReturn(List.of(income, expense));

        List<TransactionResponse> byType = transactionService.getTransactions(
                user, "2024-01-01", "2024-01-31", null, null, "INCOME");
        assertEquals(1, byType.size());
        assertEquals("Salary", byType.get(0).getCategory());

        List<TransactionResponse> byCategoryId = transactionService.getTransactions(
                user, "2024-01-01", "2024-01-31", null, 2L, null);
        assertEquals(1, byCategoryId.size());
        assertEquals("Food", byCategoryId.get(0).getCategory());

        List<TransactionResponse> byName = transactionService.getTransactions(
                user, "2024-01-01", "2024-01-31", "Food", null, null);
        assertEquals(1, byName.size());
    }

    @Test
    void getTransactions_invalidType_throwsBadRequest() {
        when(transactionRepository.findByUserOrderByDateDesc(user)).thenReturn(List.of());
        assertThrows(BadRequestException.class,
                () -> transactionService.getTransactions(user, null, null, null, null, "UNKNOWN"));
    }

    @Test
    void updateTransaction_ignoresDateAndUpdatesFields() {
        Transaction existing = Transaction.builder()
                .id(1L).amount(BigDecimal.valueOf(100)).date(LocalDate.of(2024, 1, 15))
                .category(salary).type(CategoryType.INCOME).description("Old").user(user).build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findByNameAndUser("Food", user)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("Food")).thenReturn(Optional.of(food));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransactionRequest request = TransactionRequest.builder()
                .amount(200.0)
                .date("2099-01-01")
                .category("Food")
                .description("Updated")
                .build();

        TransactionResponse response = transactionService.updateTransaction(user, 1L, request);

        assertEquals("2024-01-15", response.getDate());
        assertEquals(200.0, response.getAmount());
        assertEquals("Food", response.getCategory());
        assertEquals("EXPENSE", response.getType());
    }

    @Test
    void updateTransaction_otherUser_throwsNotFound() {
        User other = User.builder().id(2L).build();
        Transaction existing = Transaction.builder().id(1L).user(other).build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.updateTransaction(user, 1L, TransactionRequest.builder().amount(10.0).build()));
    }

    @Test
    void deleteTransaction_success() {
        Transaction existing = Transaction.builder().id(1L).user(user).build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));

        transactionService.deleteTransaction(user, 1L);

        verify(transactionRepository).delete(existing);
    }

    @Test
    void deleteTransaction_missing_throwsNotFound() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(user, 99L));
    }

    @Test
    void createTransaction_missingDate_throwsBadRequest() {
        TransactionRequest request = TransactionRequest.builder().amount(10.0).category("Food").build();
        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(user, request));
    }

    @Test
    void createTransaction_missingCategory_throwsBadRequest() {
        TransactionRequest request = TransactionRequest.builder().amount(10.0).date("2024-01-01").build();
        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(user, request));
    }

    @Test
    void createTransaction_unknownCategory_throwsBadRequest() {
        TransactionRequest request = TransactionRequest.builder()
                .amount(10.0).date("2024-01-01").category("Unknown").build();
        when(categoryRepository.findByNameAndUser("Unknown", user)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUserIsNull("Unknown")).thenReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(user, request));
    }

    @Test
    void updateTransaction_invalidAmount_throwsBadRequest() {
        Transaction existing = Transaction.builder().id(1L).user(user).amount(BigDecimal.TEN)
                .date(LocalDate.of(2024, 1, 1)).category(food).type(CategoryType.EXPENSE).build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));
        assertThrows(BadRequestException.class, () -> transactionService.updateTransaction(user, 1L,
                TransactionRequest.builder().amount(-1.0).build()));
    }

    @Test
    void updateTransaction_missing_throwsNotFound() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transactionService.updateTransaction(user, 1L,
                TransactionRequest.builder().amount(1.0).build()));
    }

    @Test
    void deleteTransaction_otherUser_throwsNotFound() {
        Transaction existing = Transaction.builder().id(1L).user(User.builder().id(2L).build()).build();
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));
        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(user, 1L));
    }

    @Test
    void getTransactions_withoutDateRange_usesNewestFirst() {
        when(transactionRepository.findByUserOrderByDateDesc(user)).thenReturn(List.of());
        assertEquals(0, transactionService.getTransactions(user, null, null, null, null, null).size());
    }
}
