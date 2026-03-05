package br.com.tger.api.persistence.controller;

import br.com.tger.api.dto.common.GlobalParameterDto;
import br.com.tger.api.persistence.service.GlobalParameterPersistenceService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/config/global-params")
public class GlobalParameterPersistenceController {
    private final GlobalParameterPersistenceService service;

    public GlobalParameterPersistenceController(GlobalParameterPersistenceService service) {
        this.service = service;
    }

    @GetMapping
    public List<GlobalParameterDto> list(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.list(authorizationHeader);
    }

    @PutMapping
    public List<GlobalParameterDto> save(
            @RequestBody List<GlobalParameterDto> payload,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.save(payload, authorizationHeader);
    }
}

