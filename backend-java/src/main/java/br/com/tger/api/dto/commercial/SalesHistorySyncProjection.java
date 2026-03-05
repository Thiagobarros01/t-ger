package br.com.tger.api.dto.commercial;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface SalesHistorySyncProjection {
    String getCustomerErpCode();
    String getSellerErpCode();
    LocalDate getLastOrderDate();
    String getLastOrderNumber();
    String getLastOrderStatusCode();
    LocalDate getLastBilledDate();
    LocalDate getLastReturnedDate();
    LocalDate getLastCanceledDate();
    BigDecimal getLastOrderValue();
    BigDecimal getValue90d();
    Integer getOrders90d();
}
