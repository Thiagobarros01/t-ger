package br.com.tger.api.crm.infrastructure.repository;

import br.com.tger.api.crm.infrastructure.entity.CrmTaskStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmTaskStatusHistoryRepository extends JpaRepository<CrmTaskStatusHistoryEntity, Long> {
    List<CrmTaskStatusHistoryEntity> findByTaskIdOrderByChangedAtDesc(Long taskId);
}

