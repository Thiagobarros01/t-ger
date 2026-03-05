package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.GlobalParameterEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GlobalParameterRepository extends JpaRepository<GlobalParameterEntity, Long> {
    Optional<GlobalParameterEntity> findByParamKey(String paramKey);
}

