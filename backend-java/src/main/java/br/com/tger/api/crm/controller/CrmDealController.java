package br.com.tger.api.crm.controller;

import br.com.tger.api.crm.application.service.CrmDealService;
import br.com.tger.api.crm.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crm/deals")
public class CrmDealController {
    private final CrmDealService service;

    public CrmDealController(CrmDealService service) {
        this.service = service;
    }

    @GetMapping
    public List<DealResponseDto> list(@RequestHeader(name = "Authorization", required = false) String authorizationHeader) {
        return service.listDeals(authorizationHeader);
    }

    @PostMapping
    public DealResponseDto create(@Valid @RequestBody CreateDealRequestDto request) {
        return service.createDeal(request);
    }

    @PatchMapping("/{dealId}")
    public DealResponseDto update(@PathVariable Long dealId, @Valid @RequestBody UpdateDealRequestDto request) {
        return service.updateDeal(dealId, request);
    }

    @PatchMapping("/{dealId}/stage")
    public DealResponseDto moveStage(@PathVariable Long dealId, @Valid @RequestBody MoveDealStageRequestDto request) {
        return service.moveStage(dealId, request);
    }

    @PostMapping("/{dealId}/close-won")
    public DealResponseDto closeAsWon(@PathVariable Long dealId) {
        return service.closeAsWon(dealId);
    }

    @PostMapping("/{dealId}/close-lost")
    public DealResponseDto closeAsLost(@PathVariable Long dealId, @Valid @RequestBody CloseDealLostRequestDto request) {
        return service.closeAsLost(dealId, request);
    }
}
