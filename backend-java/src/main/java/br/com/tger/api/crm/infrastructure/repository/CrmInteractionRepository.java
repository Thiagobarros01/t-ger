package br.com.tger.api.crm.infrastructure.repository;

import br.com.tger.api.crm.infrastructure.entity.CrmInteractionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmInteractionRepository extends JpaRepository<CrmInteractionEntity, Long> {
    List<CrmInteractionEntity> findByClienteIdOrderByOcorridoEmDesc(Long clienteId);
}
