package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.TransactionRequest;
import com.syfe.financemanager.dto.TransactionResponse;
import com.syfe.financemanager.model.User;
import com.syfe.financemanager.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @InjectMocks
    private TransactionController transactionController;

    private final User user = User.builder().id(1L).username("user@example.com").build();

    @Test
    void createTransaction_returnsCreated() {
        TransactionRequest request = TransactionRequest.builder().amount(100.0).date("2024-01-01").category("Food").build();
        TransactionResponse body = TransactionResponse.builder().id(1L).amount(100.0).category("Food").type("EXPENSE").build();
        when(transactionService.createTransaction(user, request)).thenReturn(body);

        ResponseEntity<TransactionResponse> response = transactionController.createTransaction(user, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1L, response.getBody().getId());
    }

    @Test
    void getTransactions_wrapsList() {
        when(transactionService.getTransactions(user, null, null, null, null, null))
                .thenReturn(List.of(TransactionResponse.builder().id(1L).build()));

        ResponseEntity<?> response = transactionController.getTransactions(user, null, null, null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(1, ((List<?>) body.get("transactions")).size());
    }

    @Test
    void updateTransaction_returnsOk() {
        TransactionRequest request = TransactionRequest.builder().amount(200.0).build();
        TransactionResponse body = TransactionResponse.builder().id(1L).amount(200.0).build();
        when(transactionService.updateTransaction(user, 1L, request)).thenReturn(body);

        ResponseEntity<TransactionResponse> response = transactionController.updateTransaction(user, 1L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(200.0, response.getBody().getAmount());
    }

    @Test
    void deleteTransaction_returnsMessage() {
        ResponseEntity<?> response = transactionController.deleteTransaction(user, 1L);

        verify(transactionService).deleteTransaction(user, 1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Transaction deleted successfully", body.get("message"));
    }
}
