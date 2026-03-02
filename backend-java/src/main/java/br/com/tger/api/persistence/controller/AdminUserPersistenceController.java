package br.com.tger.api.persistence.controller;

import br.com.tger.api.dto.admin.AdminUserCreateRequestDto;
import br.com.tger.api.dto.admin.AdminUserResponseDto;
import br.com.tger.api.dto.admin.UpdateUserPermissionsRequestDto;
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

    @GetMapping
    public List<AdminUserResponseDto> list(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.list(authorizationHeader);
    }
    @PostMapping
    public AdminUserResponseDto create(
            @Valid @RequestBody AdminUserCreateRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.create(req, authorizationHeader);
    }
    @PatchMapping("/{id}/email")
    public void updateEmail(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserEmailRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        service.updateEmail(id, req.email(), authorizationHeader);
    }
    @PatchMapping("/{id}/permissions")
    public AdminUserResponseDto updatePermissions(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserPermissionsRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.updatePermissions(id, req, authorizationHeader);
    }
    @PostMapping("/{id}/reset-password")
    public void resetPassword(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        service.resetPassword(id, authorizationHeader);
    }
    @PostMapping("/{id}/deactivate")
    public void deactivate(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        service.deactivate(id, authorizationHeader);
    }
    @PostMapping("/{id}/reactivate")
    public void reactivate(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        service.reactivate(id, authorizationHeader);
    }
}
