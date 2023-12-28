package com.duty.manager.service.impl;

import com.duty.manager.dto.GetExecutionFactDTO;
import com.duty.manager.dto.RecordExecutionFactDTO;
import com.duty.manager.entity.ExecutionFact;
import com.duty.manager.entity.Role;
import com.duty.manager.repository.DutyRepository;
import com.duty.manager.repository.ExecutionFactRepository;
import com.duty.manager.repository.ParticipantRepository;
import com.duty.manager.service.ExecutionFactService;
import com.duty.manager.service.ServiceException;
import com.duty.manager.service.TimeService;
import jakarta.annotation.Nullable;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional
public class ExecutionFactServiceImpl implements ExecutionFactService {

    public static final int MAXIMAL_PAGE_SIZE = 200;

    private final ExecutionFactRepository executionFactRepository;

    private final TimeService timeService;

    private final ModelMapper modelMapper;

    private final DutyRepository dutyRepository;

    private final ParticipantRepository participantRepository;

    @PostConstruct
    private void configureModelMapper() {
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
    }

    @Override
    public List<GetExecutionFactDTO> getFinishedForDateRange(@NotNull LocalDateTime from, @Nullable LocalDateTime to) {
        return getExecutionFactDTOS(to, (notNullTo, pageSize) -> executionFactRepository
                .getAllFinishedInRange(from, notNullTo, pageSize)
        );
    }

    private List<GetExecutionFactDTO> getExecutionFactDTOS(LocalDateTime to, BiFunction<LocalDateTime, Pageable, List<ExecutionFact>> factSupplier) {
        if (to == null) {
            to = timeService.now();
        }
        return factSupplier.apply(to, PageRequest.ofSize(MAXIMAL_PAGE_SIZE)).stream()
                .map(this::mapEntityToGetDTO)
                .toList();
    }

    private GetExecutionFactDTO mapEntityToGetDTO(ExecutionFact fact) {
        GetExecutionFactDTO getDTO = modelMapper.map(fact, GetExecutionFactDTO.class);
        getDTO.getTestimonies().forEach(t -> t.setDutyName(getDTO.getDutyName()));
        return getDTO;
    }

    @Override
    public UUID recordExecutionFact(RecordExecutionFactDTO factDTO) {
        ExecutionFact fact = modelMapper.map(factDTO, ExecutionFact.class);
        fact.setStartTime(timeService.now());
        fact.setDuty(dutyRepository.getReferenceById(fact.getDuty().getId()));
        fact.setExecutor(participantRepository.getReferenceById(fact.getExecutor().getId()));
        return executionFactRepository.save(fact).getId();
    }

    @Override
    public List<GetExecutionFactDTO> getFinishedForDateRangeForParticipant(@NotNull LocalDateTime from,
                                                                           @Nullable LocalDateTime to,
                                                                           @NotNull UUID participantId) {
        return getExecutionFactDTOS(to, (notNullTo, pageSize) -> executionFactRepository
                .getAllFinishedInRangeForParticipant(from, notNullTo, participantId, pageSize)
        );
    }

    @Override
    public void finishExecution(UUID id, Authentication authentication) {
        ExecutionFact executionFact = getRawExecutionFact(id);
        if(!authentication.getName().equals(executionFact.getExecutor().getEmail()) &&
                !authentication.getAuthorities().contains(Role.ADMIN)) {
            throw new ServiceException("You are not allowed to finish this execution fact", HttpStatus.FORBIDDEN);
        }
        if(executionFact.getFinishTime() != null) {
            throw new ServiceException("Execution fact with id %s is already finished".formatted(id));
        }
        executionFact.setFinishTime(timeService.now());
        executionFactRepository.flush();
    }

    private ExecutionFact getRawExecutionFact(UUID id) {
        return executionFactRepository.findById(id).orElseThrow(notFound(id.toString()));
    }

    private Supplier<ServiceException> notFound(String identifier) {
        return () -> new ServiceException("Execution fact with id %s not found".formatted(identifier));
    }

    @Override
    public List<GetExecutionFactDTO> getActiveForDateRange(@NotNull LocalDateTime from, @Nullable LocalDateTime to) {
        return getExecutionFactDTOS(to, (notNullTo, pageSize) -> executionFactRepository
                .getAllActiveInRange(from, notNullTo, pageSize)
        );
    }

    @Override
    public List<GetExecutionFactDTO> getActiveForDateRangeForParticipant(@NotNull LocalDateTime from,
                                                                         @Nullable LocalDateTime to,
                                                                         @NotNull UUID participantId) {
        return getExecutionFactDTOS(to, (notNullTo, pageSize) -> executionFactRepository
                .getAllActiveInRangeForParticipant(from, notNullTo, participantId, pageSize)
        );
    }

    @Override
    public GetExecutionFactDTO getById(UUID id) {
        return mapEntityToGetDTO(getRawExecutionFact(id));
    }

}
