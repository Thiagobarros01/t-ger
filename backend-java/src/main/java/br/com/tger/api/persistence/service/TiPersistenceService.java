package br.com.tger.api.persistence.service;

import br.com.tger.api.dto.TiAssetDto;
import br.com.tger.api.dto.TiAssetHistoryDto;
import br.com.tger.api.dto.TiAssetSummaryDto;
import br.com.tger.api.dto.TiTermDto;
import br.com.tger.api.dto.TicketDto;
import br.com.tger.api.dto.TicketMessageDto;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.dto.common.PagedResponseDto;
import br.com.tger.api.dto.ti.TiAssetRequestDto;
import br.com.tger.api.dto.ti.TiTermRequestDto;
import br.com.tger.api.dto.ti.TicketMessageRequestDto;
import br.com.tger.api.model.AssetStatus;
import br.com.tger.api.model.EquipmentCondition;
import br.com.tger.api.model.IpMode;
import br.com.tger.api.persistence.entity.TiAssetEntity;
import br.com.tger.api.persistence.entity.TiAssetHistoryEntity;
import br.com.tger.api.persistence.entity.TiTermEntity;
import br.com.tger.api.persistence.entity.TiTicketEntity;
import br.com.tger.api.persistence.entity.TiTicketMessageEntity;
import br.com.tger.api.persistence.repository.AppUserRepository;
import br.com.tger.api.persistence.repository.TiAssetRepository;
import br.com.tger.api.persistence.repository.TiAssetHistoryRepository;
import br.com.tger.api.persistence.repository.TiTermRepository;
import br.com.tger.api.persistence.repository.TiTicketRepository;
import br.com.tger.api.service.AccessControlService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TiPersistenceService {
    private static final TypeReference<Map<String, String>> MAP_STRING_STRING = new TypeReference<>() {};

    private final TiTermRepository termRepository;
    private final TiAssetRepository assetRepository;
    private final TiAssetHistoryRepository assetHistoryRepository;
    private final AppUserRepository appUserRepository;
    private final TiTicketRepository ticketRepository;
    private final ObjectMapper objectMapper;
    private final AccessControlService accessControlService;

    public TiPersistenceService(
            TiTermRepository termRepository,
            TiAssetRepository assetRepository,
            TiAssetHistoryRepository assetHistoryRepository,
            AppUserRepository appUserRepository,
            TiTicketRepository ticketRepository,
            ObjectMapper objectMapper,
            AccessControlService accessControlService
    ) {
        this.termRepository = termRepository;
        this.assetRepository = assetRepository;
        this.assetHistoryRepository = assetHistoryRepository;
        this.appUserRepository = appUserRepository;
        this.ticketRepository = ticketRepository;
        this.objectMapper = objectMapper;
        this.accessControlService = accessControlService;
    }

    public List<TiTermDto> listTerms(String authorizationHeader, boolean showInactives) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (!accessControlService.isOperator(user)) {
            return termRepository.findAll().stream()
                    .filter(term -> showInactives || term.isActive())
                    .map(this::toDto)
                    .toList();
        }
        return termRepository.findAll().stream()
                .filter(term -> isOwnTerm(term, user))
                .filter(term -> showInactives || term.isActive())
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TiAssetDto> listAssets(String authorizationHeader, boolean showInactives) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (!accessControlService.isOperator(user)) {
            return assetRepository.findAll().stream()
                    .filter(asset -> showInactives || asset.isActive())
                    .map(this::toDto)
                    .toList();
        }
        return assetRepository.findByResponsibleUserId(user.id()).stream()
                .filter(asset -> showInactives || asset.isActive())
                .map(this::toDto)
                .toList();
    }

    public List<TicketDto> listTickets(String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        if (!accessControlService.isOperator(user)) {
            return ticketRepository.findAll().stream().map(this::toDto).toList();
        }
        return ticketRepository.findAll().stream()
                .filter(ticket -> isOwnTicket(ticket, user))
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public TiAssetSummaryDto getAssetSummary(String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        List<TiAssetEntity> assets = accessControlService.isOperator(user)
                ? assetRepository.findByResponsibleUserId(user.id())
                : assetRepository.findAll();
        assets = assets.stream().filter(TiAssetEntity::isActive).toList();

        long total = assets.size();
        long available = assets.stream().filter(asset -> asset.getStatus() == AssetStatus.DISPONIVEL).count();
        long inUse = assets.stream().filter(asset -> asset.getStatus() == AssetStatus.EM_USO).count();
        long returned = assets.stream()
                .filter(asset -> asset.getStatus() == AssetStatus.DISPONIVEL)
                .filter(asset -> assetHistoryRepository.findTopByAsset_IdOrderByChangedAtDesc(asset.getId())
                        .map(event -> "DEVOLUCAO".equalsIgnoreCase(event.getEventType()))
                        .orElse(false))
                .count();

        return new TiAssetSummaryDto(total, available, inUse, returned);
    }

    @Transactional(readOnly = true)
    public PagedResponseDto<TiAssetDto> searchAssets(
            String authorizationHeader,
            String internalCode,
            String assetType,
            String responsible,
            String department,
            String company,
            boolean showInactives,
            Integer page,
            Integer pageSize
    ) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        int safePage = Math.max(1, page == null ? 1 : page);
        int safePageSize = normalizePageSize(pageSize);
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "id"));

        Specification<TiAssetEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String byCode = trim(internalCode);
            String byType = trim(assetType);
            String byResponsible = trim(responsible);
            String byDepartment = trim(department);
            String byCompany = trim(company);

            if (byCode != null) predicates.add(cb.like(cb.lower(root.get("internalCode")), "%" + byCode.toLowerCase() + "%"));
            if (byType != null) predicates.add(cb.equal(cb.upper(root.get("assetType").as(String.class)), byType.toUpperCase()));
            if (byResponsible != null) predicates.add(cb.like(cb.lower(root.get("responsibleUserName")), "%" + byResponsible.toLowerCase() + "%"));
            if (byDepartment != null) predicates.add(cb.like(cb.lower(root.get("department")), "%" + byDepartment.toLowerCase() + "%"));
            if (byCompany != null) predicates.add(cb.like(cb.lower(root.get("company")), "%" + byCompany.toLowerCase() + "%"));
            if (!showInactives) predicates.add(cb.isTrue(root.get("active")));

            if (accessControlService.isOperator(user)) {
                predicates.add(cb.equal(root.get("responsibleUserId"), user.id()));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TiAssetEntity> result = assetRepository.findAll(spec, pageable);
        return new PagedResponseDto<>(
                result.getContent().stream().map(this::toDto).toList(),
                safePage,
                safePageSize,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponseDto<TiTermDto> searchTerms(
            String authorizationHeader,
            String type,
            String linkedUserName,
            Long linkedAssetId,
            String linkedItemDescription,
            String status,
            String documentPath,
            boolean showInactives,
            Integer page,
            Integer pageSize
    ) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        int safePage = Math.max(1, page == null ? 1 : page);
        int safePageSize = normalizePageSize(pageSize);
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "id"));

        Specification<TiTermEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String byType = trim(type);
            String byLinkedUserName = trim(linkedUserName);
            String byLinkedItemDescription = trim(linkedItemDescription);
            String byStatus = trim(status);
            String byDocumentPath = trim(documentPath);

            if (byType != null) predicates.add(cb.equal(cb.upper(root.get("type").as(String.class)), byType.toUpperCase()));
            if (byLinkedUserName != null) predicates.add(cb.like(cb.lower(root.get("linkedUserName")), "%" + byLinkedUserName.toLowerCase() + "%"));
            if (linkedAssetId != null) predicates.add(cb.equal(root.get("linkedAssetId"), linkedAssetId));
            if (byLinkedItemDescription != null) predicates.add(cb.like(cb.lower(root.get("linkedItemDescription")), "%" + byLinkedItemDescription.toLowerCase() + "%"));
            if (byStatus != null) predicates.add(cb.like(cb.lower(root.get("status")), "%" + byStatus.toLowerCase() + "%"));
            if (byDocumentPath != null) predicates.add(cb.like(cb.lower(root.get("documentPath")), "%" + byDocumentPath.toLowerCase() + "%"));
            if (!showInactives) predicates.add(cb.isTrue(root.get("active")));

            if (accessControlService.isOperator(user)) {
                Predicate ownByName = cb.equal(cb.lower(root.get("linkedUserName")), user.name().toLowerCase());
                Predicate ownByEmail = cb.equal(cb.lower(root.get("linkedUserName")), user.email().toLowerCase());
                predicates.add(cb.or(ownByName, ownByEmail));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TiTermEntity> result = termRepository.findAll(spec, pageable);
        return new PagedResponseDto<>(
                result.getContent().stream().map(this::toDto).toList(),
                safePage,
                safePageSize,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponseDto<TicketDto> searchTickets(
            String authorizationHeader,
            String subject,
            String status,
            String assignedTo,
            Integer page,
            Integer pageSize
    ) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        int safePage = Math.max(1, page == null ? 1 : page);
        int safePageSize = normalizePageSize(pageSize);
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "id"));

        Specification<TiTicketEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            String bySubject = trim(subject);
            String byStatus = trim(status);
            String byAssignedTo = trim(assignedTo);

            if (bySubject != null) predicates.add(cb.like(cb.lower(root.get("subject")), "%" + bySubject.toLowerCase() + "%"));
            if (byStatus != null) predicates.add(cb.like(cb.lower(root.get("status")), "%" + byStatus.toLowerCase() + "%"));
            if (byAssignedTo != null) predicates.add(cb.like(cb.lower(root.get("assignedTo")), "%" + byAssignedTo.toLowerCase() + "%"));

            if (accessControlService.isOperator(user)) {
                Predicate p1 = cb.equal(cb.lower(root.get("assignedTo")), user.name().toLowerCase());
                Predicate p2 = cb.equal(cb.lower(root.get("assignedTo")), user.email().toLowerCase());
                Predicate p3 = cb.equal(cb.lower(root.get("requester")), user.name().toLowerCase());
                Predicate p4 = cb.equal(cb.lower(root.get("requester")), user.email().toLowerCase());
                predicates.add(cb.or(p1, p2, p3, p4));
            }
            return predicates.isEmpty() ? cb.conjunction() : cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TiTicketEntity> result = ticketRepository.findAll(spec, pageable);
        return new PagedResponseDto<>(
                result.getContent().stream().map(this::toDto).toList(),
                safePage,
                safePageSize,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public PagedResponseDto<TiAssetHistoryDto> searchAssetHistory(
            String authorizationHeader,
            Long assetId,
            String responsible,
            String status,
            String eventType,
            Integer page,
            Integer pageSize
    ) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        TiAssetEntity asset = assetRepository.findById(assetId).orElseThrow();
        assertOperatorOwnsAsset(user, asset);
        int safePage = Math.max(1, page == null ? 1 : page);
        int safePageSize = normalizePageSize(pageSize);
        Pageable pageable = PageRequest.of(safePage - 1, safePageSize, Sort.by(Sort.Direction.DESC, "changedAt"));

        Specification<TiAssetHistoryEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("asset").get("id"), assetId));
            String byResponsible = trim(responsible);
            String byStatus = trim(status);
            String byEventType = trim(eventType);

            if (byResponsible != null) {
                Predicate p1 = cb.like(cb.lower(root.get("previousResponsibleUserName")), "%" + byResponsible.toLowerCase() + "%");
                Predicate p2 = cb.like(cb.lower(root.get("newResponsibleUserName")), "%" + byResponsible.toLowerCase() + "%");
                predicates.add(cb.or(p1, p2));
            }
            if (byStatus != null) {
                Predicate p1 = cb.like(cb.lower(root.get("previousStatus")), "%" + byStatus.toLowerCase() + "%");
                Predicate p2 = cb.like(cb.lower(root.get("newStatus")), "%" + byStatus.toLowerCase() + "%");
                predicates.add(cb.or(p1, p2));
            }
            if (byEventType != null) {
                predicates.add(cb.equal(cb.lower(root.get("eventType")), byEventType.toLowerCase()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TiAssetHistoryEntity> result = assetHistoryRepository.findAll(spec, pageable);
        return new PagedResponseDto<>(
                result.getContent().stream().map(this::toHistoryDto).toList(),
                safePage,
                safePageSize,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Transactional
    public TiTermDto createTerm(TiTermRequestDto req, String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        TiTermEntity e = new TiTermEntity();
        applyTerm(req, e, user);
        TiTermEntity saved = termRepository.save(e);
        syncAssetByTermChange(null, saved, user);
        return toDto(saved);
    }

    @Transactional
    public TiTermDto updateTerm(Long id, TiTermRequestDto req, String authorizationHeader) {
        TiTermEntity e = termRepository.findById(id).orElseThrow();
        UserDto user = accessControlService.requireUser(authorizationHeader);
        assertOperatorOwnsTerm(user, e);
        TiTermSnapshot previous = TiTermSnapshot.from(e);
        applyTerm(req, e, user);
        TiTermEntity saved = termRepository.save(e);
        syncAssetByTermChange(previous, saved, user);
        return toDto(saved);
    }

    @Transactional
    public TiTermDto inactivateTerm(Long id, String authorizationHeader) {
        TiTermEntity e = termRepository.findById(id).orElseThrow();
        UserDto user = accessControlService.requireUser(authorizationHeader);
        assertOperatorOwnsTerm(user, e);
        TiTermSnapshot previous = TiTermSnapshot.from(e);
        e.setStatus("Revogado");
        e.setActive(false);
        TiTermEntity saved = termRepository.save(e);
        syncAssetByTermChange(previous, saved, user);
        return toDto(saved);
    }

    @Transactional
    public void deleteTerm(Long id, String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        TiTermEntity term = termRepository.findById(id).orElseThrow();
        if (accessControlService.isOperator(user)) assertOperatorOwnsTerm(user, term);
        syncAssetByTermChange(TiTermSnapshot.from(term), null, user);
        termRepository.deleteById(id);
    }

    @Transactional
    public TiAssetDto createAsset(TiAssetRequestDto req, String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        TiAssetEntity e = new TiAssetEntity();
        e.setInternalCode(nextAssetCode());
        e.setActive(true);
        applyAsset(req, e, user);
        TiAssetEntity saved = assetRepository.save(e);
        createAssetHistory(saved, null, null, null, null, null, user);
        return toDto(saved);
    }

    @Transactional
    public TiAssetDto updateAsset(Long id, TiAssetRequestDto req, String authorizationHeader) {
        TiAssetEntity e = assetRepository.findById(id).orElseThrow();
        UserDto user = accessControlService.requireUser(authorizationHeader);
        assertOperatorOwnsAsset(user, e);
        if (!Objects.equals(e.getResponsibleUserId(), req.responsibleUserId()) && req.responsibleUserId() != null && req.linkedTermId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transferencia exige vinculo de termo para o novo responsavel.");
        }
        Long prevResponsibleUserId = e.getResponsibleUserId();
        String prevResponsibleUserName = e.getResponsibleUserName();
        String prevStatus = e.getStatus() == null ? null : e.getStatus().name();
        Long prevTermId = e.getLinkedTermId();
        String prevTermTitle = e.getLinkedTermTitle();
        applyAsset(req, e, user);
        TiAssetEntity saved = assetRepository.save(e);
        if (changedTrackedFields(saved, prevResponsibleUserId, prevResponsibleUserName, prevStatus, prevTermId, prevTermTitle)) {
            createAssetHistory(saved, prevResponsibleUserId, prevResponsibleUserName, prevStatus, prevTermId, prevTermTitle, user);
        }
        return toDto(saved);
    }

    @Transactional
    public void deleteAsset(Long id, String authorizationHeader) {
        UserDto user = accessControlService.requireUser(authorizationHeader);
        TiAssetEntity asset = assetRepository.findById(id).orElseThrow();
        if (accessControlService.isOperator(user)) {
            assertOperatorOwnsAsset(user, asset);
        }
        assetHistoryRepository.deleteByAsset_Id(asset.getId());
        assetRepository.deleteById(id);
    }

    @Transactional
    public TiAssetDto inactivateAsset(Long id, String authorizationHeader) {
        TiAssetEntity asset = assetRepository.findById(id).orElseThrow();
        UserDto user = accessControlService.requireUser(authorizationHeader);
        assertOperatorOwnsAsset(user, asset);
        asset.setActive(false);
        return toDto(assetRepository.save(asset));
    }

    @Transactional
    public TiAssetDto returnAsset(Long id, EquipmentCondition equipmentCondition, String authorizationHeader) {
        TiAssetEntity asset = assetRepository.findById(id).orElseThrow();
        UserDto user = accessControlService.requireUser(authorizationHeader);
        assertOperatorOwnsAsset(user, asset);

        Long prevResponsibleUserId = asset.getResponsibleUserId();
        String prevResponsibleUserName = asset.getResponsibleUserName();
        String prevStatus = asset.getStatus() == null ? null : asset.getStatus().name();
        Long prevTermId = asset.getLinkedTermId();
        String prevTermTitle = asset.getLinkedTermTitle();
        if (prevTermId != null) {
            TiTermEntity linkedTerm = termRepository.findById(prevTermId).orElse(null);
            if (linkedTerm != null) {
                linkedTerm.setStatus("Revogado");
                linkedTerm.setActive(false);
                termRepository.save(linkedTerm);
            }
        }

        asset.setResponsibleUserId(null);
        asset.setResponsibleUserName(null);
        asset.setLinkedTermId(null);
        asset.setLinkedTermTitle(null);
        asset.setStatus(AssetStatus.DISPONIVEL);
        asset.setActive(true);
        asset.setEquipmentCondition(equipmentCondition == null ? EquipmentCondition.USADO : equipmentCondition);

        TiAssetEntity saved = assetRepository.save(asset);
        if (changedTrackedFields(saved, prevResponsibleUserId, prevResponsibleUserName, prevStatus, prevTermId, prevTermTitle)) {
            createAssetHistory(saved, prevResponsibleUserId, prevResponsibleUserName, prevStatus, prevTermId, prevTermTitle, user);
        }
        return toDto(saved);
    }

    @Transactional
    public TiAssetDto reactivateAsset(Long id, String authorizationHeader) {
        TiAssetEntity asset = assetRepository.findById(id).orElseThrow();
        UserDto user = accessControlService.requireUser(authorizationHeader);
        assertOperatorOwnsAsset(user, asset);
        asset.setActive(true);
        return toDto(assetRepository.save(asset));
    }

    private void applyAsset(TiAssetRequestDto req, TiAssetEntity e, UserDto user) {
        boolean operator = accessControlService.isOperator(user);
        Long targetResponsibleId = operator ? user.id() : req.responsibleUserId();
        String targetResponsibleName = operator ? user.name() : trim(req.responsibleUserName());
        if (targetResponsibleId == null) {
            targetResponsibleName = null;
        }

        e.setCompany(trim(req.company()));
        e.setCompanyErpCode(trim(req.companyErpCode()));
        e.setAssetType(req.assetType());
        e.setDepartment(trim(req.department()));
        e.setBrand(trim(req.brand()));
        e.setModel(trim(req.model()));
        e.setSerialNumber(trim(req.serialNumber()));
        e.setPatrimony(trim(req.patrimony()));
        e.setDetailedDescription(trim(req.detailedDescription()));
        e.setResponsibleUserId(targetResponsibleId);
        e.setResponsibleUserName(targetResponsibleName);
        if (targetResponsibleId == null) {
            e.setStatus(AssetStatus.DISPONIVEL);
            e.setLinkedTermId(null);
            e.setLinkedTermTitle(null);
        } else {
            e.setStatus(AssetStatus.EM_USO);
            e.setLinkedTermId(req.linkedTermId());
            e.setLinkedTermTitle(trim(req.linkedTermTitle()));
        }
        e.setTransferHistoryText(String.join("\n", req.transferHistory() == null ? List.of() : req.transferHistory().stream().map(String::trim).filter(s -> !s.isBlank()).toList()));
        e.setIpMode(req.ipMode() == null ? IpMode.DHCP : req.ipMode());
        e.setIpAddress(trim(req.ipAddress()));
        e.setImei(trim(req.imei()));
        e.setEquipmentCondition(req.equipmentCondition() == null ? EquipmentCondition.USADO : req.equipmentCondition());
        e.setExtraFieldsJson(writeJson(req.extraFields() == null ? Map.of() : req.extraFields()));
    }

    private void applyTerm(TiTermRequestDto req, TiTermEntity e, UserDto user) {
        boolean operator = accessControlService.isOperator(user);
        String normalizedStatus = normalizeTermStatus(req.status());
        if (isTermActive(normalizedStatus) && req.linkedAssetId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Termo ativo exige item vinculado de Ativos de Informatica.");
        }
        e.setType(req.type());
        e.setDefaultTermName("Termo de Responsabilidade");
        e.setLinkedUserName(operator ? user.name() : req.linkedUserName().trim());
        if (req.linkedAssetId() != null) {
            TiAssetEntity asset = assetRepository.findById(req.linkedAssetId()).orElseThrow();
            if (!asset.isActive()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ativo selecionado esta inativo.");
            }
            boolean linkedToCurrentTerm = e.getId() != null && Objects.equals(asset.getLinkedTermId(), e.getId());
            boolean availableForNewLink = asset.getStatus() == AssetStatus.DISPONIVEL || linkedToCurrentTerm;
            if (isTermActive(normalizedStatus) && !availableForNewLink) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ativo em uso. Para reaproveitar, devolva ou transfira o termo atual.");
            }
            e.setLinkedAssetId(asset.getId());
            e.setLinkedItemDescription(assetLabel(asset));
        } else {
            e.setLinkedAssetId(null);
            e.setLinkedItemDescription(trim(req.linkedItemDescription()));
        }
        e.setStartDate(req.startDate() == null || req.startDate().isBlank() ? LocalDate.now().toString() : req.startDate());
        e.setStatus(normalizedStatus);
        e.setActive(isTermActive(normalizedStatus));
        e.setDocumentPath(trim(req.documentPath()));
    }

    private void syncAssetByTermChange(TiTermSnapshot previous, TiTermEntity current, UserDto actor) {
        if (previous != null && previous.linkedAssetId() != null) {
            boolean shouldReleasePrevious = current == null
                    || !Objects.equals(previous.linkedAssetId(), current.getLinkedAssetId())
                    || !isTermActive(current.getStatus());
            if (shouldReleasePrevious) {
                TiAssetEntity previousAsset = assetRepository.findById(previous.linkedAssetId()).orElse(null);
                if (previousAsset != null && Objects.equals(previousAsset.getLinkedTermId(), previous.id())) {
                    syncAssetOwnership(previousAsset, null, null, null, actor);
                }
            }
        }

        if (current != null && current.getLinkedAssetId() != null && isTermActive(current.getStatus())) {
            TiAssetEntity currentAsset = assetRepository.findById(current.getLinkedAssetId()).orElseThrow();
            Long responsibleUserId = resolveUserId(current.getLinkedUserName());
            syncAssetOwnership(currentAsset, responsibleUserId, current.getLinkedUserName(), current, actor);
        }
    }

    private void syncAssetOwnership(
            TiAssetEntity asset,
            Long responsibleUserId,
            String responsibleUserName,
            TiTermEntity term,
            UserDto actor
    ) {
        Long prevResponsibleUserId = asset.getResponsibleUserId();
        String prevResponsibleUserName = asset.getResponsibleUserName();
        String prevStatus = asset.getStatus() == null ? null : asset.getStatus().name();
        Long prevTermId = asset.getLinkedTermId();
        String prevTermTitle = asset.getLinkedTermTitle();

        if (term == null) {
            asset.setResponsibleUserId(null);
            asset.setResponsibleUserName(null);
            asset.setLinkedTermId(null);
            asset.setLinkedTermTitle(null);
            asset.setStatus(AssetStatus.DISPONIVEL);
        } else {
            asset.setResponsibleUserId(responsibleUserId);
            asset.setResponsibleUserName(responsibleUserName);
            asset.setLinkedTermId(term.getId());
            asset.setLinkedTermTitle(term.getDefaultTermName());
            asset.setStatus(AssetStatus.EM_USO);
        }

        TiAssetEntity saved = assetRepository.save(asset);
        if (changedTrackedFields(saved, prevResponsibleUserId, prevResponsibleUserName, prevStatus, prevTermId, prevTermTitle)) {
            createAssetHistory(saved, prevResponsibleUserId, prevResponsibleUserName, prevStatus, prevTermId, prevTermTitle, actor);
        }
    }

    private Long resolveUserId(String linkedUserName) {
        String value = trim(linkedUserName);
        if (value == null) return null;
        return appUserRepository.findByEmailIgnoreCase(value)
                .or(() -> appUserRepository.findFirstByNameIgnoreCase(value))
                .map(u -> u.getId())
                .orElse(null);
    }

    private String normalizeTermStatus(String status) {
        String value = trim(status);
        if (value == null) return "Ativo";
        if (value.equalsIgnoreCase("ativo")) return "Ativo";
        if (value.equalsIgnoreCase("concluido")) return "Concluido";
        if (value.equalsIgnoreCase("inativo")) return "Inativo";
        if (value.equalsIgnoreCase("revogado")) return "Revogado";
        if (value.equalsIgnoreCase("devolvido")) return "Devolvido";
        return value;
    }

    private boolean isTermActive(String status) {
        return status != null && status.equalsIgnoreCase("Ativo");
    }

    @Transactional
    public TicketDto addTicketMessage(Long ticketId, TicketMessageRequestDto req, String authorizationHeader) {
        TiTicketEntity ticket = ticketRepository.findById(ticketId).orElseThrow();
        UserDto user = accessControlService.requireUser(authorizationHeader);
        assertOperatorOwnsTicket(user, ticket);
        TiTicketMessageEntity msg = new TiTicketMessageEntity();
        msg.setTicket(ticket);
        msg.setAuthor(accessControlService.isOperator(user) ? user.name() : req.author().trim());
        msg.setSentAt(req.sentAt() == null || req.sentAt().isBlank() ? java.time.LocalDateTime.now().toString() : req.sentAt());
        msg.setMessage(req.message().trim());
        ticket.getMessages().add(msg);
        return toDto(ticketRepository.save(ticket));
    }

    @Transactional
    public TicketDto updateTicketStatus(Long ticketId, String status, String authorizationHeader) {
        TiTicketEntity ticket = ticketRepository.findById(ticketId).orElseThrow();
        UserDto user = accessControlService.requireUser(authorizationHeader);
        assertOperatorOwnsTicket(user, ticket);
        ticket.setStatus(status == null || status.isBlank() ? ticket.getStatus() : status.trim());
        return toDto(ticketRepository.save(ticket));
    }

    private void assertOperatorOwnsAsset(UserDto user, TiAssetEntity asset) {
        if (accessControlService.isOperator(user) && !Objects.equals(asset.getResponsibleUserId(), user.id())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operador sem acesso a esse ativo");
        }
    }

    private void assertOperatorOwnsTerm(UserDto user, TiTermEntity term) {
        if (accessControlService.isOperator(user) && !isOwnTerm(term, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operador sem acesso a esse termo");
        }
    }

    private void assertOperatorOwnsTicket(UserDto user, TiTicketEntity ticket) {
        if (accessControlService.isOperator(user) && !isOwnTicket(ticket, user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operador sem acesso a esse chamado");
        }
    }

    private boolean isOwnTerm(TiTermEntity term, UserDto user) {
        if (user == null) return false;
        return equalsIgnoreCase(term.getLinkedUserName(), user.name()) || equalsIgnoreCase(term.getLinkedUserName(), user.email());
    }

    private boolean isOwnTicket(TiTicketEntity ticket, UserDto user) {
        if (user == null) return false;
        return equalsIgnoreCase(ticket.getRequester(), user.name())
                || equalsIgnoreCase(ticket.getRequester(), user.email())
                || equalsIgnoreCase(ticket.getAssignedTo(), user.name())
                || equalsIgnoreCase(ticket.getAssignedTo(), user.email());
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }

    private boolean changedTrackedFields(
            TiAssetEntity asset,
            Long prevResponsibleUserId,
            String prevResponsibleUserName,
            String prevStatus,
            Long prevTermId,
            String prevTermTitle
    ) {
        String newStatus = asset.getStatus() == null ? null : asset.getStatus().name();
        return !Objects.equals(prevResponsibleUserId, asset.getResponsibleUserId())
                || !equalsIgnoreCase(prevResponsibleUserName, asset.getResponsibleUserName())
                || !Objects.equals(prevStatus, newStatus)
                || !Objects.equals(prevTermId, asset.getLinkedTermId())
                || !equalsIgnoreCase(prevTermTitle, asset.getLinkedTermTitle());
    }

    private void createAssetHistory(
            TiAssetEntity asset,
            Long prevResponsibleUserId,
            String prevResponsibleUserName,
            String prevStatus,
            Long prevTermId,
            String prevTermTitle,
            UserDto actor
    ) {
        TiAssetHistoryEntity h = new TiAssetHistoryEntity();
        h.setAsset(asset);
        h.setPreviousResponsibleUserId(prevResponsibleUserId);
        h.setPreviousResponsibleUserName(prevResponsibleUserName);
        h.setNewResponsibleUserId(asset.getResponsibleUserId());
        h.setNewResponsibleUserName(asset.getResponsibleUserName());
        h.setPreviousStatus(prevStatus);
        h.setNewStatus(asset.getStatus() == null ? null : asset.getStatus().name());
        h.setPreviousTermId(prevTermId);
        h.setPreviousTermTitle(prevTermTitle);
        h.setNewTermId(asset.getLinkedTermId());
        h.setNewTermTitle(asset.getLinkedTermTitle());
        h.setChangedByName(actor == null ? "Sistema" : actor.name());

        String eventType = "ATUALIZACAO";
        boolean hadResponsibleBefore = prevResponsibleUserId != null || trim(prevResponsibleUserName) != null;
        boolean hasResponsibleNow = asset.getResponsibleUserId() != null || trim(asset.getResponsibleUserName()) != null;
        if (!hadResponsibleBefore && hasResponsibleNow) eventType = "ENTREGA";
        if (hadResponsibleBefore && !hasResponsibleNow) eventType = "DEVOLUCAO";
        if (hadResponsibleBefore && hasResponsibleNow
                && (!Objects.equals(prevResponsibleUserId, asset.getResponsibleUserId())
                || !equalsIgnoreCase(prevResponsibleUserName, asset.getResponsibleUserName()))) {
            eventType = "TRANSFERENCIA";
        }
        if (prevResponsibleUserId == null && prevStatus == null && prevTermId == null) eventType = "CADASTRO";
        h.setEventType(eventType);
        h.setNote(buildAssetHistoryNote(h));
        assetHistoryRepository.save(h);
    }

    private String buildAssetHistoryNote(TiAssetHistoryEntity h) {
        List<String> parts = new ArrayList<>();
        if (!Objects.equals(h.getPreviousResponsibleUserName(), h.getNewResponsibleUserName())) {
            parts.add("Responsavel: " + (h.getPreviousResponsibleUserName() == null ? "Sem responsavel" : h.getPreviousResponsibleUserName())
                    + " -> " + (h.getNewResponsibleUserName() == null ? "Sem responsavel" : h.getNewResponsibleUserName()));
        }
        if (!Objects.equals(h.getPreviousStatus(), h.getNewStatus())) {
            parts.add("Status: " + (h.getPreviousStatus() == null ? "-" : h.getPreviousStatus()) + " -> " + (h.getNewStatus() == null ? "-" : h.getNewStatus()));
        }
        if (!Objects.equals(h.getPreviousTermId(), h.getNewTermId())) {
            parts.add("Termo: " + (h.getPreviousTermTitle() == null ? "-" : h.getPreviousTermTitle()) + " -> " + (h.getNewTermTitle() == null ? "-" : h.getNewTermTitle()));
        }
        if (parts.isEmpty()) return "Atualizacao de metadados do ativo.";
        return String.join(" | ", parts);
    }

    private int normalizePageSize(Integer pageSize) {
        if (pageSize == null || pageSize < 1) return 10;
        return Math.min(pageSize, 100);
    }

    private String nextAssetCode() {
        long next = assetRepository.findMaxInternalCodeSequence() + 1L;
        return "TI-" + String.format("%04d", next);
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
        return new TiTermDto(
                e.getId(),
                e.getType(),
                e.getDefaultTermName(),
                e.getLinkedUserName(),
                e.getLinkedAssetId(),
                e.getLinkedItemDescription(),
                buildTermDescription(e),
                e.getStartDate(),
                e.getStatus(),
                e.getDocumentPath(),
                e.isActive()
        );
    }

    private String buildTermDescription(TiTermEntity e) {
        StringBuilder sb = new StringBuilder("Termo de Responsabilidade");
        if (e.getType() != null) {
            sb.append(" ").append(e.getType().name().toLowerCase());
        }
        if (e.getLinkedItemDescription() != null && !e.getLinkedItemDescription().isBlank()) {
            sb.append(" ").append(e.getLinkedItemDescription().trim());
        }
        if (e.getLinkedUserName() != null && !e.getLinkedUserName().isBlank()) {
            sb.append(" - ").append(e.getLinkedUserName().trim());
        }
        return sb.toString();
    }

    private String assetLabel(TiAssetEntity asset) {
        String code = asset.getInternalCode() == null ? "ID " + asset.getId() : asset.getInternalCode();
        return code + " - " + asset.getAssetType();
    }

    private record TiTermSnapshot(
            Long id,
            Long linkedAssetId
    ) {
        private static TiTermSnapshot from(TiTermEntity term) {
            return new TiTermSnapshot(term.getId(), term.getLinkedAssetId());
        }
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
                e.getEquipmentCondition() == null ? EquipmentCondition.USADO : e.getEquipmentCondition(),
                extra,
                e.isActive()
        );
    }

    private TicketDto toDto(TiTicketEntity e) {
        List<TicketMessageDto> messages = e.getMessages().stream()
                .map(m -> new TicketMessageDto(m.getId(), m.getAuthor(), m.getSentAt(), m.getMessage(), false))
                .toList();
        return new TicketDto(e.getId(), e.getSubject(), e.getRequester(), e.getAssignedTo(), e.getPriority(), e.getStatus(), messages);
    }

    private TiAssetHistoryDto toHistoryDto(TiAssetHistoryEntity e) {
        return new TiAssetHistoryDto(
                e.getId(),
                e.getAsset().getId(),
                e.getEventType(),
                e.getPreviousResponsibleUserId(),
                e.getPreviousResponsibleUserName(),
                e.getNewResponsibleUserId(),
                e.getNewResponsibleUserName(),
                e.getPreviousStatus(),
                e.getNewStatus(),
                e.getPreviousTermId(),
                e.getPreviousTermTitle(),
                e.getNewTermId(),
                e.getNewTermTitle(),
                e.getNote(),
                e.getChangedByName(),
                e.getChangedAt()
        );
    }
}
