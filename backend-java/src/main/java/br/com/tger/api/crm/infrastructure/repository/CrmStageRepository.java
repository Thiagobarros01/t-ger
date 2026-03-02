package br.com.tger.api.crm.infrastructure.repository;

import br.com.tger.api.crm.infrastructure.entity.CrmStageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmStageRepository extends JpaRepository<CrmStageEntity, Long> {
    List<CrmStageEntity> findByPipelineIdOrderByOrdemAsc(Long pipelineId);
}
