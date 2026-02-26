package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    Optional<CompanyEntity> findTopByOrderByIdDesc();
}
