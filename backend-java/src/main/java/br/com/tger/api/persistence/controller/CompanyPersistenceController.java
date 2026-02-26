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
    @GetMapping public List<CompanyResponseDto> list() { return service.list(); }
    @PostMapping public CompanyResponseDto create(@Valid @RequestBody CodeNameRequestDto req) { return service.create(req); }
    @PutMapping("/{id}") public CompanyResponseDto update(@PathVariable Long id, @Valid @RequestBody CodeNameRequestDto req) { return service.update(id, req); }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id) { service.deactivateDelete(id); }
}
