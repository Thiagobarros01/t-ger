package br.com.tger.api.crm.infrastructure.repository;

import br.com.tger.api.crm.infrastructure.entity.CrmDealEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrmDealRepository extends JpaRepository<CrmDealEntity, Long> {
    List<CrmDealEntity> findByVendedorId(Long vendedorId);
    List<CrmDealEntity> findByVendedorEmailIgnoreCase(String email);
}
