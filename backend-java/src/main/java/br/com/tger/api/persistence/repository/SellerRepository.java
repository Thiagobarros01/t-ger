package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.SellerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository extends JpaRepository<SellerEntity, Long> {
    Optional<SellerEntity> findByErpCodeIgnoreCase(String erpCode);
    Optional<SellerEntity> findTopByOrderByIdDesc();
}
