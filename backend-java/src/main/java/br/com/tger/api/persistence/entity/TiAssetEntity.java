package br.com.tger.api.persistence.entity;

import br.com.tger.api.model.AssetStatus;
import br.com.tger.api.model.AssetType;
import br.com.tger.api.model.IpMode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "ti_assets")
public class TiAssetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "internal_code", nullable = false, unique = true)
    private String internalCode;

    private String company;
    @Column(name = "company_erp_code")
    private String companyErpCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    private AssetType assetType;

    private String department;
    private String brand;
    private String model;
    @Column(name = "serial_number")
    private String serialNumber;
    private String patrimony;

    @Column(name = "detailed_description", length = 4000)
    private String detailedDescription;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssetStatus status;

    @Column(name = "responsible_user_id")
    private Long responsibleUserId;
    @Column(name = "responsible_user_name")
    private String responsibleUserName;

    @Column(name = "linked_term_id")
    private Long linkedTermId;
    @Column(name = "linked_term_title")
    private String linkedTermTitle;

    @Lob
    @Column(name = "transfer_history_text")
    private String transferHistoryText;

    @Enumerated(EnumType.STRING)
    @Column(name = "ip_mode", nullable = false)
    private IpMode ipMode;

    @Column(name = "ip_address")
    private String ipAddress;
    private String imei;

    @Lob
    @Column(name = "extra_fields_json")
    private String extraFieldsJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInternalCode() { return internalCode; }
    public void setInternalCode(String internalCode) { this.internalCode = internalCode; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getCompanyErpCode() { return companyErpCode; }
    public void setCompanyErpCode(String companyErpCode) { this.companyErpCode = companyErpCode; }
    public AssetType getAssetType() { return assetType; }
    public void setAssetType(AssetType assetType) { this.assetType = assetType; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public String getPatrimony() { return patrimony; }
    public void setPatrimony(String patrimony) { this.patrimony = patrimony; }
    public String getDetailedDescription() { return detailedDescription; }
    public void setDetailedDescription(String detailedDescription) { this.detailedDescription = detailedDescription; }
    public AssetStatus getStatus() { return status; }
    public void setStatus(AssetStatus status) { this.status = status; }
    public Long getResponsibleUserId() { return responsibleUserId; }
    public void setResponsibleUserId(Long responsibleUserId) { this.responsibleUserId = responsibleUserId; }
    public String getResponsibleUserName() { return responsibleUserName; }
    public void setResponsibleUserName(String responsibleUserName) { this.responsibleUserName = responsibleUserName; }
    public Long getLinkedTermId() { return linkedTermId; }
    public void setLinkedTermId(Long linkedTermId) { this.linkedTermId = linkedTermId; }
    public String getLinkedTermTitle() { return linkedTermTitle; }
    public void setLinkedTermTitle(String linkedTermTitle) { this.linkedTermTitle = linkedTermTitle; }
    public String getTransferHistoryText() { return transferHistoryText; }
    public void setTransferHistoryText(String transferHistoryText) { this.transferHistoryText = transferHistoryText; }
    public IpMode getIpMode() { return ipMode; }
    public void setIpMode(IpMode ipMode) { this.ipMode = ipMode; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public String getImei() { return imei; }
    public void setImei(String imei) { this.imei = imei; }
    public String getExtraFieldsJson() { return extraFieldsJson; }
    public void setExtraFieldsJson(String extraFieldsJson) { this.extraFieldsJson = extraFieldsJson; }
}
