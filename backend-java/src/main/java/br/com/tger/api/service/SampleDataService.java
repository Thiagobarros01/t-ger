package br.com.tger.api.service;

import br.com.tger.api.dto.ModuleDto;
import br.com.tger.api.dto.TiAssetDto;
import br.com.tger.api.dto.TiTermDto;
import br.com.tger.api.dto.TicketDto;
import br.com.tger.api.dto.TicketMessageDto;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.model.AssetStatus;
import br.com.tger.api.model.AssetType;
import br.com.tger.api.model.IpMode;
import br.com.tger.api.model.ModuleCode;
import br.com.tger.api.model.TermType;
import br.com.tger.api.model.UserProfile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class SampleDataService {

    public List<ModuleDto> getModules() {
        return List.of(
                new ModuleDto(ModuleCode.DIRETORIA, "Gestao da Diretoria"),
                new ModuleDto(ModuleCode.TI, "Gestao da TI"),
                new ModuleDto(ModuleCode.FINANCEIRO, "Gestao do Financeiro"),
                new ModuleDto(ModuleCode.COMERCIAL, "Gestao do Comercial"),
                new ModuleDto(ModuleCode.LOGISTICA, "Gestao da Logistica"),
                new ModuleDto(ModuleCode.COMPRAS, "Gestao de Compras"),
                new ModuleDto(ModuleCode.VENDEDOR, "Gestao do Vendedor"),
                new ModuleDto(ModuleCode.RECEBIMENTO, "Gestao do Recebimento"),
                new ModuleDto(ModuleCode.CONFIGURACOES, "Configuracoes")
        );
    }

    public List<UserProfile> getProfiles() {
        return List.of(UserProfile.ADMINISTRADOR, UserProfile.GESTOR, UserProfile.OPERADOR);
    }

    public List<UserDto> getUsers() {
        return List.of(
                new UserDto(1L, "Thiago Admin", "thiago@tger.local", UserProfile.ADMINISTRADOR, List.of()),
                new UserDto(2L, "Marina TI", "marina.ti@tger.local", UserProfile.GESTOR, List.of(ModuleCode.TI)),
                new UserDto(3L, "Joao Suporte", "joao.suporte@tger.local", UserProfile.OPERADOR, List.of(ModuleCode.TI)),
                new UserDto(4L, "Ana Financeiro", "ana.fin@tger.local", UserProfile.GESTOR, List.of(ModuleCode.FINANCEIRO))
        );
    }

    public List<TiTermDto> getTiTerms() {
        return List.of(
                new TiTermDto(101L, TermType.COMODATO, "Termo de Responsabilidade", "Joao Suporte", "2026-01-15", "Ativo", "C:/documentos/ti/comodato/notebook-dell-101.pdf"),
                new TiTermDto(102L, TermType.COMODATO, "Termo de Responsabilidade", "Equipe Comercial", "2025-11-03", "Ativo", "C:/documentos/ti/comodato/tablet-comercial-102.pdf"),
                new TiTermDto(103L, TermType.CLT, "Termo de Responsabilidade", "Colaborador Novo", "2026-02-10", "Concluido", "C:/documentos/ti/clt/kit-admissional-103.pdf")
        );
    }

    public List<TiAssetDto> getTiAssets() {
        return List.of(
                new TiAssetDto(
                        201L,
                        "TI-0001",
                        "Empresa Matriz",
                        "EMP-ERP-001",
                        AssetType.NOTEBOOK,
                        "TI",
                        "Dell",
                        "Latitude 5420",
                        "SN-5420-ABCD",
                        "PAT-10021",
                        "Notebook corporativo para analista de suporte.",
                        AssetStatus.EM_USO,
                        3L,
                        "Joao Suporte",
                        101L,
                        "Termo de Responsabilidade",
                        List.of("2026-02-01 - RH -> TI", "2026-02-12 - TI -> Joao Suporte"),
                        IpMode.DHCP,
                        "192.168.0.45",
                        null,
                        Map.of("Sistema Operacional", "Windows 11 Pro")
                ),
                new TiAssetDto(
                        202L,
                        "TI-0002",
                        "Empresa Matriz",
                        "EMP-ERP-001",
                        AssetType.CELULAR,
                        "Comercial",
                        "Samsung",
                        "Galaxy A54",
                        "SN-A54-9988",
                        "PAT-10022",
                        "Aparelho para vendedor externo.",
                        AssetStatus.EM_USO,
                        2L,
                        "Marina TI",
                        102L,
                        "Termo de Responsabilidade",
                        List.of("2026-01-20 - Estoque -> Comercial"),
                        IpMode.ESTATICO,
                        "10.0.0.88",
                        "359999999999999",
                        Map.of("Plano", "Corporativo 20GB")
                )
        );
    }

    public List<TicketDto> getTiTickets() {
        return List.of(
                new TicketDto(
                        301L,
                        "Notebook sem acesso a VPN",
                        "Carlos Comercial",
                        "Joao Suporte",
                        "Alta",
                        "Em andamento",
                        List.of(
                                new TicketMessageDto(1L, "Carlos Comercial", "2026-02-25 09:10", "Nao consigo conectar na VPN desde cedo.", false),
                                new TicketMessageDto(2L, "Joao Suporte", "2026-02-25 09:22", "Vou validar credenciais e politica da maquina.", true),
                                new TicketMessageDto(3L, "Carlos Comercial", "2026-02-25 09:31", "Perfeito, fico no aguardo.", false)
                        )
                ),
                new TicketDto(
                        302L,
                        "Troca de mouse",
                        "Bianca Financeiro",
                        "Joao Suporte",
                        "Baixa",
                        "Novo",
                        List.of(
                                new TicketMessageDto(4L, "Bianca Financeiro", "2026-02-24 16:05", "Mouse com clique falhando.", false)
                        )
                )
        );
    }
}
