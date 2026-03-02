package br.com.tger.api.crm.infrastructure.entity;

import br.com.tger.api.crm.domain.InteractionType;
import br.com.tger.api.persistence.entity.AppUserEntity;
import br.com.tger.api.persistence.entity.CustomerEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "crm_interaction", indexes = {
        @Index(name = "idx_crm_interaction_cliente", columnList = "cliente_id")
})
public class CrmInteractionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private CustomerEntity cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id")
    private CrmDealEntity deal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InteractionType tipo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "ocorrido_em", nullable = false)
    private Instant ocorridoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "criado_por", nullable = false)
    private AppUserEntity criadoPor;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CustomerEntity getCliente() { return cliente; }
    public void setCliente(CustomerEntity cliente) { this.cliente = cliente; }
    public CrmDealEntity getDeal() { return deal; }
    public void setDeal(CrmDealEntity deal) { this.deal = deal; }
    public InteractionType getTipo() { return tipo; }
    public void setTipo(InteractionType tipo) { this.tipo = tipo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public Instant getOcorridoEm() { return ocorridoEm; }
    public void setOcorridoEm(Instant ocorridoEm) { this.ocorridoEm = ocorridoEm; }
    public AppUserEntity getCriadoPor() { return criadoPor; }
    public void setCriadoPor(AppUserEntity criadoPor) { this.criadoPor = criadoPor; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
