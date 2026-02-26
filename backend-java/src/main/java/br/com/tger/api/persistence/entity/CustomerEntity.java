package br.com.tger.api.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class CustomerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String code;
    @Column(name = "erp_code")
    private String erpCode;
    @Column(name = "corporate_name", nullable = false)
    private String corporateName;
    private String email;
    @Column(nullable = false)
    private String type;
    @Column(name = "trade_name")
    private String tradeName;
    private String phone;
    @Column(name = "erp_seller_code")
    private String erpSellerCode;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getErpCode() { return erpCode; }
    public void setErpCode(String erpCode) { this.erpCode = erpCode; }
    public String getCorporateName() { return corporateName; }
    public void setCorporateName(String corporateName) { this.corporateName = corporateName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getErpSellerCode() { return erpSellerCode; }
    public void setErpSellerCode(String erpSellerCode) { this.erpSellerCode = erpSellerCode; }
}
