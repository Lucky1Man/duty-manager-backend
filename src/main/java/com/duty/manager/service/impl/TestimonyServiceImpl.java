package com.duty.manager.service.impl;

import com.duty.manager.dto.GetTestimonyDTO;
import com.duty.manager.entity.Testimony;
import com.duty.manager.repository.ExecutionFactRepository;
import com.duty.manager.repository.ParticipantRepository;
import com.duty.manager.repository.TestimonyRepository;
import com.duty.manager.service.ServiceException;
import com.duty.manager.service.TestimonyService;
import com.duty.manager.service.TimeService;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional
public class TestimonyServiceImpl implements TestimonyService {

    private final TimeService timeService;

    private final TestimonyRepository testimonyRepository;

    private final ParticipantRepository participantRepository;

    private final ExecutionFactRepository executionFactRepository;

    private final ModelMapper modelMapper;

    @Override
    public UUID testifyExecutionFact(@NotNull UUID executionFactId, @NotNull Authentication authentication) {
        UUID witnessId = participantRepository.findByEmail(authentication.getName())
                .orElseThrow(notFound(authentication.getName())).getId();
        checkIfExist(executionFactId);
        throwIfPersonAlreadyTestifiedFact(executionFactId, witnessId);
        return testimonyRepository.save(
                Testimony.builder()
                        .withExecutionFact(executionFactRepository.getReferenceById(executionFactId))
                        .withWitness(participantRepository.getReferenceById(witnessId))
                        .withTimestamp(timeService.now())
                        .build()
        ).getId();
    }

    private Supplier<ServiceException> notFound(String identifier) {
        return () -> new ServiceException("Participant with email %s not found".formatted(identifier));
    }

    private void checkIfExist(UUID executionFactId) {
        executionFactRepository.getReferenceById(executionFactId);
    }

    private void throwIfPersonAlreadyTestifiedFact(UUID executionFactId, UUID witnessId) {
        testimonyRepository.findByExecutionFactIdAndWitnessId(executionFactId, witnessId)
                .ifPresent(t -> {
                    throw new ServiceException("Execution fact with id %s was already witnesses by %s"
                            .formatted(executionFactId, witnessId));
                });
    }

    @Override
    public List<GetTestimonyDTO> getTestimoniesForExecutionFact(@NotNull UUID id, @Min(0) @NotNull Integer page,
                                                                @Max(200) @NotNull Integer pageSize) {
        return testimonyRepository.findAllByExecutionFactId(id, PageRequest.of(page, pageSize)).stream()
                .map(this::mapToGetDTO)
                .toList();
    }

    private GetTestimonyDTO mapToGetDTO(Testimony t) {
        GetTestimonyDTO getDTO = modelMapper.map(t, GetTestimonyDTO.class);
        getDTO.setDutyName(t.getExecutionFact().getDuty().getName());
        return getDTO;
    }

}
