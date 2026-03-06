package br.com.tger.api.crm.dto;

import java.math.BigDecimal;

public interface CrmCustomerTopProductProjection {
    String getProductErpCode();
    String getProductName();
    BigDecimal getTotalQuantity();
    Long getTotalOrders();
}
