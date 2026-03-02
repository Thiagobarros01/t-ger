package br.com.tger.api.persistence.controller;

import br.com.tger.api.dto.TiAssetDto;
import br.com.tger.api.dto.TiTermDto;
import br.com.tger.api.dto.TicketDto;
import br.com.tger.api.dto.ti.TiAssetRequestDto;
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
    public List<TiAssetDto> assets(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.listAssets(authorizationHeader);
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

    @GetMapping("/terms-contracts")
    public List<TiTermDto> terms(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.listTerms(authorizationHeader);
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
    @DeleteMapping("/terms-contracts/{id}")
    public void deleteTerm(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        service.deleteTerm(id, authorizationHeader);
    }

    @GetMapping("/tickets")
    public List<TicketDto> tickets(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        return service.listTickets(authorizationHeader);
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
