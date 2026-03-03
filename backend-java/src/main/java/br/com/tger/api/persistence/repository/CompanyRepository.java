package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long>, JpaSpecificationExecutor<CompanyEntity> {
    Optional<CompanyEntity> findTopByOrderByIdDesc();
    Optional<CompanyEntity> findByErpCodeIgnoreCase(String erpCode);
}
