package br.com.tger.api.crm.infrastructure.repository;

import br.com.tger.api.crm.infrastructure.entity.CrmTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmTaskRepository extends JpaRepository<CrmTaskEntity, Long> {
    List<CrmTaskEntity> findByResponsavelIdAndStatus(Long responsavelId, br.com.tger.api.crm.domain.TaskStatus status);
    List<CrmTaskEntity> findByResponsavelId(Long responsavelId);
}
