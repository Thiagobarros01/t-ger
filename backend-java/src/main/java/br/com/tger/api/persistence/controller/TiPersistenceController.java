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
    public List<TiAssetDto> assets() { return service.listAssets(); }

    @PostMapping("/assets")
    public TiAssetDto createAsset(@Valid @RequestBody TiAssetRequestDto req) { return service.createAsset(req); }
    @PutMapping("/assets/{id}")
    public TiAssetDto updateAsset(@PathVariable Long id, @Valid @RequestBody TiAssetRequestDto req) { return service.updateAsset(id, req); }
    @DeleteMapping("/assets/{id}")
    public void deleteAsset(@PathVariable Long id) { service.deleteAsset(id); }

    @GetMapping("/terms-contracts")
    public List<TiTermDto> terms() { return service.listTerms(); }

    @PostMapping("/terms-contracts")
    public TiTermDto createTerm(@Valid @RequestBody TiTermRequestDto req) { return service.createTerm(req); }
    @PutMapping("/terms-contracts/{id}")
    public TiTermDto updateTerm(@PathVariable Long id, @Valid @RequestBody TiTermRequestDto req) { return service.updateTerm(id, req); }
    @DeleteMapping("/terms-contracts/{id}")
    public void deleteTerm(@PathVariable Long id) { service.deleteTerm(id); }

    @GetMapping("/tickets")
    public List<TicketDto> tickets() { return service.listTickets(); }

    @PostMapping("/tickets/{ticketId}/messages")
    public TicketDto addMessage(@PathVariable Long ticketId, @Valid @RequestBody TicketMessageRequestDto req) {
        return service.addTicketMessage(ticketId, req);
    }

    @PatchMapping("/tickets/{ticketId}/status")
    public TicketDto updateStatus(@PathVariable Long ticketId, @Valid @RequestBody TicketStatusUpdateRequestDto req) {
        return service.updateTicketStatus(ticketId, req.status());
    }
}
