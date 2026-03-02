package br.com.tger.api.crm.infrastructure.repository;

import br.com.tger.api.crm.infrastructure.entity.CrmPipelineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmPipelineRepository extends JpaRepository<CrmPipelineEntity, Long> {
}
