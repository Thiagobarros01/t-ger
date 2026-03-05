package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.imports.ImportFieldConfigDto;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.persistence.entity.ImportFieldConfigEntity;
import br.com.tger.api.persistence.repository.ImportFieldConfigRepository;
import br.com.tger.api.service.AccessControlService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ImportConfigPersistenceService {
    private final ImportFieldConfigRepository repository;
    private final AccessControlService accessControlService;
    public ImportConfigPersistenceService(ImportFieldConfigRepository repository, AccessControlService accessControlService) {
        this.repository = repository;
        this.accessControlService = accessControlService;
    }

    private static final Map<String, List<ImportFieldConfigDto>> DEFAULTS = Map.of(
            "clientes", List.of(
                    new ImportFieldConfigDto("codigo_erp", "Codigo ERP", "codigo_erp", false),
                    new ImportFieldConfigDto("razao_social", "Razao Social", "razao_social", true),
                    new ImportFieldConfigDto("email", "Email", "email", false),
                    new ImportFieldConfigDto("tipo", "Tipo (PJ/PF)", "tipo", true),
                    new ImportFieldConfigDto("nome_fantasia", "Nome Fantasia", "nome_fantasia", false),
                    new ImportFieldConfigDto("telefone", "Telefone", "telefone", false),
                    new ImportFieldConfigDto("codigo_erp_vendedor", "Codigo ERP Vendedor", "codigo_erp_vendedor", false)
            ),
            "produtos", List.of(
                    new ImportFieldConfigDto("codigo_erp", "Codigo ERP", "codigo_erp", false),
                    new ImportFieldConfigDto("descricao", "Descricao", "descricao", true),
                    new ImportFieldConfigDto("departamento", "Departamento", "departamento", false),
                    new ImportFieldConfigDto("categoria", "Categoria", "categoria", false),
                    new ImportFieldConfigDto("linha", "Linha", "linha", false),
                    new ImportFieldConfigDto("fabricante", "Fabricante", "fabricante", false)
            ),
            "vendedores", List.of(
                    new ImportFieldConfigDto("codigo_erp", "Codigo ERP", "codigo_erp", true),
                    new ImportFieldConfigDto("nome", "Nome do vendedor", "nome", true),
                    new ImportFieldConfigDto("email", "Email", "email", false),
                    new ImportFieldConfigDto("telefone", "Telefone", "telefone", false)
            ),
            "historico_vendas", List.of(
                    new ImportFieldConfigDto("cd_emp", "Empresa ERP", "cd_emp", false),
                    new ImportFieldConfigDto("nu_ped", "Numero Pedido", "nu_ped", false),
                    new ImportFieldConfigDto("seq", "Sequencia", "seq", false),
                    new ImportFieldConfigDto("nu_nf", "Numero NF", "nu_nf", false),
                    new ImportFieldConfigDto("dt_ped", "Data Pedido", "dt_ped", false),
                    new ImportFieldConfigDto("dt_fatur", "Data Faturamento", "dt_fatur", false),
                    new ImportFieldConfigDto("dt_devol", "Data Devolucao", "dt_devol", false),
                    new ImportFieldConfigDto("dt_canc", "Data Cancelamento", "dt_canc", false),
                    new ImportFieldConfigDto("cd_situacao_pedido", "Status Pedido", "cd_situacao_pedido", false),
                    new ImportFieldConfigDto("cd_clien", "Cliente ERP", "cd_clien", true),
                    new ImportFieldConfigDto("cd_vend", "Vendedor ERP", "cd_vend", true),
                    new ImportFieldConfigDto("cd_prod", "Produto ERP", "cd_prod", true),
                    new ImportFieldConfigDto("qtde", "Quantidade", "qtde", false),
                    new ImportFieldConfigDto("vl_liquido", "Valor Liquido", "vl_liquido", false),
                    new ImportFieldConfigDto("vl_tot_nf", "Valor Total NF", "vl_tot_nf", false),
                    new ImportFieldConfigDto("vl_cancelado", "Valor Cancelado", "vl_cancelado", false),
                    new ImportFieldConfigDto("vl_devolvido", "Valor Devolvido", "vl_devolvido", false)
            )
    );

    @Transactional
    public List<ImportFieldConfigDto> getByEntity(String entity, String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (accessControlService.isOperator(user)) return List.of();
        var rows = repository.findByEntityNameOrderByIdAsc(entity);
        if (rows.isEmpty()) {
            seedDefaults(entity);
            rows = repository.findByEntityNameOrderByIdAsc(entity);
        }
        return rows.stream().map(this::toDto).toList();
    }

    @Transactional
    public List<ImportFieldConfigDto> saveAll(String entity, List<ImportFieldConfigDto> configs, String authorizationHeader) {
        accessControlService.assertNotOperator(accessControlService.requireUser(authorizationHeader));
        for (ImportFieldConfigDto dto : configs) {
            ImportFieldConfigEntity row = repository.findByEntityNameAndFieldKey(entity, dto.key()).orElseGet(ImportFieldConfigEntity::new);
            row.setEntityName(entity);
            row.setFieldKey(dto.key());
            row.setFieldName(dto.name());
            row.setAlias(dto.alias());
            row.setRequiredFlag(dto.required());
            repository.save(row);
        }
        return getByEntity(entity, authorizationHeader);
    }

    private void seedDefaults(String entity) {
        for (ImportFieldConfigDto dto : DEFAULTS.getOrDefault(entity, List.of())) {
            ImportFieldConfigEntity e = new ImportFieldConfigEntity();
            e.setEntityName(entity);
            e.setFieldKey(dto.key());
            e.setFieldName(dto.name());
            e.setAlias(dto.alias());
            e.setRequiredFlag(dto.required());
            repository.save(e);
        }
    }

    private ImportFieldConfigDto toDto(ImportFieldConfigEntity e) {
        return new ImportFieldConfigDto(e.getFieldKey(), e.getFieldName(), e.getAlias(), e.isRequiredFlag());
    }
}
