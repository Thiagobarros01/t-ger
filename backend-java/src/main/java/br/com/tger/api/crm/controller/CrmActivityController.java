package br.com.tger.api.crm.controller;

import br.com.tger.api.crm.application.service.CrmActivityService;
import br.com.tger.api.crm.dto.CreateInteractionRequestDto;
import br.com.tger.api.crm.dto.CreateTaskRequestDto;
import br.com.tger.api.crm.dto.InteractionResponseDto;
import br.com.tger.api.crm.dto.TaskResponseDto;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crm")
public class CrmActivityController {
    private final CrmActivityService service;

    public CrmActivityController(CrmActivityService service) {
        this.service = service;
    }

    @GetMapping("/interactions")
    public List<InteractionResponseDto> listInteractions(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        return service.listInteractions(authorizationHeader);
    }

    @PostMapping("/interactions")
    public InteractionResponseDto createInteraction(@Valid @RequestBody CreateInteractionRequestDto request) {
        return service.createInteraction(request);
    }

    @GetMapping("/tasks")
    public List<TaskResponseDto> listTasks(
            @RequestHeader(name = "Authorization", required = false) String authorizationHeader
    ) {
        return service.listTasks(authorizationHeader);
    }

    @PostMapping("/tasks")
    public TaskResponseDto createTask(@Valid @RequestBody CreateTaskRequestDto request) {
        return service.createTask(request);
    }
}
