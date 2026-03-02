package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.common.CodeNameRequestDto;
import br.com.tger.api.dto.common.CompanyResponseDto;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.persistence.entity.CompanyEntity;
import br.com.tger.api.persistence.repository.CompanyRepository;
import br.com.tger.api.service.AccessControlService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyPersistenceService {
    private final CompanyRepository repository;
    private final AccessControlService accessControlService;
    public CompanyPersistenceService(CompanyRepository repository, AccessControlService accessControlService) {
        this.repository = repository;
        this.accessControlService = accessControlService;
    }

    public List<CompanyResponseDto> list(String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (accessControlService.isOperator(user)) return List.of();
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public CompanyResponseDto create(CodeNameRequestDto req, String authorizationHeader) {
        accessControlService.assertNotOperator(accessControlService.requireUser(authorizationHeader));
        CompanyEntity e = new CompanyEntity();
        e.setCode("EMP-" + String.format("%04d", repository.count() + 1));
        e.setName(req.name().trim());
        e.setErpCode(req.erpCode() == null ? null : req.erpCode().trim());
        return toDto(repository.save(e));
    }

    public CompanyResponseDto update(Long id, CodeNameRequestDto req, String authorizationHeader) {
        accessControlService.assertNotOperator(accessControlService.requireUser(authorizationHeader));
        CompanyEntity e = repository.findById(id).orElseThrow();
        e.setName(req.name().trim());
        e.setErpCode(req.erpCode() == null || req.erpCode().isBlank() ? null : req.erpCode().trim());
        return toDto(repository.save(e));
    }

    public void deactivateDelete(Long id, String authorizationHeader) {
        accessControlService.assertNotOperator(accessControlService.requireUser(authorizationHeader));
        repository.deleteById(id);
    } // placeholder until soft-delete field is added

    private CompanyResponseDto toDto(CompanyEntity e) { return new CompanyResponseDto(e.getId(), e.getCode(), e.getName(), e.getErpCode()); }
}
