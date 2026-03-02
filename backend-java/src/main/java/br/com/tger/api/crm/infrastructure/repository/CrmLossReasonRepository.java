package br.com.tger.api.crm.infrastructure.repository;

import br.com.tger.api.crm.infrastructure.entity.CrmLossReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrmLossReasonRepository extends JpaRepository<CrmLossReasonEntity, Long> {
}
