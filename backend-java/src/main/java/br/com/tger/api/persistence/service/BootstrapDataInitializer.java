package br.com.tger.api.persistence.service;

import br.com.tger.api.model.ModuleCode;
import br.com.tger.api.model.UserProfile;
import br.com.tger.api.persistence.entity.AppUserEntity;
import br.com.tger.api.persistence.entity.TiTicketEntity;
import br.com.tger.api.persistence.entity.TiTicketMessageEntity;
import br.com.tger.api.persistence.repository.AppUserRepository;
import br.com.tger.api.persistence.repository.TiTicketRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class BootstrapDataInitializer {

    @Bean
    CommandLineRunner seedUsers(AppUserRepository userRepository) {
        return args -> {
            if (userRepository.count() > 0) return;
            userRepository.save(user("Thiago Admin", "thiago@tger.local", "USR-ADMIN", UserProfile.ADMINISTRADOR, List.of()));
            userRepository.save(user("Marina TI", "marina.ti@tger.local", "USR-0002", UserProfile.GESTOR, List.of(ModuleCode.TI)));
            userRepository.save(user("Joao Suporte", "joao.suporte@tger.local", "USR-0003", UserProfile.OPERADOR, List.of(ModuleCode.TI)));
            userRepository.save(user("Ana Financeiro", "ana.fin@tger.local", "USR-0004", UserProfile.GESTOR, List.of(ModuleCode.FINANCEIRO)));
        };
    }

    @Bean
    CommandLineRunner seedTiTickets(TiTicketRepository ticketRepository) {
        return args -> {
            if (ticketRepository.count() > 0) return;

            TiTicketEntity t1 = new TiTicketEntity();
            t1.setSubject("Notebook sem acesso a VPN");
            t1.setRequester("Carlos Comercial");
            t1.setAssignedTo("Joao Suporte");
            t1.setPriority("Alta");
            t1.setStatus("Em andamento");
            t1.getMessages().add(message(t1, "Carlos Comercial", "2026-02-25 09:10", "Nao consigo conectar na VPN desde cedo."));
            t1.getMessages().add(message(t1, "Joao Suporte", "2026-02-25 09:22", "Vou validar credenciais e politica da maquina."));
            ticketRepository.save(t1);

            TiTicketEntity t2 = new TiTicketEntity();
            t2.setSubject("Troca de mouse");
            t2.setRequester("Bianca Financeiro");
            t2.setAssignedTo("Joao Suporte");
            t2.setPriority("Baixa");
            t2.setStatus("Novo");
            t2.getMessages().add(message(t2, "Bianca Financeiro", "2026-02-24 16:05", "Mouse com clique falhando."));
            ticketRepository.save(t2);
        };
    }

    private AppUserEntity user(String name, String email, String erp, UserProfile profile, List<ModuleCode> modules) {
        AppUserEntity u = new AppUserEntity();
        u.setName(name);
        u.setEmail(email);
        u.setErpCode(erp);
        u.setProfile(profile);
        u.setModules(modules);
        u.setActive(true);
        return u;
    }

    private TiTicketMessageEntity message(TiTicketEntity ticket, String author, String sentAt, String text) {
        TiTicketMessageEntity msg = new TiTicketMessageEntity();
        msg.setTicket(ticket);
        msg.setAuthor(author);
        msg.setSentAt(sentAt);
        msg.setMessage(text);
        return msg;
    }
}
