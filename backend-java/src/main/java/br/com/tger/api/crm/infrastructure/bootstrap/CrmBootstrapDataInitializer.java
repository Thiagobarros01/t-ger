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

@Configuration
public class CrmBootstrapDataInitializer {

    @Bean
    CommandLineRunner seedCrmCatalog(
            CrmPipelineRepository pipelineRepository,
            CrmStageRepository stageRepository,
            CrmLossReasonRepository lossReasonRepository
    ) {
        return args -> {
            if (pipelineRepository.count() == 0) {
                CrmPipelineEntity b2c = new CrmPipelineEntity();
                b2c.setNome("Pipeline B2C");
                b2c.setTipoNegocio(BusinessType.B2C);
                b2c.setAtivo(true);
                b2c = pipelineRepository.save(b2c);

                CrmPipelineEntity b2b = new CrmPipelineEntity();
                b2b.setNome("Pipeline B2B");
                b2b.setTipoNegocio(BusinessType.B2B);
                b2b.setAtivo(true);
                b2b = pipelineRepository.save(b2b);

                stageRepository.save(stage(b2c, "Lead", 1, false, false));
                stageRepository.save(stage(b2c, "Qualificacao", 2, false, false));
                stageRepository.save(stage(b2c, "Ganho", 3, true, false));
                stageRepository.save(stage(b2c, "Perdido", 4, false, true));

                stageRepository.save(stage(b2b, "Prospeccao", 1, false, false));
                stageRepository.save(stage(b2b, "Diagnostico", 2, false, false));
                stageRepository.save(stage(b2b, "Proposta", 3, false, false));
                stageRepository.save(stage(b2b, "Ganho", 4, true, false));
                stageRepository.save(stage(b2b, "Perdido", 5, false, true));
            }

            if (lossReasonRepository.count() == 0) {
                lossReasonRepository.save(lossReason("Preco"));
                lossReasonRepository.save(lossReason("Sem retorno"));
                lossReasonRepository.save(lossReason("Concorrente"));
            }
        };
    }

    private CrmStageEntity stage(CrmPipelineEntity pipeline, String nome, int ordem, boolean won, boolean lost) {
        CrmStageEntity stage = new CrmStageEntity();
        stage.setPipeline(pipeline);
        stage.setNome(nome);
        stage.setOrdem(ordem);
        stage.setWon(won);
        stage.setLost(lost);
        return stage;
    }

    private CrmLossReasonEntity lossReason(String descricao) {
        CrmLossReasonEntity reason = new CrmLossReasonEntity();
        reason.setDescricao(descricao);
        reason.setAtivo(true);
        return reason;
    }
}
