package br.com.tger.api.persistence.entity;

import br.com.tger.api.model.TermType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "ti_terms")
public class TiTermEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TermType type;

    @Column(name = "default_term_name", nullable = false)
    private String defaultTermName = "Termo de Responsabilidade";

    @Column(name = "linked_user_name", nullable = false)
    private String linkedUserName;

    @Column(name = "start_date")
    private String startDate;

    @Column(nullable = false)
    private String status;

    @Column(name = "document_path")
    private String documentPath;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public TermType getType() { return type; }
    public void setType(TermType type) { this.type = type; }
    public String getDefaultTermName() { return defaultTermName; }
    public void setDefaultTermName(String defaultTermName) { this.defaultTermName = defaultTermName; }
    public String getLinkedUserName() { return linkedUserName; }
    public void setLinkedUserName(String linkedUserName) { this.linkedUserName = linkedUserName; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String documentPath) { this.documentPath = documentPath; }
}
