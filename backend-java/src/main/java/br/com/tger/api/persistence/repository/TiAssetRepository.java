package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.TiAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TiAssetRepository extends JpaRepository<TiAssetEntity, Long>, JpaSpecificationExecutor<TiAssetEntity> {
    Optional<TiAssetEntity> findTopByOrderByIdDesc();
    List<TiAssetEntity> findByResponsibleUserId(Long responsibleUserId);
    Optional<TiAssetEntity> findByInternalCodeIgnoreCase(String internalCode);
    boolean existsByInternalCodeIgnoreCase(String internalCode);

    @Query(value = "select coalesce(max(cast(regexp_replace(internal_code, '\\D', '', 'g') as bigint)), 0) from ti_assets", nativeQuery = true)
    long findMaxInternalCodeSequence();
}
