package br.com.tger.api.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "global_parameter", uniqueConstraints = {
        @UniqueConstraint(name = "uk_global_parameter_key", columnNames = "param_key")
})
public class GlobalParameterEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "param_key", nullable = false, length = 100)
    private String paramKey;

    @Column(name = "param_label", nullable = false, length = 120)
    private String label;

    @Column(name = "param_description", length = 255)
    private String description;

    @Column(name = "param_value", nullable = false, length = 120)
    private String value;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getParamKey() { return paramKey; }
    public void setParamKey(String paramKey) { this.paramKey = paramKey; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

