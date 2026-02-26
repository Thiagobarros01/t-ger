package br.com.tger.api.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "sellers")
public class SellerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String code;
    @Column(name = "erp_code", nullable = false, unique = true)
    private String erpCode;
    @Column(nullable = false)
    private String name;
    private String email;
    private String phone;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getErpCode() { return erpCode; }
    public void setErpCode(String erpCode) { this.erpCode = erpCode; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
