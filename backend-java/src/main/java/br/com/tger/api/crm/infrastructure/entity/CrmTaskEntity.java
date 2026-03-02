package br.com.tger.api.crm.infrastructure.entity;

import br.com.tger.api.crm.domain.TaskPriority;
import br.com.tger.api.crm.domain.TaskStatus;
import br.com.tger.api.persistence.entity.AppUserEntity;
import br.com.tger.api.persistence.entity.CustomerEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "crm_task", indexes = {
        @Index(name = "idx_crm_task_responsavel_status", columnList = "responsavel_id,status")
})
public class CrmTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private CustomerEntity cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id")
    private CrmDealEntity deal;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.PENDENTE;

    @Column(name = "vencimento_em")
    private Instant vencimentoEm;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "responsavel_id", nullable = false)
    private AppUserEntity responsavel;

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
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public TaskPriority getPrioridade() { return prioridade; }
    public void setPrioridade(TaskPriority prioridade) { this.prioridade = prioridade; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Instant getVencimentoEm() { return vencimentoEm; }
    public void setVencimentoEm(Instant vencimentoEm) { this.vencimentoEm = vencimentoEm; }
    public AppUserEntity getResponsavel() { return responsavel; }
    public void setResponsavel(AppUserEntity responsavel) { this.responsavel = responsavel; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
