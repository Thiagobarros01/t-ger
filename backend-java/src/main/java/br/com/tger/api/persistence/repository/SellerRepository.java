package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.SellerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<SellerEntity, Long>, JpaSpecificationExecutor<SellerEntity> {
    Optional<SellerEntity> findByErpCodeIgnoreCase(String erpCode);
    Optional<SellerEntity> findByEmailIgnoreCase(String email);
    Optional<SellerEntity> findTopByOrderByIdDesc();
}
