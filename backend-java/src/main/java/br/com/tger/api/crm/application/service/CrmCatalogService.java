package br.com.tger.api.crm.application.service;

import br.com.tger.api.crm.dto.*;
import br.com.tger.api.crm.infrastructure.entity.CrmLossReasonEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmPipelineEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmStageEntity;
import br.com.tger.api.crm.infrastructure.repository.CrmDealRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmLossReasonRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmPipelineRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmStageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CrmCatalogService {
    private final CrmPipelineRepository pipelineRepository;
    private final CrmStageRepository stageRepository;
    private final CrmLossReasonRepository lossReasonRepository;
    private final CrmDealRepository dealRepository;

    public CrmCatalogService(
            CrmPipelineRepository pipelineRepository,
            CrmStageRepository stageRepository,
            CrmLossReasonRepository lossReasonRepository,
            CrmDealRepository dealRepository
    ) {
        this.pipelineRepository = pipelineRepository;
        this.stageRepository = stageRepository;
        this.lossReasonRepository = lossReasonRepository;
        this.dealRepository = dealRepository;
    }

    public List<PipelineResponseDto> listPipelines() {
        return pipelineRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public PipelineResponseDto createPipeline(CreatePipelineRequestDto request) {
        CrmPipelineEntity entity = new CrmPipelineEntity();
        entity.setNome(request.nome().trim());
        entity.setTipoNegocio(request.tipoNegocio());
        entity.setAtivo(request.ativo());
        return toDto(pipelineRepository.save(entity));
    }

    public List<StageResponseDto> listStagesByPipeline(Long pipelineId) {
        return stageRepository.findByPipelineIdOrderByOrdemAsc(pipelineId).stream().map(this::toDto).toList();
    }

    @Transactional
    public StageResponseDto createStage(CreateStageRequestDto request) {
        CrmPipelineEntity pipeline = pipelineRepository.findById(request.pipelineId()).orElseThrow();
        validateStageFlags(request.isWon(), request.isLost());
        CrmStageEntity entity = new CrmStageEntity();
        entity.setPipeline(pipeline);
        entity.setNome(request.nome().trim());
        entity.setOrdem(request.ordem());
        entity.setWon(request.isWon());
        entity.setLost(request.isLost());
        return toDto(stageRepository.save(entity));
    }

    @Transactional
    public StageResponseDto updateStage(Long stageId, UpdateStageRequestDto request) {
        CrmStageEntity entity = stageRepository.findById(stageId).orElseThrow();
        validateStageFlags(request.isWon(), request.isLost());
        entity.setNome(request.nome().trim());
        entity.setOrdem(request.ordem());
        entity.setWon(request.isWon());
        entity.setLost(request.isLost());
        return toDto(stageRepository.save(entity));
    }

    @Transactional
    public void deleteStage(Long stageId) {
        long deals = dealRepository.countByStageId(stageId);
        if (deals > 0) {
            throw new IllegalArgumentException("Nao e possivel remover etapa com oportunidades vinculadas.");
        }
        stageRepository.deleteById(stageId);
    }

    public List<LossReasonResponseDto> listLossReasons() {
        return lossReasonRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public LossReasonResponseDto createLossReason(CreateLossReasonRequestDto request) {
        CrmLossReasonEntity entity = new CrmLossReasonEntity();
        entity.setDescricao(request.descricao().trim());
        entity.setAtivo(request.ativo());
        return toDto(lossReasonRepository.save(entity));
    }

    private PipelineResponseDto toDto(CrmPipelineEntity e) {
        return new PipelineResponseDto(e.getId(), e.getNome(), e.getTipoNegocio(), e.isAtivo());
    }

    private StageResponseDto toDto(CrmStageEntity e) {
        return new StageResponseDto(e.getId(), e.getPipeline().getId(), e.getNome(), e.getOrdem(), e.isWon(), e.isLost());
    }

    private LossReasonResponseDto toDto(CrmLossReasonEntity e) {
        return new LossReasonResponseDto(e.getId(), e.getDescricao(), e.isAtivo());
    }

    private void validateStageFlags(boolean isWon, boolean isLost) {
        if (isWon && isLost) {
            throw new IllegalArgumentException("Etapa nao pode ser GANHA e PERDIDA ao mesmo tempo.");
        }
    }
}
