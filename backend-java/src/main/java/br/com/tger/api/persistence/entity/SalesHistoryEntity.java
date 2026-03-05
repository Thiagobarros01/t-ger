package br.com.tger.api.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "sales_history", indexes = {
        @Index(name = "idx_sales_history_customer", columnList = "customer_erp_code"),
        @Index(name = "idx_sales_history_seller", columnList = "seller_erp_code"),
        @Index(name = "idx_sales_history_order_date", columnList = "order_date")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uk_sales_history_external_key", columnNames = "external_key")
})
public class SalesHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_key", nullable = false, length = 120)
    private String externalKey;

    @Column(name = "company_erp_code")
    private String companyErpCode;

    @Column(name = "order_number")
    private String orderNumber;

    private String sequence;

    @Column(name = "nf_number")
    private String nfNumber;

    @Column(name = "order_date")
    private LocalDate orderDate;

    @Column(name = "billed_date")
    private LocalDate billedDate;

    @Column(name = "returned_date")
    private LocalDate returnedDate;

    @Column(name = "canceled_date")
    private LocalDate canceledDate;

    @Column(name = "order_status_code")
    private String orderStatusCode;

    @Column(name = "customer_erp_code")
    private String customerErpCode;

    @Column(name = "seller_erp_code")
    private String sellerErpCode;

    @Column(name = "product_erp_code")
    private String productErpCode;

    @Column(precision = 18, scale = 4)
    private BigDecimal quantity;

    @Column(name = "net_value", precision = 18, scale = 2)
    private BigDecimal netValue;

    @Column(name = "total_nf_value", precision = 18, scale = 2)
    private BigDecimal totalNfValue;

    @Column(name = "canceled_value", precision = 18, scale = 2)
    private BigDecimal canceledValue;

    @Column(name = "returned_value", precision = 18, scale = 2)
    private BigDecimal returnedValue;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    @PreUpdate
    void onChange() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getExternalKey() { return externalKey; }
    public void setExternalKey(String externalKey) { this.externalKey = externalKey; }
    public String getCompanyErpCode() { return companyErpCode; }
    public void setCompanyErpCode(String companyErpCode) { this.companyErpCode = companyErpCode; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public String getSequence() { return sequence; }
    public void setSequence(String sequence) { this.sequence = sequence; }
    public String getNfNumber() { return nfNumber; }
    public void setNfNumber(String nfNumber) { this.nfNumber = nfNumber; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public LocalDate getBilledDate() { return billedDate; }
    public void setBilledDate(LocalDate billedDate) { this.billedDate = billedDate; }
    public LocalDate getReturnedDate() { return returnedDate; }
    public void setReturnedDate(LocalDate returnedDate) { this.returnedDate = returnedDate; }
    public LocalDate getCanceledDate() { return canceledDate; }
    public void setCanceledDate(LocalDate canceledDate) { this.canceledDate = canceledDate; }
    public String getOrderStatusCode() { return orderStatusCode; }
    public void setOrderStatusCode(String orderStatusCode) { this.orderStatusCode = orderStatusCode; }
    public String getCustomerErpCode() { return customerErpCode; }
    public void setCustomerErpCode(String customerErpCode) { this.customerErpCode = customerErpCode; }
    public String getSellerErpCode() { return sellerErpCode; }
    public void setSellerErpCode(String sellerErpCode) { this.sellerErpCode = sellerErpCode; }
    public String getProductErpCode() { return productErpCode; }
    public void setProductErpCode(String productErpCode) { this.productErpCode = productErpCode; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getNetValue() { return netValue; }
    public void setNetValue(BigDecimal netValue) { this.netValue = netValue; }
    public BigDecimal getTotalNfValue() { return totalNfValue; }
    public void setTotalNfValue(BigDecimal totalNfValue) { this.totalNfValue = totalNfValue; }
    public BigDecimal getCanceledValue() { return canceledValue; }
    public void setCanceledValue(BigDecimal canceledValue) { this.canceledValue = canceledValue; }
    public BigDecimal getReturnedValue() { return returnedValue; }
    public void setReturnedValue(BigDecimal returnedValue) { this.returnedValue = returnedValue; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}

