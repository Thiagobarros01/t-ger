package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.admin.AdminUserCreateRequestDto;
import br.com.tger.api.dto.admin.AdminUserResponseDto;
import br.com.tger.api.dto.admin.UpdateUserPermissionsRequestDto;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.dto.common.PagedResponseDto;
import br.com.tger.api.persistence.entity.AppUserEntity;
import br.com.tger.api.persistence.repository.AppUserRepository;
import br.com.tger.api.service.AccessControlService;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUserPersistenceService {
    private final AppUserRepository repository;
    private final AccessControlService accessControlService;
    public AdminUserPersistenceService(AppUserRepository repository, AccessControlService accessControlService) {
        this.repository = repository;
        this.accessControlService = accessControlService;
    }

    public List<AdminUserResponseDto> list(String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (!accessControlService.isOperator(user)) {
            return repository.findAll().stream().map(this::toDto).toList();
        }
        return repository.findById(user.id()).stream().map(this::toDto).toList();
    }

    public PagedResponseDto<AdminUserResponseDto> search(
            String authorizationHeader,
            String name,
            String email,
            String profile,
            Boolean active,
            Integer page,
            Integer pageSize
    ) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        int safePage = Math.max(1, page == null ? 1 : page);
        int safePageSize = normalizePageSize(pageSize);
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "id"));

        Specification<AppUserEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String byName = trim(name);
            String byEmail = trim(email);
            String byProfile = trim(profile);

            if (byName != null) predicates.add(cb.like(cb.lower(root.get("name")), "%" + byName.toLowerCase() + "%"));
            if (byEmail != null) predicates.add(cb.like(cb.lower(root.get("email")), "%" + byEmail.toLowerCase() + "%"));
            if (byProfile != null) predicates.add(cb.equal(cb.upper(root.get("profile").as(String.class)), byProfile.toUpperCase()));
            if (active != null) predicates.add(cb.equal(root.get("active"), active));
            if (accessControlService.isOperator(user)) predicates.add(cb.equal(root.get("id"), user.id()));
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<AppUserEntity> result = repository.findAll(spec, pageable);
        return new PagedResponseDto<>(
                result.getContent().stream().map(this::toDto).toList(),
                safePage,
                safePageSize,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional
    public AdminUserResponseDto create(AdminUserCreateRequestDto request, String authorizationHeader) {
        accessControlService.assertNotOperator(accessControlService.requireUser(authorizationHeader));
        AppUserEntity entity = new AppUserEntity();
        entity.setName(request.name().trim());
        entity.setEmail(request.email().trim());
        entity.setErpCode(request.erpCode() == null ? null : request.erpCode().trim());
        entity.setProfile(request.profile());
        entity.setModules(request.profile().name().equals("ADMINISTRADOR") ? new ArrayList<>() : new ArrayList<>(request.modules() == null ? List.of() : request.modules()));
        entity.setActive(true);
        return toDto(repository.save(entity));
    }

    @Transactional
    public void updateEmail(Long id, String email, String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (accessControlService.isOperator(user) && !user.id().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operador pode editar apenas o proprio e-mail");
        }
        AppUserEntity appUser = repository.findById(id).orElseThrow();
        appUser.setEmail(email.trim());
    }

    @Transactional
    public AdminUserResponseDto updatePermissions(Long id, UpdateUserPermissionsRequestDto request, String authorizationHeader) {
        accessControlService.assertNotOperator(accessControlService.requireUser(authorizationHeader));
        AppUserEntity user = repository.findById(id).orElseThrow();
        user.setProfile(request.profile());
        user.setModules(
                request.profile().name().equals("ADMINISTRADOR")
                        ? new ArrayList<>()
                        : new ArrayList<>(request.modules() == null ? List.of() : request.modules())
        );
        return toDto(repository.save(user));
    }

    @Transactional
    public void resetPassword(Long id, String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (accessControlService.isOperator(user) && !user.id().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operador pode resetar apenas a propria senha");
        }
        AppUserEntity appUser = repository.findById(id).orElseThrow();
        appUser.setLastPasswordResetAt(Instant.now());
    }

    @Transactional
    public void deactivate(Long id, String authorizationHeader) {
        accessControlService.assertNotOperator(accessControlService.requireUser(authorizationHeader));
        AppUserEntity appUser = repository.findById(id).orElseThrow();
        appUser.setActive(false);
    }

    @Transactional
    public void reactivate(Long id, String authorizationHeader) {
        accessControlService.assertNotOperator(accessControlService.requireUser(authorizationHeader));
        AppUserEntity appUser = repository.findById(id).orElseThrow();
        appUser.setActive(true);
    }

    public AdminUserResponseDto toDto(AppUserEntity e) {
        return new AdminUserResponseDto(e.getId(), e.getName(), e.getEmail(), e.getErpCode(), e.getProfile(), e.isActive(), e.getLastPasswordResetAt(), e.getModules());
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) return 10;
        return Math.min(pageSize, 100);
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
