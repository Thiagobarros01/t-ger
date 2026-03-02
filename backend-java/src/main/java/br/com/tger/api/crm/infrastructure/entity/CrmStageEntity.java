package br.com.tger.api.crm.infrastructure.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "crm_stage", uniqueConstraints = {
        @UniqueConstraint(name = "uk_crm_stage_pipeline_ordem", columnNames = {"pipeline_id", "ordem"})
})
public class CrmStageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pipeline_id", nullable = false)
    private CrmPipelineEntity pipeline;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private Integer ordem;

    @Column(name = "is_won", nullable = false)
    private boolean isWon = false;

    @Column(name = "is_lost", nullable = false)
    private boolean isLost = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CrmPipelineEntity getPipeline() { return pipeline; }
    public void setPipeline(CrmPipelineEntity pipeline) { this.pipeline = pipeline; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public Integer getOrdem() { return ordem; }
    public void setOrdem(Integer ordem) { this.ordem = ordem; }
    public boolean isWon() { return isWon; }
    public void setWon(boolean won) { isWon = won; }
    public boolean isLost() { return isLost; }
    public void setLost(boolean lost) { isLost = lost; }
}
