package br.com.tger.api.persistence.service;

import br.com.tger.api.model.AssetStatus;
import br.com.tger.api.model.AssetType;
import br.com.tger.api.model.IpMode;
import br.com.tger.api.model.ModuleCode;
import br.com.tger.api.model.TermType;
import br.com.tger.api.model.UserProfile;
import br.com.tger.api.persistence.entity.AppUserEntity;
import br.com.tger.api.persistence.entity.CompanyEntity;
import br.com.tger.api.persistence.entity.CustomerEntity;
import br.com.tger.api.persistence.entity.ProductEntity;
import br.com.tger.api.persistence.entity.SellerEntity;
import br.com.tger.api.persistence.entity.TiAssetEntity;
import br.com.tger.api.persistence.entity.TiTermEntity;
import br.com.tger.api.persistence.entity.TiTicketEntity;
import br.com.tger.api.persistence.entity.TiTicketMessageEntity;
import br.com.tger.api.persistence.repository.AppUserRepository;
import br.com.tger.api.persistence.repository.CompanyRepository;
import br.com.tger.api.persistence.repository.CustomerRepository;
import br.com.tger.api.persistence.repository.ProductRepository;
import br.com.tger.api.persistence.repository.SellerRepository;
import br.com.tger.api.persistence.repository.TiAssetRepository;
import br.com.tger.api.persistence.repository.TiTermRepository;
import br.com.tger.api.persistence.repository.TiTicketRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
public class BootstrapDataInitializer {

    @Bean
    CommandLineRunner seedUsers(AppUserRepository userRepository) {
        return args -> {
            upsertUser(userRepository, "Thiago Admin", "thiago@tger.local", "USR-ADMIN", UserProfile.ADMINISTRADOR, List.of());
            upsertUser(userRepository, "Marina TI", "marina.ti@tger.local", "USR-0002", UserProfile.GESTOR, List.of(ModuleCode.TI));
            upsertUser(userRepository, "Joao Suporte", "joao.suporte@tger.local", "USR-0003", UserProfile.OPERADOR, List.of(ModuleCode.TI));
            upsertUser(userRepository, "Ana Financeiro", "ana.fin@tger.local", "USR-0004", UserProfile.GESTOR, List.of(ModuleCode.FINANCEIRO));
            upsertUser(userRepository, "Carlos Comercial", "carlos.comercial@tger.local", "USR-0005", UserProfile.GESTOR, List.of(ModuleCode.COMERCIAL));
            upsertUser(userRepository, "Lucas Vendas", "lucas.vendas@tger.local", "USR-0006", UserProfile.OPERADOR, List.of(ModuleCode.COMERCIAL));
            upsertUser(userRepository, "Paula Config", "paula.config@tger.local", "USR-0007", UserProfile.GESTOR, List.of(ModuleCode.CONFIGURACOES));
        };
    }

    @Bean
    CommandLineRunner seedCommercialAndConfigData(
            CompanyRepository companyRepository,
            SellerRepository sellerRepository,
            ProductRepository productRepository,
            CustomerRepository customerRepository
    ) {
        return args -> {
            upsertCompany(companyRepository, "EMP-0001", "Empresa Matriz", "EMP-ERP-001");
            upsertCompany(companyRepository, "EMP-0002", "Filial Sul", "EMP-ERP-002");

            upsertSeller(sellerRepository, "VDR-00001", "VND-ERP-001", "Lucas Vendas", "lucas.vendas@tger.local", "(11) 99999-1111");
            upsertSeller(sellerRepository, "VDR-00002", "VND-ERP-002", "Carla Hunter", "carla.hunter@tger.local", "(11) 99999-2222");
            upsertSeller(sellerRepository, "VDR-00003", "VND-ERP-003", "Marco KeyAccount", "marco.key@tger.local", "(11) 99999-3333");

            upsertProduct(productRepository, "PRD-00001", "PRD-ERP-001", "Notebook Dell Latitude", "TI", "Hardware", "Notebooks", "Dell");
            upsertProduct(productRepository, "PRD-00002", "PRD-ERP-002", "Smartphone Samsung A54", "Comercial", "Mobilidade", "Celulares", "Samsung");
            upsertProduct(productRepository, "PRD-00003", "PRD-ERP-003", "Monitor LG 24", "TI", "Perifericos", "Monitores", "LG");

            upsertCustomer(customerRepository, "CLI-00001", "CLI-ERP-001", "Mercado Bom Preco LTDA", "compras@mercadobompreco.com",
                    "PJ", "Mercado Bom Preco", "(11) 3232-1000", "VND-ERP-001");
            upsertCustomer(customerRepository, "CLI-00002", "CLI-ERP-002", "Construtora Horizonte SA", "contato@horizonte.com",
                    "PJ", "Construtora Horizonte", "(11) 3434-2000", "VND-ERP-002");
            upsertCustomer(customerRepository, "CLI-00003", "CLI-ERP-003", "Julia Mendes", "julia.mendes@email.com",
                    "PF", "Julia Mendes", "(11) 95555-3000", "VND-ERP-001");
            upsertCustomer(customerRepository, "CLI-00004", "CLI-ERP-004", "Papelaria Central LTDA", "suprimentos@papelariacentral.com",
                    "PJ", "Papelaria Central", "(11) 3666-4000", "VND-ERP-003");
        };
    }

    @Bean
    CommandLineRunner seedTiData(
            AppUserRepository userRepository,
            TiTermRepository termRepository,
            TiAssetRepository assetRepository,
            TiTicketRepository ticketRepository
    ) {
        return args -> {
            AppUserEntity joao = userRepository.findByEmailIgnoreCase("joao.suporte@tger.local").orElse(null);
            AppUserEntity marina = userRepository.findByEmailIgnoreCase("marina.ti@tger.local").orElse(null);
            Long joaoId = joao == null ? null : joao.getId();
            Long marinaId = marina == null ? null : marina.getId();
            String joaoName = joao == null ? "Joao Suporte" : joao.getName();
            String marinaName = marina == null ? "Marina TI" : marina.getName();

            TiTermEntity termJoao = upsertTerm(termRepository, "storage/docs/ti/termo-joao-comodato.pdf", TermType.COMODATO, joaoName, "2026-02-01", "Ativo");
            TiTermEntity termMarina = upsertTerm(termRepository, "storage/docs/ti/termo-marina-clt.pdf", TermType.CLT, marinaName, "2026-01-10", "Ativo");

            upsertAsset(assetRepository, "TI-0001", "Empresa Matriz", "EMP-ERP-001", AssetType.NOTEBOOK, "TI", "Dell", "Latitude 5420",
                    "SN-5420-ABCD", "PAT-10021", "Notebook de suporte", AssetStatus.EM_USO, joaoId, joaoName, termJoao.getId(),
                    "Termo de Responsabilidade", "2026-02-01 - RH -> TI\n2026-02-02 - TI -> Joao", IpMode.DHCP, "192.168.0.45", null);
            upsertAsset(assetRepository, "TI-0002", "Empresa Matriz", "EMP-ERP-001", AssetType.CELULAR, "Comercial", "Samsung", "A54",
                    "SN-A54-9988", "PAT-10022", "Celular de campo", AssetStatus.EM_USO, marinaId, marinaName, termMarina.getId(),
                    "Termo de Responsabilidade", "2026-01-20 - Estoque -> Comercial", IpMode.ESTATICO, "10.0.0.88", "359999999999999");
            upsertAsset(assetRepository, "TI-0003", "Filial Sul", "EMP-ERP-002", AssetType.MONITOR, "Financeiro", "LG", "24MP400",
                    "SN-LG24-9090", "PAT-10023", "Monitor reserva", AssetStatus.DISPONIVEL, marinaId, marinaName, null,
                    null, "", IpMode.DHCP, null, null);

            upsertTicket(ticketRepository, "Notebook sem acesso a VPN", "Carlos Comercial", "Joao Suporte", "Alta", "Em andamento",
                    List.of(
                            Map.entry("Carlos Comercial", "Nao consigo conectar na VPN desde cedo."),
                            Map.entry("Joao Suporte", "Vou validar credenciais e politica da maquina.")
                    ));
            upsertTicket(ticketRepository, "Troca de mouse", "Bianca Financeiro", "Joao Suporte", "Baixa", "Novo",
                    List.of(Map.entry("Bianca Financeiro", "Mouse com clique falhando.")));
            upsertTicket(ticketRepository, "Atualizacao de inventario", "Marina TI", "Marina TI", "Media", "Pendente",
                    List.of(Map.entry("Marina TI", "Conferir ativos da filial sul ate sexta.")));
        };
    }

    private AppUserEntity upsertUser(AppUserRepository repository, String name, String email, String erp, UserProfile profile, List<ModuleCode> modules) {
        Optional<AppUserEntity> existing = repository.findByEmailIgnoreCase(email);
        if (existing.isPresent()) return existing.get();
        AppUserEntity entity = new AppUserEntity();
        entity.setName(name);
        entity.setEmail(email);
        entity.setErpCode(erp);
        entity.setProfile(profile);
        entity.setModules(modules);
        entity.setActive(true);
        return repository.save(entity);
    }

    private CompanyEntity upsertCompany(CompanyRepository repository, String code, String name, String erpCode) {
        Optional<CompanyEntity> existing = repository.findByErpCodeIgnoreCase(erpCode);
        if (existing.isPresent()) return existing.get();
        CompanyEntity entity = new CompanyEntity();
        entity.setCode(code);
        entity.setName(name);
        entity.setErpCode(erpCode);
        return repository.save(entity);
    }

    private SellerEntity upsertSeller(SellerRepository repository, String code, String erpCode, String name, String email, String phone) {
        Optional<SellerEntity> existing = repository.findByErpCodeIgnoreCase(erpCode);
        if (existing.isPresent()) return existing.get();
        SellerEntity entity = new SellerEntity();
        entity.setCode(code);
        entity.setErpCode(erpCode);
        entity.setName(name);
        entity.setEmail(email);
        entity.setPhone(phone);
        return repository.save(entity);
    }

    private ProductEntity upsertProduct(
            ProductRepository repository, String code, String erpCode, String description,
            String department, String category, String line, String manufacturer
    ) {
        Optional<ProductEntity> existing = repository.findByErpCodeIgnoreCase(erpCode);
        if (existing.isPresent()) return existing.get();
        ProductEntity entity = new ProductEntity();
        entity.setCode(code);
        entity.setErpCode(erpCode);
        entity.setDescription(description);
        entity.setDepartment(department);
        entity.setCategory(category);
        entity.setLine(line);
        entity.setManufacturer(manufacturer);
        return repository.save(entity);
    }

    private CustomerEntity upsertCustomer(
            CustomerRepository repository, String code, String erpCode, String corporateName, String email,
            String type, String tradeName, String phone, String erpSellerCode
    ) {
        Optional<CustomerEntity> existing = repository.findByErpCodeIgnoreCase(erpCode);
        if (existing.isPresent()) return existing.get();
        CustomerEntity entity = new CustomerEntity();
        entity.setCode(code);
        entity.setErpCode(erpCode);
        entity.setCorporateName(corporateName);
        entity.setEmail(email);
        entity.setType(type);
        entity.setTradeName(tradeName);
        entity.setPhone(phone);
        entity.setErpSellerCode(erpSellerCode);
        return repository.save(entity);
    }

    private TiTermEntity upsertTerm(
            TiTermRepository repository, String documentPath, TermType type, String linkedUserName,
            String startDate, String status
    ) {
        Optional<TiTermEntity> existing = repository.findByDocumentPath(documentPath);
        if (existing.isPresent()) return existing.get();
        TiTermEntity entity = new TiTermEntity();
        entity.setType(type);
        entity.setDefaultTermName("Termo de Responsabilidade");
        entity.setLinkedUserName(linkedUserName);
        entity.setStartDate(startDate);
        entity.setStatus(status);
        entity.setDocumentPath(documentPath);
        return repository.save(entity);
    }

    private void upsertAsset(
            TiAssetRepository repository,
            String internalCode,
            String company,
            String companyErpCode,
            AssetType assetType,
            String department,
            String brand,
            String model,
            String serialNumber,
            String patrimony,
            String detailedDescription,
            AssetStatus status,
            Long responsibleUserId,
            String responsibleUserName,
            Long linkedTermId,
            String linkedTermTitle,
            String transferHistory,
            IpMode ipMode,
            String ipAddress,
            String imei
    ) {
        if (repository.existsByInternalCodeIgnoreCase(internalCode)) return;
        TiAssetEntity entity = new TiAssetEntity();
        entity.setInternalCode(internalCode);
        entity.setCompany(company);
        entity.setCompanyErpCode(companyErpCode);
        entity.setAssetType(assetType);
        entity.setDepartment(department);
        entity.setBrand(brand);
        entity.setModel(model);
        entity.setSerialNumber(serialNumber);
        entity.setPatrimony(patrimony);
        entity.setDetailedDescription(detailedDescription);
        entity.setStatus(status);
        entity.setResponsibleUserId(responsibleUserId);
        entity.setResponsibleUserName(responsibleUserName);
        entity.setLinkedTermId(linkedTermId);
        entity.setLinkedTermTitle(linkedTermTitle);
        entity.setTransferHistoryText(transferHistory);
        entity.setIpMode(ipMode);
        entity.setIpAddress(ipAddress);
        entity.setImei(imei);
        entity.setExtraFieldsJson("{}");
        repository.save(entity);
    }

    private TiTicketEntity upsertTicket(
            TiTicketRepository repository,
            String subject,
            String requester,
            String assignedTo,
            String priority,
            String status,
            List<Map.Entry<String, String>> messages
    ) {
        Optional<TiTicketEntity> existing = repository.findBySubjectAndRequester(subject, requester);
        if (existing.isPresent()) return existing.get();
        TiTicketEntity ticket = new TiTicketEntity();
        ticket.setSubject(subject);
        ticket.setRequester(requester);
        ticket.setAssignedTo(assignedTo);
        ticket.setPriority(priority);
        ticket.setStatus(status);
        int index = 1;
        for (Map.Entry<String, String> item : messages) {
            String sentAt = "2026-03-0" + index + " 09:0" + index;
            ticket.getMessages().add(message(ticket, item.getKey(), sentAt, item.getValue()));
            index++;
        }
        return repository.save(ticket);
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
