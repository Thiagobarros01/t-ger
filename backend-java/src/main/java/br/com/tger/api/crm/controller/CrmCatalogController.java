package br.com.tger.api.crm.controller;

import br.com.tger.api.crm.application.service.CrmCatalogService;
import br.com.tger.api.crm.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crm/catalog")
public class CrmCatalogController {
    private final CrmCatalogService service;

    public CrmCatalogController(CrmCatalogService service) {
        this.service = service;
    }

    @GetMapping("/pipelines")
    public List<PipelineResponseDto> listPipelines() {
        return service.listPipelines();
    }

    @PostMapping("/pipelines")
    public PipelineResponseDto createPipeline(@Valid @RequestBody CreatePipelineRequestDto request) {
        return service.createPipeline(request);
    }

    @GetMapping("/pipelines/{pipelineId}/stages")
    public List<StageResponseDto> listStages(@PathVariable Long pipelineId) {
        return service.listStagesByPipeline(pipelineId);
    }

    @PostMapping("/stages")
    public StageResponseDto createStage(@Valid @RequestBody CreateStageRequestDto request) {
        return service.createStage(request);
    }

    @PutMapping("/stages/{stageId}")
    public StageResponseDto updateStage(@PathVariable Long stageId, @Valid @RequestBody UpdateStageRequestDto request) {
        return service.updateStage(stageId, request);
    }

    @DeleteMapping("/stages/{stageId}")
    public void deleteStage(@PathVariable Long stageId) {
        service.deleteStage(stageId);
    }

    @GetMapping("/loss-reasons")
    public List<LossReasonResponseDto> listLossReasons() {
        return service.listLossReasons();
    }

    @PostMapping("/loss-reasons")
    public LossReasonResponseDto createLossReason(@Valid @RequestBody CreateLossReasonRequestDto request) {
        return service.createLossReason(request);
    }
}
