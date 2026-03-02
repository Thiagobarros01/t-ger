package br.com.tger.api.persistence.controller;

import br.com.tger.api.dto.imports.ImportFieldConfigDto;
import br.com.tger.api.persistence.service.ImportConfigPersistenceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/config/import-layouts")
public class ImportConfigPersistenceController {
    private final ImportConfigPersistenceService service;
    public ImportConfigPersistenceController(ImportConfigPersistenceService service) { this.service = service; }

    @GetMapping("/{entity}")
    public List<ImportFieldConfigDto> get(
            @PathVariable String entity,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.getByEntity(entity, authorizationHeader);
    }

    @PutMapping("/{entity}")
    public List<ImportFieldConfigDto> save(
            @PathVariable String entity,
            @RequestBody List<ImportFieldConfigDto> body,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.saveAll(entity, body, authorizationHeader);
    }
}
