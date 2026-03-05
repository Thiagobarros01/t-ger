package br.com.tger.api.crm.infrastructure.entity;

import br.com.tger.api.crm.domain.BusinessType;
import br.com.tger.api.crm.domain.DealStatus;
import br.com.tger.api.crm.domain.OpportunityType;
import br.com.tger.api.persistence.entity.CompanyEntity;
import br.com.tger.api.persistence.entity.CustomerEntity;
import br.com.tger.api.persistence.entity.SellerEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "crm_deal", indexes = {
        @Index(name = "idx_crm_deal_stage_status", columnList = "stage_id,status"),
        @Index(name = "idx_crm_deal_vendedor", columnList = "vendedor_id")
})
public class CrmDealEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private CustomerEntity cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id")
    private CompanyEntity empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private SellerEntity vendedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_negocio", nullable = false)
    private BusinessType tipoNegocio;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_oportunidade")
    private OpportunityType tipoOportunidade = OpportunityType.NOVA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private CrmPipelineEntity pipeline;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stage_id", nullable = false)
    private CrmStageEntity stage;

    @Column(name = "valor_estimado", precision = 18, scale = 2)
    private BigDecimal valorEstimado;

    private Integer probabilidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DealStatus status = DealStatus.ABERTA;

    @Column(name = "data_prevista_fechamento")
    private LocalDate dataPrevistaFechamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "motivo_perda_id")
    private CrmLossReasonEntity motivoPerda;

    @Column(name = "source_deal_id")
    private Long sourceDealId;

    @Column(name = "source_order_number")
    private String sourceOrderNumber;

    @Column(name = "source_order_status")
    private String sourceOrderStatus;

    @Column(name = "source_order_date")
    private LocalDate sourceOrderDate;

    @Column(name = "source_sync_key", length = 180)
    private String sourceSyncKey;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CustomerEntity getCliente() { return cliente; }
    public void setCliente(CustomerEntity cliente) { this.cliente = cliente; }
    public CompanyEntity getEmpresa() { return empresa; }
    public void setEmpresa(CompanyEntity empresa) { this.empresa = empresa; }
    public SellerEntity getVendedor() { return vendedor; }
    public void setVendedor(SellerEntity vendedor) { this.vendedor = vendedor; }
    public BusinessType getTipoNegocio() { return tipoNegocio; }
    public void setTipoNegocio(BusinessType tipoNegocio) { this.tipoNegocio = tipoNegocio; }
    public OpportunityType getTipoOportunidade() { return tipoOportunidade; }
    public void setTipoOportunidade(OpportunityType tipoOportunidade) { this.tipoOportunidade = tipoOportunidade; }
    public CrmPipelineEntity getPipeline() { return pipeline; }
    public void setPipeline(CrmPipelineEntity pipeline) { this.pipeline = pipeline; }
    public CrmStageEntity getStage() { return stage; }
    public void setStage(CrmStageEntity stage) { this.stage = stage; }
    public BigDecimal getValorEstimado() { return valorEstimado; }
    public void setValorEstimado(BigDecimal valorEstimado) { this.valorEstimado = valorEstimado; }
    public Integer getProbabilidade() { return probabilidade; }
    public void setProbabilidade(Integer probabilidade) { this.probabilidade = probabilidade; }
    public DealStatus getStatus() { return status; }
    public void setStatus(DealStatus status) { this.status = status; }
    public LocalDate getDataPrevistaFechamento() { return dataPrevistaFechamento; }
    public void setDataPrevistaFechamento(LocalDate dataPrevistaFechamento) { this.dataPrevistaFechamento = dataPrevistaFechamento; }
    public CrmLossReasonEntity getMotivoPerda() { return motivoPerda; }
    public void setMotivoPerda(CrmLossReasonEntity motivoPerda) { this.motivoPerda = motivoPerda; }
    public Long getSourceDealId() { return sourceDealId; }
    public void setSourceDealId(Long sourceDealId) { this.sourceDealId = sourceDealId; }
    public String getSourceOrderNumber() { return sourceOrderNumber; }
    public void setSourceOrderNumber(String sourceOrderNumber) { this.sourceOrderNumber = sourceOrderNumber; }
    public String getSourceOrderStatus() { return sourceOrderStatus; }
    public void setSourceOrderStatus(String sourceOrderStatus) { this.sourceOrderStatus = sourceOrderStatus; }
    public LocalDate getSourceOrderDate() { return sourceOrderDate; }
    public void setSourceOrderDate(LocalDate sourceOrderDate) { this.sourceOrderDate = sourceOrderDate; }
    public String getSourceSyncKey() { return sourceSyncKey; }
    public void setSourceSyncKey(String sourceSyncKey) { this.sourceSyncKey = sourceSyncKey; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
