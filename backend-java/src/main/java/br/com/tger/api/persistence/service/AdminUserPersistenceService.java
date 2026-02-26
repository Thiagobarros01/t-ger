package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.admin.AdminUserCreateRequestDto;
import br.com.tger.api.dto.admin.AdminUserResponseDto;
import br.com.tger.api.persistence.entity.AppUserEntity;
import br.com.tger.api.persistence.repository.AppUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class AdminUserPersistenceService {
    private final AppUserRepository repository;
    public AdminUserPersistenceService(AppUserRepository repository) { this.repository = repository; }

    public List<AdminUserResponseDto> list() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public AdminUserResponseDto create(AdminUserCreateRequestDto request) {
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
    public void updateEmail(Long id, String email) {
        AppUserEntity user = repository.findById(id).orElseThrow();
        user.setEmail(email.trim());
    }

    @Transactional
    public void resetPassword(Long id) {
        AppUserEntity user = repository.findById(id).orElseThrow();
        user.setLastPasswordResetAt(Instant.now());
    }

    @Transactional
    public void deactivate(Long id) {
        AppUserEntity user = repository.findById(id).orElseThrow();
        user.setActive(false);
    }

    @Transactional
    public void reactivate(Long id) {
        AppUserEntity user = repository.findById(id).orElseThrow();
        user.setActive(true);
    }

    public AdminUserResponseDto toDto(AppUserEntity e) {
        return new AdminUserResponseDto(e.getId(), e.getName(), e.getEmail(), e.getErpCode(), e.getProfile(), e.isActive(), e.getLastPasswordResetAt(), e.getModules());
    }
}
