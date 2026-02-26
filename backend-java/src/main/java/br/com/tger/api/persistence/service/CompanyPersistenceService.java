package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.common.CodeNameRequestDto;
import br.com.tger.api.dto.common.CompanyResponseDto;
import br.com.tger.api.persistence.entity.CompanyEntity;
import br.com.tger.api.persistence.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyPersistenceService {
    private final CompanyRepository repository;
    public CompanyPersistenceService(CompanyRepository repository) { this.repository = repository; }

    public List<CompanyResponseDto> list() { return repository.findAll().stream().map(this::toDto).toList(); }

    public CompanyResponseDto create(CodeNameRequestDto req) {
        CompanyEntity e = new CompanyEntity();
        e.setCode("EMP-" + String.format("%04d", repository.count() + 1));
        e.setName(req.name().trim());
        e.setErpCode(req.erpCode() == null ? null : req.erpCode().trim());
        return toDto(repository.save(e));
    }

    public CompanyResponseDto update(Long id, CodeNameRequestDto req) {
        CompanyEntity e = repository.findById(id).orElseThrow();
        e.setName(req.name().trim());
        e.setErpCode(req.erpCode() == null || req.erpCode().isBlank() ? null : req.erpCode().trim());
        return toDto(repository.save(e));
    }

    public void deactivateDelete(Long id) { repository.deleteById(id); } // placeholder until soft-delete field is added

    private CompanyResponseDto toDto(CompanyEntity e) { return new CompanyResponseDto(e.getId(), e.getCode(), e.getName(), e.getErpCode()); }
}
