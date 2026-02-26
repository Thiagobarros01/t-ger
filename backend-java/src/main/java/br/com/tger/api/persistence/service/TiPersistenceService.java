package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.TiAssetDto;
import br.com.tger.api.dto.TiTermDto;
import br.com.tger.api.dto.TicketDto;
import br.com.tger.api.dto.TicketMessageDto;
import br.com.tger.api.dto.ti.TiAssetRequestDto;
import br.com.tger.api.dto.ti.TiTermRequestDto;
import br.com.tger.api.dto.ti.TicketMessageRequestDto;
import br.com.tger.api.model.IpMode;
import br.com.tger.api.persistence.entity.TiAssetEntity;
import br.com.tger.api.persistence.entity.TiTermEntity;
import br.com.tger.api.persistence.entity.TiTicketEntity;
import br.com.tger.api.persistence.entity.TiTicketMessageEntity;
import br.com.tger.api.persistence.repository.TiAssetRepository;
import br.com.tger.api.persistence.repository.TiTermRepository;
import br.com.tger.api.persistence.repository.TiTicketRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TiPersistenceService {
    private static final TypeReference<Map<String, String>> MAP_STRING_STRING = new TypeReference<>() {};

    private final TiTermRepository termRepository;
    private final TiAssetRepository assetRepository;
    private final TiTicketRepository ticketRepository;
    private final ObjectMapper objectMapper;

    public TiPersistenceService(
            TiTermRepository termRepository,
            TiAssetRepository assetRepository,
            TiTicketRepository ticketRepository,
            ObjectMapper objectMapper
    ) {
        this.termRepository = termRepository;
        this.assetRepository = assetRepository;
        this.ticketRepository = ticketRepository;
        this.objectMapper = objectMapper;
    }

    public List<TiTermDto> listTerms() {
        return termRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<TiAssetDto> listAssets() {
        return assetRepository.findAll().stream().map(this::toDto).toList();
    }

    public List<TicketDto> listTickets() {
        return ticketRepository.findAll().stream().map(this::toDto).toList();
    }

    public TiTermDto createTerm(TiTermRequestDto req) {
        TiTermEntity e = new TiTermEntity();
        applyTerm(req, e);
        return toDto(termRepository.save(e));
    }

    public TiTermDto updateTerm(Long id, TiTermRequestDto req) {
        TiTermEntity e = termRepository.findById(id).orElseThrow();
        applyTerm(req, e);
        return toDto(termRepository.save(e));
    }

    public void deleteTerm(Long id) {
        termRepository.deleteById(id);
    }

    @Transactional
    public TiAssetDto createAsset(TiAssetRequestDto req) {
        TiAssetEntity e = new TiAssetEntity();
        e.setInternalCode(nextAssetCode());
        applyAsset(req, e);
        return toDto(assetRepository.save(e));
    }

    @Transactional
    public TiAssetDto updateAsset(Long id, TiAssetRequestDto req) {
        TiAssetEntity e = assetRepository.findById(id).orElseThrow();
        applyAsset(req, e);
        return toDto(assetRepository.save(e));
    }

    public void deleteAsset(Long id) {
        assetRepository.deleteById(id);
    }

    private void applyAsset(TiAssetRequestDto req, TiAssetEntity e) {
        e.setCompany(trim(req.company()));
        e.setCompanyErpCode(trim(req.companyErpCode()));
        e.setAssetType(req.assetType());
        e.setDepartment(trim(req.department()));
        e.setBrand(trim(req.brand()));
        e.setModel(trim(req.model()));
        e.setSerialNumber(trim(req.serialNumber()));
        e.setPatrimony(trim(req.patrimony()));
        e.setDetailedDescription(trim(req.detailedDescription()));
        e.setStatus(req.status());
        e.setResponsibleUserId(req.responsibleUserId());
        e.setResponsibleUserName(trim(req.responsibleUserName()));
        e.setLinkedTermId(req.linkedTermId());
        e.setLinkedTermTitle(trim(req.linkedTermTitle()));
        e.setTransferHistoryText(String.join("\n", req.transferHistory() == null ? List.of() : req.transferHistory().stream().map(String::trim).filter(s -> !s.isBlank()).toList()));
        e.setIpMode(req.ipMode() == null ? IpMode.DHCP : req.ipMode());
        e.setIpAddress(trim(req.ipAddress()));
        e.setImei(trim(req.imei()));
        e.setExtraFieldsJson(writeJson(req.extraFields() == null ? Map.of() : req.extraFields()));
    }

    private void applyTerm(TiTermRequestDto req, TiTermEntity e) {
        e.setType(req.type());
        e.setDefaultTermName("Termo de Responsabilidade");
        e.setLinkedUserName(req.linkedUserName().trim());
        e.setStartDate(req.startDate() == null || req.startDate().isBlank() ? LocalDate.now().toString() : req.startDate());
        e.setStatus(req.status().trim());
        e.setDocumentPath(trim(req.documentPath()));
    }

    @Transactional
    public TicketDto addTicketMessage(Long ticketId, TicketMessageRequestDto req) {
        TiTicketEntity ticket = ticketRepository.findById(ticketId).orElseThrow();
        TiTicketMessageEntity msg = new TiTicketMessageEntity();
        msg.setTicket(ticket);
        msg.setAuthor(req.author().trim());
        msg.setSentAt(req.sentAt() == null || req.sentAt().isBlank() ? java.time.LocalDateTime.now().toString() : req.sentAt());
        msg.setMessage(req.message().trim());
        ticket.getMessages().add(msg);
        return toDto(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDto updateTicketStatus(Long ticketId, String status) {
        TiTicketEntity ticket = ticketRepository.findById(ticketId).orElseThrow();
        ticket.setStatus(status == null || status.isBlank() ? ticket.getStatus() : status.trim());
        return toDto(ticketRepository.save(ticket));
    }

    private String nextAssetCode() {
        long next = assetRepository.findTopByOrderByIdDesc()
                .map(TiAssetEntity::getInternalCode)
                .map(this::extractAssetSeq)
                .orElse(0L) + 1L;
        return "TI-" + String.format("%04d", next);
    }

    private long extractAssetSeq(String code) {
        if (code == null) return 0L;
        String digits = code.replaceAll("\\D+", "");
        if (digits.isBlank()) return 0L;
        try { return Long.parseLong(digits); } catch (NumberFormatException ex) { return 0L; }
    }

    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String writeJson(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception ex) {
            return "{}";
        }
    }

    private Map<String, String> readJson(String raw) {
        if (raw == null || raw.isBlank()) return Map.of();
        try {
            Map<String, String> parsed = objectMapper.readValue(raw, MAP_STRING_STRING);
            return parsed == null ? Map.of() : parsed;
        } catch (Exception ex) {
            return Map.of();
        }
    }

    private TiTermDto toDto(TiTermEntity e) {
        return new TiTermDto(e.getId(), e.getType(), e.getDefaultTermName(), e.getLinkedUserName(), e.getStartDate(), e.getStatus(), e.getDocumentPath());
    }

    private TiAssetDto toDto(TiAssetEntity e) {
        List<String> history = e.getTransferHistoryText() == null || e.getTransferHistoryText().isBlank()
                ? List.of()
                : java.util.Arrays.stream(e.getTransferHistoryText().split("\\R")).map(String::trim).filter(s -> !s.isBlank()).toList();
        Map<String, String> extra = new LinkedHashMap<>(readJson(e.getExtraFieldsJson()));
        return new TiAssetDto(
                e.getId(),
                e.getInternalCode(),
                e.getCompany(),
                e.getCompanyErpCode(),
                e.getAssetType(),
                e.getDepartment(),
                e.getBrand(),
                e.getModel(),
                e.getSerialNumber(),
                e.getPatrimony(),
                e.getDetailedDescription(),
                e.getStatus(),
                e.getResponsibleUserId(),
                e.getResponsibleUserName(),
                e.getLinkedTermId(),
                e.getLinkedTermTitle(),
                history,
                e.getIpMode(),
                e.getIpAddress(),
                e.getImei(),
                extra
        );
    }

    private TicketDto toDto(TiTicketEntity e) {
        List<TicketMessageDto> messages = e.getMessages().stream()
                .map(m -> new TicketMessageDto(m.getId(), m.getAuthor(), m.getSentAt(), m.getMessage(), false))
                .toList();
        return new TicketDto(e.getId(), e.getSubject(), e.getRequester(), e.getAssignedTo(), e.getPriority(), e.getStatus(), messages);
    }
}
