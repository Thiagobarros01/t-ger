package br.com.tger.api.persistence.controller;

import br.com.tger.api.dto.TiAssetDto;
import br.com.tger.api.dto.TiAssetHistoryDto;
import br.com.tger.api.dto.TiAssetSummaryDto;
import br.com.tger.api.dto.TiTermDto;
import br.com.tger.api.dto.TicketDto;
import br.com.tger.api.dto.common.PagedResponseDto;
import br.com.tger.api.dto.ti.TiAssetRequestDto;
import br.com.tger.api.dto.ti.TiAssetReturnRequestDto;
import br.com.tger.api.dto.ti.TiTermRequestDto;
import br.com.tger.api.dto.ti.TicketMessageRequestDto;
import br.com.tger.api.dto.ti.TicketStatusUpdateRequestDto;
import br.com.tger.api.persistence.service.TiPersistenceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ti")
public class TiPersistenceController {
    private final TiPersistenceService service;

    public TiPersistenceController(TiPersistenceService service) {
        this.service = service;
    }

    @GetMapping("/assets")
    public List<TiAssetDto> assets(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false, defaultValue = "false") boolean showInactives
    ) {
        return service.listAssets(authorizationHeader, showInactives);
    }
    @GetMapping("/assets/summary")
    public TiAssetSummaryDto assetSummary(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.getAssetSummary(authorizationHeader);
    }
    @GetMapping("/assets/paged")
    public PagedResponseDto<TiAssetDto> assetsPaged(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String internalCode,
            @RequestParam(required = false) String assetType,
            @RequestParam(required = false) String responsible,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String company,
            @RequestParam(required = false, defaultValue = "false") boolean showInactives,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        return service.searchAssets(authorizationHeader, internalCode, assetType, responsible, department, company, showInactives, page, pageSize);
    }

    @PostMapping("/assets")
    public TiAssetDto createAsset(
            @Valid @RequestBody TiAssetRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.createAsset(req, authorizationHeader);
    }
    @PutMapping("/assets/{id}")
    public TiAssetDto updateAsset(
            @PathVariable Long id,
            @Valid @RequestBody TiAssetRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.updateAsset(id, req, authorizationHeader);
    }
    @DeleteMapping("/assets/{id}")
    public void deleteAsset(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        service.deleteAsset(id, authorizationHeader);
    }
    @PatchMapping("/assets/{id}/return")
    public TiAssetDto returnAsset(
            @PathVariable Long id,
            @Valid @RequestBody TiAssetReturnRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.returnAsset(id, req.equipmentCondition(), authorizationHeader);
    }
    @PatchMapping("/assets/{id}/inactivate")
    public TiAssetDto inactivateAsset(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.inactivateAsset(id, authorizationHeader);
    }
    @PatchMapping("/assets/{id}/reactivate")
    public TiAssetDto reactivateAsset(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.reactivateAsset(id, authorizationHeader);
    }
    @GetMapping("/assets/{id}/history")
    public PagedResponseDto<TiAssetHistoryDto> assetHistory(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String responsible,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        return service.searchAssetHistory(authorizationHeader, id, responsible, status, eventType, page, pageSize);
    }

    @GetMapping("/terms-contracts")
    public List<TiTermDto> terms(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false, defaultValue = "false") boolean showInactives
    ) {
        return service.listTerms(authorizationHeader, showInactives);
    }
    @GetMapping("/terms-contracts/paged")
    public PagedResponseDto<TiTermDto> termsPaged(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String linkedUserName,
            @RequestParam(required = false) Long linkedAssetId,
            @RequestParam(required = false) String linkedItemDescription,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String documentPath,
            @RequestParam(required = false, defaultValue = "false") boolean showInactives,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        return service.searchTerms(authorizationHeader, type, linkedUserName, linkedAssetId, linkedItemDescription, status, documentPath, showInactives, page, pageSize);
    }

    @PostMapping("/terms-contracts")
    public TiTermDto createTerm(
            @Valid @RequestBody TiTermRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.createTerm(req, authorizationHeader);
    }
    @PutMapping("/terms-contracts/{id}")
    public TiTermDto updateTerm(
            @PathVariable Long id,
            @Valid @RequestBody TiTermRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.updateTerm(id, req, authorizationHeader);
    }
    @PatchMapping("/terms-contracts/{id}/inactivate")
    public TiTermDto inactivateTerm(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.inactivateTerm(id, authorizationHeader);
    }
    @DeleteMapping("/terms-contracts/{id}")
    public void deleteTerm(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        service.deleteTerm(id, authorizationHeader);
    }

    @GetMapping("/tickets")
    public List<TicketDto> tickets(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.listTickets(authorizationHeader);
    }
    @GetMapping("/tickets/paged")
    public PagedResponseDto<TicketDto> ticketsPaged(
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assignedTo,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize
    ) {
        return service.searchTickets(authorizationHeader, subject, status, assignedTo, page, pageSize);
    }

    @PostMapping("/tickets/{ticketId}/messages")
    public TicketDto addMessage(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketMessageRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.addTicketMessage(ticketId, req, authorizationHeader);
    }

    @PatchMapping("/tickets/{ticketId}/status")
    public TicketDto updateStatus(
            @PathVariable Long ticketId,
            @Valid @RequestBody TicketStatusUpdateRequestDto req,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        return service.updateTicketStatus(ticketId, req.status(), authorizationHeader);
    }
}
