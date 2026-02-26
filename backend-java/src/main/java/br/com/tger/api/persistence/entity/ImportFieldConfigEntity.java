package br.com.tger.api.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "import_field_configs", uniqueConstraints = @UniqueConstraint(columnNames = {"entity_name", "field_key"}))
public class ImportFieldConfigEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "entity_name", nullable = false)
    private String entityName;
    @Column(name = "field_key", nullable = false)
    private String fieldKey;
    @Column(name = "field_name", nullable = false)
    private String fieldName;
    @Column(nullable = false)
    private String alias;
    @Column(nullable = false)
    private boolean requiredFlag;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEntityName() { return entityName; }
    public void setEntityName(String entityName) { this.entityName = entityName; }
    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }
    public String getFieldName() { return fieldName; }
    public void setFieldName(String fieldName) { this.fieldName = fieldName; }
    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
    public boolean isRequiredFlag() { return requiredFlag; }
    public void setRequiredFlag(boolean requiredFlag) { this.requiredFlag = requiredFlag; }
}
