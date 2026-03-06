package br.com.tger.api.crm.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface CrmCustomerOrderHistoryProjection {
    Long getHistoryId();
    LocalDate getOrderDate();
    String getOrderNumber();
    String getOrderStatusCode();
    String getProductErpCode();
    String getProductName();
    BigDecimal getQuantity();
    BigDecimal getTotalValue();
}
