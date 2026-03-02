package br.com.tger.api.persistence.controller;

import br.com.tger.api.dto.common.CodeNameRequestDto;
import br.com.tger.api.dto.common.CompanyResponseDto;
import br.com.tger.api.persistence.service.CompanyPersistenceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/config/companies")
public class CompanyPersistenceController {
    private final CompanyPersistenceService service;
    public CompanyPersistenceController(CompanyPersistenceService service) { this.service = service; }
    @GetMapping
    public List<CompanyResponseDto> list(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.list(authorizationHeader);
    }
    @PostMapping
    public CompanyResponseDto create(
            @Valid @RequestBody CodeNameRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.create(req, authorizationHeader);
    }
    @PutMapping("/{id}")
    public CompanyResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody CodeNameRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.update(id, req, authorizationHeader);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        service.deactivateDelete(id, authorizationHeader);
    }
}
