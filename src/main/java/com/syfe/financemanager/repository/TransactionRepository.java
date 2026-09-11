package com.syfe.financemanager.repository;

import com.syfe.financemanager.model.Transaction;
import com.syfe.financemanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Persistence operations for financial transactions.
 */
public interface TransactionRepository extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
    /**
     * Returns the user's transactions newest first.
     */
    List<Transaction> findByUserOrderByDateDesc(User user);
    
    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.date >= :startDate AND t.date <= :endDate ORDER BY t.date DESC")
    List<Transaction> findByUserAndDateBetween(@Param("user") User user, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    boolean existsByCategoryId(Long categoryId);
}
