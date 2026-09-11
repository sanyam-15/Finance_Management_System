package com.syfe.financemanager.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private String name;
    private String type;
    private boolean isCustom;
}
