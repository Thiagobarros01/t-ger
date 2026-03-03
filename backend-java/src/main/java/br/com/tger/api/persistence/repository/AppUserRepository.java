package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUserEntity, Long>, JpaSpecificationExecutor<AppUserEntity> {
    Optional<AppUserEntity> findByEmailIgnoreCase(String email);
    Optional<AppUserEntity> findFirstByNameIgnoreCase(String name);
}
