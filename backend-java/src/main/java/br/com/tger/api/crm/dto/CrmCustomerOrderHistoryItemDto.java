package br.com.tger.api.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CrmCustomerOrderHistoryItemDto(
        Long historyId,
        LocalDate orderDate,
        String orderNumber,
        String orderStatusCode,
        String productErpCode,
        String productName,
        BigDecimal quantity,
        BigDecimal totalValue
) {}
