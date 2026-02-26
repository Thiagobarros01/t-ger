package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.AppUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUserEntity, Long> {
    Optional<AppUserEntity> findByEmailIgnoreCase(String email);
}
