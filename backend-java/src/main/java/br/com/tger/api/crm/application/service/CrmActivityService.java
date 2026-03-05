package br.com.tger.api.crm.application.service;

import br.com.tger.api.crm.dto.CreateInteractionRequestDto;
import br.com.tger.api.crm.dto.CreateTaskRequestDto;
import br.com.tger.api.crm.dto.InteractionResponseDto;
import br.com.tger.api.crm.dto.TaskResponseDto;
import br.com.tger.api.crm.dto.TaskStatusHistoryResponseDto;
import br.com.tger.api.crm.domain.TaskStatus;
import br.com.tger.api.dto.UserDto;
import br.com.tger.api.crm.infrastructure.entity.CrmDealEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmInteractionEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmTaskEntity;
import br.com.tger.api.crm.infrastructure.entity.CrmTaskStatusHistoryEntity;
import br.com.tger.api.crm.infrastructure.repository.CrmDealRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmInteractionRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmTaskRepository;
import br.com.tger.api.crm.infrastructure.repository.CrmTaskStatusHistoryRepository;
import br.com.tger.api.persistence.entity.AppUserEntity;
import br.com.tger.api.persistence.entity.CustomerEntity;
import br.com.tger.api.persistence.repository.AppUserRepository;
import br.com.tger.api.persistence.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CrmActivityService {
    private final CrmInteractionRepository interactionRepository;
    private final CrmTaskRepository taskRepository;
    private final CrmDealRepository dealRepository;
    private final CustomerRepository customerRepository;
    private final AppUserRepository appUserRepository;
    private final CrmTaskStatusHistoryRepository taskStatusHistoryRepository;
    private final CrmAccessService accessService;

    public CrmActivityService(
            CrmInteractionRepository interactionRepository,
            CrmTaskRepository taskRepository,
            CrmDealRepository dealRepository,
            CustomerRepository customerRepository,
            AppUserRepository appUserRepository,
            CrmTaskStatusHistoryRepository taskStatusHistoryRepository,
            CrmAccessService accessService
    ) {
        this.interactionRepository = interactionRepository;
        this.taskRepository = taskRepository;
        this.dealRepository = dealRepository;
        this.customerRepository = customerRepository;
        this.appUserRepository = appUserRepository;
        this.taskStatusHistoryRepository = taskStatusHistoryRepository;
        this.accessService = accessService;
    }

    public List<InteractionResponseDto> listInteractions(String authorizationHeader) {
        UserDto user = accessService.resolveUser(authorizationHeader);
        if (accessService.isOperator(user)) {
            java.util.Set<Long> operatorDealIds = resolveOperatorDeals(user).stream()
                    .map(CrmDealEntity::getId)
                    .collect(java.util.stream.Collectors.toSet());
            return interactionRepository.findAll().stream()
                    .filter(i -> i.getCriadoPor().getId().equals(user.id()) || (i.getDeal() != null && operatorDealIds.contains(i.getDeal().getId())))
                    .map(this::toDto)
                    .toList();
        }
        return interactionRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public InteractionResponseDto createInteraction(CreateInteractionRequestDto request) {
        CustomerEntity cliente = customerRepository.findById(request.clienteId()).orElseThrow();
        CrmDealEntity deal = resolveDeal(request.dealId());
        AppUserEntity criadoPor = appUserRepository.findById(request.criadoPor()).orElseThrow();

        CrmInteractionEntity entity = new CrmInteractionEntity();
        entity.setCliente(cliente);
        entity.setDeal(deal);
        entity.setTipo(request.tipo());
        entity.setDescricao(request.descricao().trim());
        entity.setOcorridoEm(request.ocorridoEm());
        entity.setCriadoPor(criadoPor);
        return toDto(interactionRepository.save(entity));
    }

    public List<TaskResponseDto> listTasks(String authorizationHeader) {
        UserDto user = accessService.resolveUser(authorizationHeader);
        if (accessService.isOperator(user)) {
            return taskRepository.findByResponsavelId(user.id()).stream().map(this::toDto).toList();
        }
        return taskRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public TaskResponseDto createTask(CreateTaskRequestDto request) {
        CustomerEntity cliente = customerRepository.findById(request.clienteId()).orElseThrow();
        CrmDealEntity deal = resolveDeal(request.dealId());
        AppUserEntity responsavel = appUserRepository.findById(request.responsavelId()).orElseThrow();

        CrmTaskEntity entity = new CrmTaskEntity();
        entity.setCliente(cliente);
        entity.setDeal(deal);
        entity.setTitulo(request.titulo().trim());
        entity.setDescricao(request.descricao() == null ? null : request.descricao().trim());
        entity.setPrioridade(request.prioridade());
        entity.setStatus(request.status() == null ? TaskStatus.PENDENTE : request.status());
        entity.setVencimentoEm(request.vencimentoEm());
        entity.setResponsavel(responsavel);
        CrmTaskEntity saved = taskRepository.save(entity);
        createStatusHistory(saved, null, saved.getStatus(), responsavel.getId(), responsavel.getName(), "Cadastro da tarefa");
        return toDto(saved);
    }

    @Transactional
    public TaskResponseDto updateTaskStatus(Long taskId, TaskStatus status, String authorizationHeader) {
        CrmTaskEntity task = taskRepository.findById(taskId).orElseThrow();
        UserDto user = accessService.resolveUser(authorizationHeader);
        if (accessService.isOperator(user) && !task.getResponsavel().getId().equals(user.id())) {
            throw new IllegalArgumentException("Operador pode alterar apenas as proprias tarefas.");
        }
        if (status == null) throw new IllegalArgumentException("Status obrigatorio.");
        TaskStatus previous = task.getStatus();
        if (previous == status) {
            return toDto(task);
        }
        task.setStatus(status);
        CrmTaskEntity saved = taskRepository.save(task);
        createStatusHistory(saved, previous, status, user.id(), user.name(), "Alteracao de status");
        return toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<TaskStatusHistoryResponseDto> listTaskStatusHistory(Long taskId, String authorizationHeader) {
        CrmTaskEntity task = taskRepository.findById(taskId).orElseThrow();
        UserDto user = accessService.resolveUser(authorizationHeader);
        if (accessService.isOperator(user) && !task.getResponsavel().getId().equals(user.id())) {
            throw new IllegalArgumentException("Operador pode visualizar apenas o historico das proprias tarefas.");
        }
        return taskStatusHistoryRepository.findByTaskIdOrderByChangedAtDesc(taskId).stream()
                .map(this::toDto)
                .toList();
    }

    private CrmDealEntity resolveDeal(Long dealId) {
        if (dealId == null) return null;
        return dealRepository.findById(dealId).orElseThrow();
    }

    private List<CrmDealEntity> resolveOperatorDeals(UserDto user) {
        if (user == null) return List.of();
        if (user.linkedSellerErpCode() != null && !user.linkedSellerErpCode().isBlank()) {
            return dealRepository.findByVendedorErpCodeIgnoreCase(user.linkedSellerErpCode());
        }
        return dealRepository.findByVendedorEmailIgnoreCase(user.email());
    }

    private InteractionResponseDto toDto(CrmInteractionEntity e) {
        return new InteractionResponseDto(
                e.getId(),
                e.getCliente().getId(),
                e.getDeal() != null ? e.getDeal().getId() : null,
                e.getTipo(),
                e.getDescricao(),
                e.getOcorridoEm(),
                e.getCriadoPor().getId(),
                e.getCreatedAt()
        );
    }

    private TaskResponseDto toDto(CrmTaskEntity e) {
        return new TaskResponseDto(
                e.getId(),
                e.getCliente().getId(),
                e.getDeal() != null ? e.getDeal().getId() : null,
                e.getTitulo(),
                e.getDescricao(),
                e.getPrioridade(),
                e.getStatus(),
                e.getVencimentoEm(),
                e.getResponsavel().getId(),
                e.getCreatedAt()
        );
    }

    private TaskStatusHistoryResponseDto toDto(CrmTaskStatusHistoryEntity e) {
        return new TaskStatusHistoryResponseDto(
                e.getId(),
                e.getTask().getId(),
                e.getPreviousStatus(),
                e.getNewStatus(),
                e.getChangedByUserId(),
                e.getChangedByName(),
                e.getChangedAt(),
                e.getNote()
        );
    }

    private void createStatusHistory(
            CrmTaskEntity task,
            TaskStatus previous,
            TaskStatus next,
            Long changedByUserId,
            String changedByName,
            String note
    ) {
        CrmTaskStatusHistoryEntity event = new CrmTaskStatusHistoryEntity();
        event.setTask(task);
        event.setPreviousStatus(previous);
        event.setNewStatus(next);
        event.setChangedByUserId(changedByUserId);
        event.setChangedByName(changedByName == null || changedByName.isBlank() ? "Sistema" : changedByName);
        event.setNote(note);
        taskStatusHistoryRepository.save(event);
    }
}
