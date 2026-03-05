package br.com.tger.api.crm.infrastructure.bootstrap;

import br.com.tger.api.crm.domain.BusinessType;
import br.com.tger.api.crm.infrastructure.entity.CrmLossReasonEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmPipelineEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmStageEntity;
import br.com.tger.api.crm.infrastructure.repository.CrmLossReasonRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmPipelineRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmStageRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CrmBootstrapDataInitializer {

    @Bean
    CommandLineRunner seedCrmCatalog(
            CrmPipelineRepository pipelineRepository,
            CrmStageRepository stageRepository,
            CrmLossReasonRepository lossReasonRepository
    ) {
        return args -> {
            CrmPipelineEntity b2c = upsertPipeline(pipelineRepository, "Pipeline B2C", BusinessType.B2C);
            CrmPipelineEntity b2b = upsertPipeline(pipelineRepository, "Pipeline B2B", BusinessType.B2B);

            // Funil operacional padrão para sincronizacao ERP:
            // Prospeccao -> Em Negociacao -> Venda Fechada -> Recorrente -> Venda Perdida
            syncStages(stageRepository, b2c, List.of(
                    stageDef("Prospeccao", 1, false, false),
                    stageDef("Em Negociacao", 2, false, false),
                    stageDef("Venda Fechada", 3, true, false),
                    stageDef("Recorrente", 4, false, false),
                    stageDef("Venda Perdida", 5, false, true)
            ));

            syncStages(stageRepository, b2b, List.of(
                    stageDef("Prospeccao", 1, false, false),
                    stageDef("Em Negociacao", 2, false, false),
                    stageDef("Venda Fechada", 3, true, false),
                    stageDef("Recorrente", 4, false, false),
                    stageDef("Venda Perdida", 5, false, true)
            ));

            if (lossReasonRepository.count() == 0) {
                lossReasonRepository.save(lossReason("Preco"));
                lossReasonRepository.save(lossReason("Sem retorno"));
                lossReasonRepository.save(lossReason("Concorrente"));
            }
        };
    }

    private CrmPipelineEntity upsertPipeline(CrmPipelineRepository repository, String nome, BusinessType tipo) {
        CrmPipelineEntity pipeline = repository.findByNomeIgnoreCase(nome).orElseGet(CrmPipelineEntity::new);
        pipeline.setNome(nome);
        pipeline.setTipoNegocio(tipo);
        pipeline.setAtivo(true);
        return repository.save(pipeline);
    }

    private void syncStages(CrmStageRepository repository, CrmPipelineEntity pipeline, List<StageDef> defs) {
        for (StageDef def : defs) {
            CrmStageEntity stage = repository.findByPipelineIdAndOrdem(pipeline.getId(), def.ordem());
            if (stage == null) {
                stage = new CrmStageEntity();
                stage.setPipeline(pipeline);
                stage.setOrdem(def.ordem());
            }
            stage.setNome(def.nome());
            stage.setWon(def.won());
            stage.setLost(def.lost());
            repository.save(stage);
        }
    }

    private CrmLossReasonEntity lossReason(String descricao) {
        CrmLossReasonEntity reason = new CrmLossReasonEntity();
        reason.setDescricao(descricao);
        reason.setAtivo(true);
        return reason;
    }

    private StageDef stageDef(String nome, int ordem, boolean won, boolean lost) {
        return new StageDef(nome, ordem, won, lost);
    }

    private record StageDef(String nome, int ordem, boolean won, boolean lost) {}
}
