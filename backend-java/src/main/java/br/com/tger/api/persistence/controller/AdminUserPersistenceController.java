package br.com.tger.api.persistence.controller;

import br.com.tger.api.dto.admin.AdminUserCreateRequestDto;
import br.com.tger.api.dto.admin.AdminUserResponseDto;
import br.com.tger.api.dto.admin.UpdateUserEmailRequestDto;
import br.com.tger.api.persistence.service.AdminUserPersistenceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserPersistenceController {
    private final AdminUserPersistenceService service;
    public AdminUserPersistenceController(AdminUserPersistenceService service) { this.service = service; }

    @GetMapping public List<AdminUserResponseDto> list() { return service.list(); }
    @PostMapping public AdminUserResponseDto create(@Valid @RequestBody AdminUserCreateRequestDto req) { return service.create(req); }
    @PatchMapping("/{id}/email") public void updateEmail(@PathVariable Long id, @Valid @RequestBody UpdateUserEmailRequestDto req) { service.updateEmail(id, req.email()); }
    @PostMapping("/{id}/reset-password") public void resetPassword(@PathVariable Long id) { service.resetPassword(id); }
    @PostMapping("/{id}/deactivate") public void deactivate(@PathVariable Long id) { service.deactivate(id); }
    @PostMapping("/{id}/reactivate") public void reactivate(@PathVariable Long id) { service.reactivate(id); }
}
