package com.syfe.financemanager.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionResponse {
    private Long id;
    private Double amount;
    private String date;
    private String category;
    private String description;
    private String type;
}
