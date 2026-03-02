package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.TiTermEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TiTermRepository extends JpaRepository<TiTermEntity, Long> {
    Optional<TiTermEntity> findByDocumentPath(String documentPath);
}
