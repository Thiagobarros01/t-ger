package br.com.tger.api.crm.application.service;

import br.com.tger.api.crm.domain.DealStatus;
import br.com.tger.api.crm.domain.OpportunityType;
import br.com.tger.api.crm.dto.CloseDealLostRequestDto;
import br.com.tger.api.crm.dto.CrmHistorySyncResultDto;
import br.com.tger.api.crm.dto.CreateDealRequestDto;
import br.com.tger.api.crm.dto.DealResponseDto;
import br.com.tger.api.crm.dto.MoveDealStageRequestDto;
import br.com.tger.api.crm.dto.UpdateDealRequestDto;
import br.com.tger.api.crm.infrastructure.entity.CrmDealEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmLossReasonEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmPipelineEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmStageEntity;
import br.com.tger.api.crm.infrastructure.repository.CrmDealRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmLossReasonRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmPipelineRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmStageRepository;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.persistence.entity.CompanyEntity;
import br.com.tger.api.persistence.entity.CustomerEntity;
import br.com.tger.api.persistence.entity.GlobalParameterEntity;
import br.com.tger.api.persistence.entity.SellerEntity;
import br.com.tger.api.persistence.repository.CompanyRepository;
import br.com.tger.api.persistence.repository.CustomerRepository;
import br.com.tger.api.persistence.repository.GlobalParameterRepository;
import br.com.tger.api.persistence.repository.SalesHistoryRepository;
import br.com.tger.api.persistence.repository.SellerRepository;
import br.com.tger.api.persistence.service.GlobalParameterPersistenceService;
import br.com.tger.api.dto.commercial.SalesHistorySyncProjection;
import br.com.tger.api.crm.domain.BusinessType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;
import java.util.Locale;

@Service
public class CrmDealService {
    private final CrmDealRepository dealRepository;
    private final CrmPipelineRepository pipelineRepository;
    private final CrmStageRepository stageRepository;
    private final CrmLossReasonRepository lossReasonRepository;
    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final SellerRepository sellerRepository;
    private final GlobalParameterRepository globalParameterRepository;
    private final SalesHistoryRepository salesHistoryRepository;
    private final CrmAccessService accessService;

    public CrmDealService(
            CrmDealRepository dealRepository,
            CrmPipelineRepository pipelineRepository,
            CrmStageRepository stageRepository,
            CrmLossReasonRepository lossReasonRepository,
            CustomerRepository customerRepository,
            CompanyRepository companyRepository,
            SellerRepository sellerRepository,
            GlobalParameterRepository globalParameterRepository,
            SalesHistoryRepository salesHistoryRepository,
            CrmAccessService accessService
    ) {
        this.dealRepository = dealRepository;
        this.pipelineRepository = pipelineRepository;
        this.stageRepository = stageRepository;
        this.lossReasonRepository = lossReasonRepository;
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
        this.sellerRepository = sellerRepository;
        this.globalParameterRepository = globalParameterRepository;
        this.salesHistoryRepository = salesHistoryRepository;
        this.accessService = accessService;
    }

    public List<DealResponseDto> listDeals(String authorizationHeader) {
        UserDto user = accessService.resolveUser(authorizationHeader);
        if (accessService.isOperator(user)) {
            String linkedSellerErpCode = user.linkedSellerErpCode();
            if (linkedSellerErpCode != null && !linkedSellerErpCode.isBlank()) {
                return dealRepository.findByVendedorErpCodeIgnoreCase(linkedSellerErpCode).stream().map(this::toDto).toList();
            }
            return dealRepository.findByVendedorEmailIgnoreCase(user.email()).stream().map(this::toDto).toList();
        }
        return dealRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public DealResponseDto createDeal(CreateDealRequestDto request) {
        CustomerEntity cliente = customerRepository.findById(request.clienteId()).orElseThrow();
        SellerEntity vendedor = sellerRepository.findById(request.vendedorId()).orElseThrow();
        CrmPipelineEntity pipeline = pipelineRepository.findById(request.pipelineId()).orElseThrow();
        CrmStageEntity stage = stageRepository.findById(request.stageId()).orElseThrow();

        validatePipelineAndStage(request.pipelineId(), pipeline, stage);
        if (pipeline.getTipoNegocio() != request.tipoNegocio()) {
            throw new IllegalArgumentException("Tipo de negocio do deal deve ser igual ao tipo do pipeline.");
        }

        CrmDealEntity entity = new CrmDealEntity();
        entity.setCliente(cliente);
        entity.setEmpresa(resolveCompany(request.empresaId()));
        entity.setVendedor(vendedor);
        entity.setTipoNegocio(request.tipoNegocio());
        entity.setTipoOportunidade(request.tipoOportunidade() == null ? OpportunityType.NOVA : request.tipoOportunidade());
        entity.setPipeline(pipeline);
        entity.setStage(stage);
        entity.setValorEstimado(request.valorEstimado());
        entity.setProbabilidade(request.probabilidade());
        entity.setDataPrevistaFechamento(request.dataPrevistaFechamento());
        entity.setStatus(DealStatus.ABERTA);

        applyStageStatusRules(entity, stage, request.motivoPerdaId());
        CrmDealEntity saved = dealRepository.save(entity);
        createRebuyFollowUp(saved);
        return toDto(saved);
    }

    @Transactional
    public DealResponseDto moveStage(Long dealId, MoveDealStageRequestDto request) {
        CrmDealEntity deal = dealRepository.findById(dealId).orElseThrow();
        CrmStageEntity stage = stageRepository.findById(request.stageId()).orElseThrow();
        validatePipelineAndStage(deal.getPipeline().getId(), deal.getPipeline(), stage);
        deal.setStage(stage);
        applyStageStatusRules(deal, stage, request.motivoPerdaId());
        if (!stage.isWon() && !stage.isLost()) {
            deal.setStatus(DealStatus.ABERTA);
            deal.setMotivoPerda(null);
        }
        CrmDealEntity saved = dealRepository.save(deal);
        createRebuyFollowUp(saved);
        return toDto(saved);
    }

    @Transactional
    public DealResponseDto closeAsWon(Long dealId) {
        CrmDealEntity deal = dealRepository.findById(dealId).orElseThrow();
        deal.setStatus(DealStatus.GANHA);
        deal.setMotivoPerda(null);
        CrmDealEntity saved = dealRepository.save(deal);
        createRebuyFollowUp(saved);
        return toDto(saved);
    }

    @Transactional
    public DealResponseDto closeAsLost(Long dealId, CloseDealLostRequestDto request) {
        CrmDealEntity deal = dealRepository.findById(dealId).orElseThrow();
        CrmLossReasonEntity motivo = resolveLossReasonRequired(request.motivoPerdaId());
        deal.setStatus(DealStatus.PERDIDA);
        deal.setMotivoPerda(motivo);
        return toDto(dealRepository.save(deal));
    }

    @Transactional
    public DealResponseDto updateDeal(Long dealId, UpdateDealRequestDto request) {
        CrmDealEntity deal = dealRepository.findById(dealId).orElseThrow();
        CustomerEntity cliente = customerRepository.findById(request.clienteId()).orElseThrow();
        SellerEntity vendedor = sellerRepository.findById(request.vendedorId()).orElseThrow();
        deal.setCliente(cliente);
        deal.setEmpresa(resolveCompany(request.empresaId()));
        deal.setVendedor(vendedor);
        if (request.tipoOportunidade() != null) {
            deal.setTipoOportunidade(request.tipoOportunidade());
        }
        deal.setValorEstimado(request.valorEstimado());
        deal.setProbabilidade(request.probabilidade());
        deal.setDataPrevistaFechamento(request.dataPrevistaFechamento());
        return toDto(dealRepository.save(deal));
    }

    @Transactional
    public CrmHistorySyncResultDto syncDealsFromSalesHistory(String authorizationHeader) {
        UserDto user = accessService.resolveUser(authorizationHeader);
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao invalida");
        if (accessService.isOperator(user)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operador nao pode executar sincronizacao global");
        }

        Map<BusinessType, PipelineSyncContext> pipelineContexts = buildPipelineSyncContexts();
        if (pipelineContexts.isEmpty()) {
            return new CrmHistorySyncResultDto(0, 0, 0, 0, 0);
        }

        int recompraDias = getIntParam(GlobalParameterPersistenceService.KEY_RECOMPRA_DIAS, 40);
        int recorrenciaDias = 30;
        int resgateDias = getIntParam(GlobalParameterPersistenceService.KEY_RESGATE_DIAS, 60);
        BigDecimal recompraValor = getDecimalParam(GlobalParameterPersistenceService.KEY_RECOMPRA_VALOR, new BigDecimal("500.00"));
        BigDecimal resgateValor = getDecimalParam(GlobalParameterPersistenceService.KEY_RESGATE_VALOR, new BigDecimal("300.00"));

        Map<String, CustomerEntity> customerByErp = new HashMap<>();
        for (CustomerEntity customer : customerRepository.findAll()) {
            if (customer.getErpCode() != null && !customer.getErpCode().isBlank()) {
                customerByErp.put(customer.getErpCode().trim().toLowerCase(), customer);
            }
        }

        Map<String, SellerEntity> sellerByErp = new HashMap<>();
        for (SellerEntity seller : sellerRepository.findAll()) {
            if (seller.getErpCode() != null && !seller.getErpCode().isBlank()) {
                sellerByErp.put(seller.getErpCode().trim().toLowerCase(), seller);
            }
        }

        int analyzed = 0;
        int created = 0;
        int updated = 0;
        int skipped = 0;
        int unmapped = 0;

        List<SalesHistorySyncProjection> summarized = salesHistoryRepository.summarizeLastOrderByCustomerSeller();
        LocalDate today = LocalDate.now();

        for (SalesHistorySyncProjection row : summarized) {
            analyzed++;
            if (row.getLastOrderDate() == null) {
                skipped++;
                continue;
            }

            CustomerEntity customer = customerByErp.get(trimLower(row.getCustomerErpCode()));
            SellerEntity seller = sellerByErp.get(trimLower(row.getSellerErpCode()));
            if (customer == null || seller == null) {
                unmapped++;
                continue;
            }

            BusinessType businessType = resolveBusinessTypeForCustomer(customer);
            PipelineSyncContext context = pipelineContexts.get(businessType);
            if (context == null) {
                context = pipelineContexts.get(BusinessType.B2B);
            }
            if (context == null) {
                skipped++;
                continue;
            }

            long daysSinceLastOrder = ChronoUnit.DAYS.between(row.getLastOrderDate(), today);
            OpportunityType tipo;
            BigDecimal valor;
            int probabilidade;
            CrmStageEntity targetStage = context.defaultStage();
            if (daysSinceLastOrder >= resgateDias) {
                tipo = OpportunityType.RESGATE;
                valor = valueFromHistory(row.getLastOrderValue(), row.getValue90d(), row.getOrders90d(), resgateValor);
                probabilidade = 20;
            } else if (daysSinceLastOrder >= recompraDias) {
                tipo = OpportunityType.RECOMPRA;
                valor = valueFromHistory(row.getLastOrderValue(), row.getValue90d(), row.getOrders90d(), recompraValor);
                probabilidade = 35;
            } else {
                tipo = OpportunityType.NOVA;
                valor = valueFromHistory(row.getLastOrderValue(), row.getValue90d(), row.getOrders90d(), recompraValor);
                probabilidade = 40;
            }
            String situacaoPedido = deriveOrderSituation(row);
            DealStatus crmStatus = mapCrmStatusByOrderSituation(situacaoPedido);
            targetStage = resolveStageByBusinessRule(context.funnelStageMap(), situacaoPedido, context.defaultStage());

            if ("FATURADO".equals(situacaoPedido) && daysSinceLastOrder >= recompraDias) {
                tipo = OpportunityType.RECOMPRA;
                crmStatus = DealStatus.ABERTA;
                targetStage = context.funnelStageMap().getOrDefault(
                        "RECOMPRA",
                        context.funnelStageMap().getOrDefault("EM_NEGOCIACAO", context.defaultStage())
                );
            }
            if ("FATURADO".equals(situacaoPedido) && daysSinceLastOrder < recorrenciaDias) {
                targetStage = context.funnelStageMap().getOrDefault("RECORRENTE", targetStage);
                crmStatus = DealStatus.ABERTA;
            }

            String sourceSyncKey = buildSourceSyncKey(context.pipeline().getTipoNegocio(), customer.getErpCode(), seller.getErpCode(), tipo);
            CrmDealEntity existing = dealRepository.findFirstBySourceSyncKeyOrderByUpdatedAtDesc(sourceSyncKey).orElse(null);
            if (existing == null) {
                existing = dealRepository
                        .findFirstByClienteIdAndVendedorIdAndPipelineIdAndStatusAndTipoOportunidadeAndSourceSyncKeyIsNullOrderByUpdatedAtDesc(
                                customer.getId(),
                                seller.getId(),
                                context.pipeline().getId(),
                                DealStatus.ABERTA,
                                tipo
                        )
                        .orElse(null);
            }

            if (existing == null) {
                CrmDealEntity deal = new CrmDealEntity();
                deal.setCliente(customer);
                deal.setEmpresa(null);
                deal.setVendedor(seller);
                deal.setTipoNegocio(context.pipeline().getTipoNegocio());
                deal.setTipoOportunidade(tipo);
                deal.setPipeline(context.pipeline());
                deal.setStage(targetStage);
                deal.setValorEstimado(valor);
                deal.setProbabilidade(probabilidade);
                deal.setStatus(crmStatus);
                deal.setDataPrevistaFechamento(today.plusDays(tipo == OpportunityType.RESGATE ? 10 : 5));
                deal.setSourceOrderNumber(trim(row.getLastOrderNumber()));
                deal.setSourceOrderStatus(situacaoPedido);
                deal.setSourceOrderDate(row.getLastOrderDate());
                deal.setSourceSyncKey(sourceSyncKey);
                dealRepository.save(deal);
                created++;
            } else {
                existing.setSourceSyncKey(sourceSyncKey);
                if (existing.getTipoOportunidade() == null) {
                    existing.setTipoOportunidade(tipo);
                }
                if (existing.getValorEstimado() == null || existing.getValorEstimado().compareTo(BigDecimal.ZERO) <= 0) {
                    existing.setValorEstimado(valor);
                }
                if (existing.getProbabilidade() == null || existing.getProbabilidade() <= 0) {
                    existing.setProbabilidade(probabilidade);
                }
                if (existing.getStage() == null || existing.getStage().isWon() || existing.getStage().isLost()) {
                    existing.setStage(targetStage);
                }
                existing.setStatus(crmStatus);
                existing.setStage(targetStage);
                if (existing.getDataPrevistaFechamento() == null) {
                    existing.setDataPrevistaFechamento(today.plusDays(tipo == OpportunityType.RESGATE ? 10 : 5));
                }
                existing.setSourceOrderNumber(trim(row.getLastOrderNumber()));
                existing.setSourceOrderStatus(situacaoPedido);
                existing.setSourceOrderDate(row.getLastOrderDate());
                dealRepository.save(existing);
                updated++;
            }
        }

        return new CrmHistorySyncResultDto(analyzed, created, updated, skipped, unmapped);
    }

    private void applyStageStatusRules(CrmDealEntity deal, CrmStageEntity stage, Long motivoPerdaId) {
        if (stage.isWon()) {
            deal.setStatus(DealStatus.GANHA);
            deal.setMotivoPerda(null);
            return;
        }
        if (stage.isLost()) {
            CrmLossReasonEntity motivo = resolveLossReasonRequired(motivoPerdaId);
            deal.setStatus(DealStatus.PERDIDA);
            deal.setMotivoPerda(motivo);
            return;
        }
        deal.setStatus(DealStatus.ABERTA);
        deal.setMotivoPerda(null);
    }

    private void validatePipelineAndStage(Long pipelineId, CrmPipelineEntity pipeline, CrmStageEntity stage) {
        if (!pipeline.getId().equals(pipelineId)) {
            throw new IllegalArgumentException("Pipeline invalido.");
        }
        if (!stage.getPipeline().getId().equals(pipelineId)) {
            throw new IllegalArgumentException("Stage nao pertence ao pipeline informado.");
        }
    }

    private CrmLossReasonEntity resolveLossReasonRequired(Long motivoPerdaId) {
        if (motivoPerdaId == null) {
            throw new IllegalArgumentException("Motivo de perda obrigatorio.");
        }
        CrmLossReasonEntity motivo = lossReasonRepository.findById(motivoPerdaId).orElseThrow();
        if (!motivo.isAtivo()) {
            throw new IllegalArgumentException("Motivo de perda inativo.");
        }
        return motivo;
    }

    private CompanyEntity resolveCompany(Long empresaId) {
        if (empresaId == null) return null;
        return companyRepository.findById(empresaId).orElseThrow();
    }

    private DealResponseDto toDto(CrmDealEntity e) {
        return new DealResponseDto(
                e.getId(),
                e.getCliente().getId(),
                e.getEmpresa() != null ? e.getEmpresa().getId() : null,
                e.getVendedor().getId(),
                e.getTipoNegocio(),
                e.getTipoOportunidade() == null ? OpportunityType.NOVA : e.getTipoOportunidade(),
                e.getPipeline().getId(),
                e.getStage().getId(),
                e.getValorEstimado(),
                e.getProbabilidade(),
                e.getStatus(),
                e.getSourceOrderNumber(),
                e.getSourceOrderStatus(),
                e.getSourceOrderDate(),
                e.getDataPrevistaFechamento(),
                e.getMotivoPerda() != null ? e.getMotivoPerda().getId() : null,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }

    private void createRebuyFollowUp(CrmDealEntity wonDeal) {
        if (wonDeal == null || wonDeal.getId() == null || wonDeal.getStatus() != DealStatus.GANHA) return;
        if (dealRepository.existsBySourceDealIdAndTipoOportunidade(wonDeal.getId(), OpportunityType.RECOMPRA)) return;

        CrmStageEntity firstStage = stageRepository.findFirstByPipelineIdAndIsWonFalseAndIsLostFalseOrderByOrdemAsc(wonDeal.getPipeline().getId());
        if (firstStage == null) {
            firstStage = stageRepository.findFirstByPipelineIdOrderByOrdemAsc(wonDeal.getPipeline().getId());
        }
        if (firstStage == null) return;

        int days = getIntParam(GlobalParameterPersistenceService.KEY_RECOMPRA_DIAS, 30);
        BigDecimal value = getDecimalParam(GlobalParameterPersistenceService.KEY_RECOMPRA_VALOR, new BigDecimal("500.00"));

        CrmDealEntity followUp = new CrmDealEntity();
        followUp.setCliente(wonDeal.getCliente());
        followUp.setEmpresa(wonDeal.getEmpresa());
        followUp.setVendedor(wonDeal.getVendedor());
        followUp.setTipoNegocio(wonDeal.getTipoNegocio());
        followUp.setTipoOportunidade(OpportunityType.RECOMPRA);
        followUp.setPipeline(wonDeal.getPipeline());
        followUp.setStage(firstStage);
        followUp.setValorEstimado(value);
        followUp.setProbabilidade(30);
        followUp.setStatus(DealStatus.ABERTA);
        followUp.setDataPrevistaFechamento(LocalDate.now().plusDays(Math.max(days, 1)));
        followUp.setSourceDealId(wonDeal.getId());
        dealRepository.save(followUp);
    }

    private int getIntParam(String key, int fallback) {
        String value = globalParameterRepository.findByParamKey(key).map(GlobalParameterEntity::getValue).orElse(null);
        if (value == null || value.isBlank()) return fallback;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private BigDecimal getDecimalParam(String key, BigDecimal fallback) {
        String value = globalParameterRepository.findByParamKey(key).map(GlobalParameterEntity::getValue).orElse(null);
        if (value == null || value.isBlank()) return fallback;
        try {
            return new BigDecimal(value.trim().replace(",", "."));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private String trimLower(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim().toLowerCase();
    }

    private String trim(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private String buildSourceSyncKey(String customerErpCode, String sellerErpCode, OpportunityType tipo) {
        String customer = trimLower(customerErpCode);
        String seller = trimLower(sellerErpCode);
        return (customer == null ? "-" : customer) + "|" + (seller == null ? "-" : seller) + "|" + tipo.name();
    }

    private String buildSourceSyncKey(BusinessType businessType, String customerErpCode, String sellerErpCode, OpportunityType tipo) {
        String type = businessType == null ? "-" : businessType.name();
        return type + "|" + buildSourceSyncKey(customerErpCode, sellerErpCode, tipo);
    }

    private BusinessType resolveBusinessTypeForCustomer(CustomerEntity customer) {
        String type = trim(customer.getType());
        if (type == null) return BusinessType.B2B;
        String normalized = type.toUpperCase(Locale.ROOT);
        if ("PF".equals(normalized) || "F".equals(normalized) || "FISICA".equals(normalized) || "FÍSICA".equals(normalized)) {
            return BusinessType.B2C;
        }
        return BusinessType.B2B;
    }

    private Map<BusinessType, PipelineSyncContext> buildPipelineSyncContexts() {
        Map<BusinessType, PipelineSyncContext> contexts = new HashMap<>();
        for (BusinessType businessType : BusinessType.values()) {
            CrmPipelineEntity pipeline = pipelineRepository
                    .findFirstByTipoNegocioAndAtivoTrueOrderByIdAsc(businessType)
                    .orElse(null);
            if (pipeline == null) {
                continue;
            }

            List<CrmStageEntity> allStages = stageRepository.findByPipelineIdOrderByOrdemAsc(pipeline.getId());
            if (allStages.isEmpty()) {
                continue;
            }
            Map<String, CrmStageEntity> stageMap = buildFunnelStageMap(allStages);
            CrmStageEntity fallback = allStages.get(0);
            CrmStageEntity defaultStage = resolveDefaultStage(stageMap, fallback);
            contexts.put(businessType, new PipelineSyncContext(pipeline, stageMap, defaultStage));
        }
        return contexts;
    }

    private String deriveOrderSituation(SalesHistorySyncProjection row) {
        String code = trim(row.getLastOrderStatusCode());
        if (code != null) {
            String normalized = code.toUpperCase();
            if ("AB".equals(normalized)) return "ABERTO";
            if ("FA".equals(normalized)) return "FATURADO";
            if ("CA".equals(normalized)) return "CANCELADO";
            if ("DV".equals(normalized)) return "DEVOLVIDO";
            if (normalized.contains("AB")) return "ABERTO";
            if (normalized.contains("FAT")) return "FATURADO";
            if (normalized.contains("CANC")) return "CANCELADO";
            if (normalized.contains("DEV")) return "DEVOLVIDO";
        }
        if (row.getLastCanceledDate() != null) return "CANCELADO";
        if (row.getLastReturnedDate() != null) return "DEVOLVIDO";
        if (row.getLastBilledDate() != null) return "FATURADO";
        return "ABERTO";
    }

    private String normalizeText(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase()
                .trim();
    }

    private Map<String, CrmStageEntity> buildFunnelStageMap(List<CrmStageEntity> stages) {
        Map<String, CrmStageEntity> map = new HashMap<>();
        for (CrmStageEntity stage : stages) {
            String name = normalizeText(stage.getNome());
            if (!map.containsKey("VENDA_FECHADA") && stage.isWon()) map.put("VENDA_FECHADA", stage);
            if (!map.containsKey("VENDA_PERDIDA") && stage.isLost()) map.put("VENDA_PERDIDA", stage);
            if (!map.containsKey("PROSPECCAO") && name.contains("prospec")) map.put("PROSPECCAO", stage);
            if (!map.containsKey("EM_NEGOCIACAO") && name.contains("negoci")) map.put("EM_NEGOCIACAO", stage);
            if (!map.containsKey("EM_NEGOCIACAO") && name.contains("proposta")) map.put("EM_NEGOCIACAO", stage);
            if (!map.containsKey("VENDA_FECHADA") && (name.contains("fechad") || name.contains("fatur") || name.contains("ganh"))) map.put("VENDA_FECHADA", stage);
            if (!map.containsKey("VENDA_PERDIDA") && (name.contains("perdid") || name.contains("cancel") || name.contains("devol"))) map.put("VENDA_PERDIDA", stage);
            if (!map.containsKey("RECORRENTE") && name.contains("recorren")) map.put("RECORRENTE", stage);
            if (!map.containsKey("RECOMPRA") && name.contains("recompra")) map.put("RECOMPRA", stage);
            if (!map.containsKey("RECOMPRA") && name.contains("recuper")) map.put("RECOMPRA", stage);
            if (!map.containsKey("RECORRENTE") && name.contains("fideliz")) map.put("RECORRENTE", stage);
        }
        return map;
    }

    private CrmStageEntity resolveDefaultStage(Map<String, CrmStageEntity> stageMap, CrmStageEntity fallback) {
        if (stageMap.containsKey("EM_NEGOCIACAO")) return stageMap.get("EM_NEGOCIACAO");
        if (stageMap.containsKey("PROSPECCAO")) return stageMap.get("PROSPECCAO");
        return fallback;
    }

    private CrmStageEntity resolveStageByBusinessRule(
            Map<String, CrmStageEntity> stageMap,
            String situacaoPedido,
            CrmStageEntity fallback
    ) {
        if ("FATURADO".equals(situacaoPedido)) {
            return stageMap.getOrDefault("VENDA_FECHADA", fallback);
        }
        if ("CANCELADO".equals(situacaoPedido) || "DEVOLVIDO".equals(situacaoPedido)) {
            return stageMap.getOrDefault("VENDA_PERDIDA", fallback);
        }
        if ("ABERTO".equals(situacaoPedido)) {
            return stageMap.getOrDefault("EM_NEGOCIACAO", stageMap.getOrDefault("PROSPECCAO", fallback));
        }
        return fallback;
    }

    private record PipelineSyncContext(
            CrmPipelineEntity pipeline,
            Map<String, CrmStageEntity> funnelStageMap,
            CrmStageEntity defaultStage
    ) {}

    private DealStatus mapCrmStatusByOrderSituation(String situacaoPedido) {
        if ("FATURADO".equals(situacaoPedido)) return DealStatus.GANHA;
        if ("CANCELADO".equals(situacaoPedido) || "DEVOLVIDO".equals(situacaoPedido)) return DealStatus.PERDIDA;
        return DealStatus.ABERTA;
    }

    private BigDecimal valueFromHistory(BigDecimal lastOrderValue, BigDecimal value90d, Integer orders90d, BigDecimal fallback) {
        if (lastOrderValue != null && lastOrderValue.compareTo(BigDecimal.ZERO) > 0) {
            return lastOrderValue;
        }
        int safeOrders = orders90d == null ? 0 : Math.max(orders90d, 0);
        if (value90d != null && value90d.compareTo(BigDecimal.ZERO) > 0 && safeOrders > 0) {
            return value90d.divide(BigDecimal.valueOf(safeOrders), 2, java.math.RoundingMode.HALF_UP);
        }
        return fallback;
    }
}
