package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {
    Optional<ProductEntity> findByErpCodeIgnoreCase(String erpCode);
    Optional<ProductEntity> findTopByOrderByIdDesc();
}
