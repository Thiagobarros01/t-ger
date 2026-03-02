package br.com.tger.api.crm.application.service;

import br.com.tger.api.crm.domain.DealStatus;
import br.com.tger.api.crm.dto.CloseDealLostRequestDto;
import br.com.tger.api.crm.dto.CreateDealRequestDto;
import br.com.tger.api.crm.dto.DealResponseDto;
import br.com.tger.api.crm.dto.MoveDealStageRequestDto;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.crm.infrastructure.entity.CrmDealEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmLossReasonEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmPipelineEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmStageEntity;
import br.com.tger.api.crm.infrastructure.repository.CrmDealRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmLossReasonRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmPipelineRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmStageRepository;
import br.com.tger.api.persistence.entity.CompanyEntity;
import br.com.tger.api.persistence.entity.CustomerEntity;
import br.com.tger.api.persistence.entity.SellerEntity;
import br.com.tger.api.persistence.repository.CompanyRepository;
import br.com.tger.api.persistence.repository.CustomerRepository;
import br.com.tger.api.persistence.repository.SellerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CrmDealService {
    private final CrmDealRepository dealRepository;
    private final CrmPipelineRepository pipelineRepository;
    private final CrmStageRepository stageRepository;
    private final CrmLossReasonRepository lossReasonRepository;
    private final CustomerRepository customerRepository;
    private final CompanyRepository companyRepository;
    private final SellerRepository sellerRepository;
    private final CrmAccessService accessService;

    public CrmDealService(
            CrmDealRepository dealRepository,
            CrmPipelineRepository pipelineRepository,
            CrmStageRepository stageRepository,
            CrmLossReasonRepository lossReasonRepository,
            CustomerRepository customerRepository,
            CompanyRepository companyRepository,
            SellerRepository sellerRepository,
            CrmAccessService accessService
    ) {
        this.dealRepository = dealRepository;
        this.pipelineRepository = pipelineRepository;
        this.stageRepository = stageRepository;
        this.lossReasonRepository = lossReasonRepository;
        this.customerRepository = customerRepository;
        this.companyRepository = companyRepository;
        this.sellerRepository = sellerRepository;
        this.accessService = accessService;
    }

    public List<DealResponseDto> listDeals(String authorizationHeader) {
        UserDto user = accessService.resolveUser(authorizationHeader);
        if (accessService.isOperator(user)) {
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
        entity.setPipeline(pipeline);
        entity.setStage(stage);
        entity.setValorEstimado(request.valorEstimado());
        entity.setProbabilidade(request.probabilidade());
        entity.setDataPrevistaFechamento(request.dataPrevistaFechamento());
        entity.setStatus(DealStatus.ABERTA);

        applyStageStatusRules(entity, stage, request.motivoPerdaId());
        return toDto(dealRepository.save(entity));
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
        return toDto(dealRepository.save(deal));
    }

    @Transactional
    public DealResponseDto closeAsWon(Long dealId) {
        CrmDealEntity deal = dealRepository.findById(dealId).orElseThrow();
        deal.setStatus(DealStatus.GANHA);
        deal.setMotivoPerda(null);
        return toDto(dealRepository.save(deal));
    }

    @Transactional
    public DealResponseDto closeAsLost(Long dealId, CloseDealLostRequestDto request) {
        CrmDealEntity deal = dealRepository.findById(dealId).orElseThrow();
        CrmLossReasonEntity motivo = resolveLossReasonRequired(request.motivoPerdaId());
        deal.setStatus(DealStatus.PERDIDA);
        deal.setMotivoPerda(motivo);
        return toDto(dealRepository.save(deal));
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
                e.getPipeline().getId(),
                e.getStage().getId(),
                e.getValorEstimado(),
                e.getProbabilidade(),
                e.getStatus(),
                e.getDataPrevistaFechamento(),
                e.getMotivoPerda() != null ? e.getMotivoPerda().getId() : null,
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
