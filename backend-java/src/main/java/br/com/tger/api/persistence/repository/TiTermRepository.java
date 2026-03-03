package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.TiTermEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TiTermRepository extends JpaRepository<TiTermEntity, Long>, JpaSpecificationExecutor<TiTermEntity> {
    Optional<TiTermEntity> findByDocumentPath(String documentPath);
}
