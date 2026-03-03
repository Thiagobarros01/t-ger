package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.common.CodeNameRequestDto;
import br.com.tger.api.dto.common.CompanyResponseDto;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.dto.common.PagedResponseDto;
import br.com.tger.api.persistence.entity.CompanyEntity;
import br.com.tger.api.persistence.repository.CompanyRepository;
import br.com.tger.api.service.AccessControlService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public PagedResponseDto<CompanyResponseDto> search(String authorizationHeader, String name, String erpCode, Integer page, Integer pageSize) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (accessControlService.isOperator(user)) return new PagedResponseDto<>(List.of(), 1, 10, 0, 0);

        int safePage = Math.max(1, page == null ? 1 : page);
        int safePageSize = normalizePageSize(pageSize);
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "id"));

        Specification<CompanyEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String byName = trim(name);
            String byErp = trim(erpCode);
            if (byName != null) predicates.add(cb.like(cb.lower(root.get("name")), "%" + byName.toLowerCase() + "%"));
            if (byErp != null) predicates.add(cb.like(cb.lower(root.get("erpCode")), "%" + byErp.toLowerCase() + "%"));
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<CompanyEntity> result = repository.findAll(spec, pageable);
        return new PagedResponseDto<>(
                result.getContent().stream().map(this::toDto).toList(),
                safePage,
                safePageSize,
                result.getTotalElements(),
                result.getTotalPages()
        );
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

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) return 10;
        return Math.min(pageSize, 100);
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
