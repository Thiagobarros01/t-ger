package br.com.tger.api.controller;

import br.com.tger.api.dto.ModuleDto;
import br.com.tger.api.model.UserProfile;
import br.com.tger.api.service.SampleDataService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meta")
public class MetaController {

    private final SampleDataService sampleDataService;

    public MetaController(SampleDataService sampleDataService) {
        this.sampleDataService = sampleDataService;
    }

    @GetMapping("/modules")
    public List<ModuleDto> modules() {
        return sampleDataService.getModules();
    }

    @GetMapping("/profiles")
    public List<UserProfile> profiles() {
        return sampleDataService.getProfiles();
    }
}
