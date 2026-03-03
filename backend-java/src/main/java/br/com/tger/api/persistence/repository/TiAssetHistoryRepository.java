package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.TiAssetHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TiAssetHistoryRepository extends JpaRepository<TiAssetHistoryEntity, Long>, JpaSpecificationExecutor<TiAssetHistoryEntity> {
    Optional<TiAssetHistoryEntity> findTopByAsset_IdOrderByChangedAtDesc(Long assetId);
}
