package br.com.tger.api.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CrmCustomerProfileSummaryDto(
        Long customerId,
        String customerName,
        String customerErpCode,
        LocalDate lastOrderDate,
        Long daysWithoutPurchase,
        String topProductErpCode,
        String topProductName,
        BigDecimal topProductQuantity,
        Long totalOrders
) {}
