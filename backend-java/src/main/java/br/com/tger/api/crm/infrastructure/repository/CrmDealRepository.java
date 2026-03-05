package br.com.tger.api.crm.infrastructure.repository;

import br.com.tger.api.crm.infrastructure.entity.CrmDealEntity;
import br.com.tger.api.crm.domain.DealStatus;
import br.com.tger.api.crm.domain.OpportunityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CrmDealRepository extends JpaRepository<CrmDealEntity, Long> {
    List<CrmDealEntity> findByVendedorId(Long vendedorId);
    List<CrmDealEntity> findByVendedorEmailIgnoreCase(String email);
    List<CrmDealEntity> findByVendedorErpCodeIgnoreCase(String erpCode);
    Optional<CrmDealEntity> findFirstBySourceSyncKeyOrderByUpdatedAtDesc(String sourceSyncKey);
    Optional<CrmDealEntity> findFirstByClienteIdAndVendedorIdAndPipelineIdAndStatusAndTipoOportunidadeAndSourceSyncKeyIsNullOrderByUpdatedAtDesc(
            Long clienteId,
            Long vendedorId,
            Long pipelineId,
            DealStatus status,
            OpportunityType tipoOportunidade
    );
    boolean existsBySourceDealIdAndTipoOportunidade(Long sourceDealId, OpportunityType tipoOportunidade);
    Optional<CrmDealEntity> findFirstByClienteIdAndVendedorIdAndPipelineIdAndStatusAndTipoOportunidadeInOrderByUpdatedAtDesc(
            Long clienteId,
            Long vendedorId,
            Long pipelineId,
            DealStatus status,
            List<OpportunityType> tipos
    );
    long countByStageId(Long stageId);
}
