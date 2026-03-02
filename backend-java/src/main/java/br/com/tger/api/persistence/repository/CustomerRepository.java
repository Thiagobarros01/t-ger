package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByErpCodeIgnoreCase(String erpCode);
    List<CustomerEntity> findByErpSellerCodeIgnoreCase(String erpSellerCode);
    Optional<CustomerEntity> findTopByOrderByIdDesc();
}
