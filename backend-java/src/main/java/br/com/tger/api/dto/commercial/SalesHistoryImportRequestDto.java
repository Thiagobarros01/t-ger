package br.com.tger.api.dto.commercial;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SalesHistoryImportRequestDto(
        String companyErpCode,
        String orderNumber,
        String sequence,
        String nfNumber,
        LocalDate orderDate,
        LocalDate billedDate,
        LocalDate returnedDate,
        LocalDate canceledDate,
        String orderStatusCode,
        String customerErpCode,
        String sellerErpCode,
        String productErpCode,
        BigDecimal quantity,
        BigDecimal netValue,
        BigDecimal totalNfValue,
        BigDecimal canceledValue,
        BigDecimal returnedValue
) {
}

