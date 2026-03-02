package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.admin.AdminUserCreateRequestDto;
import br.com.tger.api.dto.admin.AdminUserResponseDto;
import br.com.tger.api.dto.admin.UpdateUserPermissionsRequestDto;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.persistence.entity.AppUserEntity;
import br.com.tger.api.persistence.repository.AppUserRepository;
import br.com.tger.api.service.AccessControlService;
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
}
