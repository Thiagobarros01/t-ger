package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.UserDto;
import br.com.tger.api.dto.common.GlobalParameterDto;
import br.com.tger.api.persistence.entity.GlobalParameterEntity;
import br.com.tger.api.persistence.repository.GlobalParameterRepository;
import br.com.tger.api.service.AccessControlService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GlobalParameterPersistenceService {
    public static final String KEY_RECOMPRA_DIAS = "recompra_dias_padrao";
    public static final String KEY_RECOMPRA_VALOR = "recompra_valor_padrao";
    public static final String KEY_RESGATE_DIAS = "resgate_dias_padrao";
    public static final String KEY_RESGATE_VALOR = "resgate_valor_padrao";

    private final GlobalParameterRepository repository;
    private final AccessControlService accessControlService;

    public GlobalParameterPersistenceService(GlobalParameterRepository repository, AccessControlService accessControlService) {
        this.repository = repository;
        this.accessControlService = accessControlService;
    }

    @Transactional
    public List<GlobalParameterDto> list(String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        accessControlService.assertNotOperator(user);
        ensureDefaults();
        return repository.findAll(Sort.by("id").ascending()).stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public List<GlobalParameterDto> save(List<GlobalParameterDto> payload, String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        accessControlService.assertNotOperator(user);
        ensureDefaults();
        Map<String, GlobalParameterDto> byKey = new LinkedHashMap<>();
        for (GlobalParameterDto dto : payload) {
            if (dto == null || dto.key() == null) continue;
            byKey.put(dto.key(), dto);
        }
        for (String key : allowedKeys().keySet()) {
            GlobalParameterDto input = byKey.get(key);
            if (input == null) continue;
            GlobalParameterEntity row = repository.findByParamKey(key).orElseThrow();
            row.setValue(input.value() == null || input.value().isBlank() ? defaultValueFor(key) : input.value().trim());
            repository.save(row);
        }
        return list(authorizationHeader);
    }

    @Transactional
    public void ensureDefaults() {
        Map<String, String> labels = allowedKeys();
        for (String key : labels.keySet()) {
            repository.findByParamKey(key).orElseGet(() -> {
                GlobalParameterEntity e = new GlobalParameterEntity();
                e.setParamKey(key);
                e.setLabel(labels.get(key));
                e.setDescription(descriptionFor(key));
                e.setValue(defaultValueFor(key));
                return repository.save(e);
            });
        }
    }

    private Map<String, String> allowedKeys() {
        return Map.of(
                KEY_RECOMPRA_DIAS, "Recompra (dias)",
                KEY_RECOMPRA_VALOR, "Recompra (valor)",
                KEY_RESGATE_DIAS, "Resgate (dias)",
                KEY_RESGATE_VALOR, "Resgate (valor)"
        );
    }

    private String descriptionFor(String key) {
        return switch (key) {
            case KEY_RECOMPRA_DIAS -> "Dias para gerar oportunidade de recompra apos ganho.";
            case KEY_RECOMPRA_VALOR -> "Valor estimado padrao para oportunidade de recompra.";
            case KEY_RESGATE_DIAS -> "Dias referencia para oportunidades de resgate.";
            case KEY_RESGATE_VALOR -> "Valor estimado padrao para oportunidade de resgate.";
            default -> "";
        };
    }

    private String defaultValueFor(String key) {
        return switch (key) {
            case KEY_RECOMPRA_DIAS -> "30";
            case KEY_RECOMPRA_VALOR -> "500.00";
            case KEY_RESGATE_DIAS -> "60";
            case KEY_RESGATE_VALOR -> "300.00";
            default -> "";
        };
    }

    private GlobalParameterDto toDto(GlobalParameterEntity entity) {
        return new GlobalParameterDto(
                entity.getParamKey(),
                entity.getLabel(),
                entity.getDescription(),
                entity.getValue()
        );
    }
}
