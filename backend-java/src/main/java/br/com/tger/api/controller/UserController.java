package br.com.tger.api.controller;

import br.com.tger.api.dto.UserDto;
import br.com.tger.api.persistence.repository.AppUserRepository;
import br.com.tger.api.service.SampleDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final SampleDataService sampleDataService;
    private final AppUserRepository appUserRepository;

    public UserController(SampleDataService sampleDataService, AppUserRepository appUserRepository) {
        this.sampleDataService = sampleDataService;
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public List<UserDto> list() {
        var persisted = appUserRepository.findAll().stream()
                .map(entity -> new UserDto(entity.getId(), entity.getName(), entity.getEmail(), entity.getProfile(), entity.getModules()))
                .toList();
        return persisted.isEmpty() ? sampleDataService.getUsers() : persisted;
    }
}
