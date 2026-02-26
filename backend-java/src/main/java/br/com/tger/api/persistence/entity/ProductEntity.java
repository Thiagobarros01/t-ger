package br.com.tger.api.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String code;
    @Column(name = "erp_code")
    private String erpCode;
    @Column(nullable = false)
    private String description;
    private String department;
    private String category;
    @Column(name = "line_name")
    private String line;
    private String manufacturer;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getErpCode() { return erpCode; }
    public void setErpCode(String erpCode) { this.erpCode = erpCode; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getLine() { return line; }
    public void setLine(String line) { this.line = line; }
    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }
}
