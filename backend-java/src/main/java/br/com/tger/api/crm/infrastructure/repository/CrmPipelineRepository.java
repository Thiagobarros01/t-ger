package br.com.tger.api.crm.infrastructure.repository;

import br.com.tger.api.crm.domain.BusinessType;
import br.com.tger.api.crm.infrastructure.entity.CrmPipelineEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CrmPipelineRepository extends JpaRepository<CrmPipelineEntity, Long> {
    Optional<CrmPipelineEntity> findByNomeIgnoreCase(String nome);
    Optional<CrmPipelineEntity> findFirstByTipoNegocioAndAtivoTrueOrderByIdAsc(BusinessType tipoNegocio);
}
