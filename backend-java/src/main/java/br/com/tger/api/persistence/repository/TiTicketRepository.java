package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.TiTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface TiTicketRepository extends JpaRepository<TiTicketEntity, Long>, JpaSpecificationExecutor<TiTicketEntity> {
    Optional<TiTicketEntity> findBySubjectAndRequester(String subject, String requester);
}
