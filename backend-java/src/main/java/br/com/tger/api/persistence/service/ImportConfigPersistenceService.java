package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.imports.ImportFieldConfigDto;
import br.com.tger.api.persistence.entity.ImportFieldConfigEntity;
import br.com.tger.api.persistence.repository.ImportFieldConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ImportConfigPersistenceService {
    private final ImportFieldConfigRepository repository;
    public ImportConfigPersistenceService(ImportFieldConfigRepository repository) { this.repository = repository; }

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
            )
    );

    @Transactional
    public List<ImportFieldConfigDto> getByEntity(String entity) {
        var rows = repository.findByEntityNameOrderByIdAsc(entity);
        if (rows.isEmpty()) {
            seedDefaults(entity);
            rows = repository.findByEntityNameOrderByIdAsc(entity);
        }
        return rows.stream().map(this::toDto).toList();
    }

    @Transactional
    public List<ImportFieldConfigDto> saveAll(String entity, List<ImportFieldConfigDto> configs) {
        for (ImportFieldConfigDto dto : configs) {
            ImportFieldConfigEntity row = repository.findByEntityNameAndFieldKey(entity, dto.key()).orElseGet(ImportFieldConfigEntity::new);
            row.setEntityName(entity);
            row.setFieldKey(dto.key());
            row.setFieldName(dto.name());
            row.setAlias(dto.alias());
            row.setRequiredFlag(dto.required());
            repository.save(row);
        }
        return getByEntity(entity);
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
