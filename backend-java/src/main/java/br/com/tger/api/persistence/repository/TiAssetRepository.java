package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.TiAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TiAssetRepository extends JpaRepository<TiAssetEntity, Long> {
    Optional<TiAssetEntity> findTopByOrderByIdDesc();
    List<TiAssetEntity> findByResponsibleUserId(Long responsibleUserId);
    Optional<TiAssetEntity> findByInternalCodeIgnoreCase(String internalCode);
    boolean existsByInternalCodeIgnoreCase(String internalCode);
}
