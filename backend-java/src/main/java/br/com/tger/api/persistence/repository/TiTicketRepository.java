package br.com.tger.api.persistence.repository;

import br.com.tger.api.persistence.entity.TiTicketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TiTicketRepository extends JpaRepository<TiTicketEntity, Long> {
}
