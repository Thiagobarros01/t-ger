package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.ImportFieldConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImportFieldConfigRepository extends JpaRepository<ImportFieldConfigEntity, Long> {
    List<ImportFieldConfigEntity> findByEntityNameOrderByIdAsc(String entityName);
    Optional<ImportFieldConfigEntity> findByEntityNameAndFieldKey(String entityName, String fieldKey);
}
