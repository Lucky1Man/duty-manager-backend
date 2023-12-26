package com.duty.manager.service.impl;

import com.duty.manager.dto.GetTestimonyDTO;
import com.duty.manager.entity.Testimony;
import com.duty.manager.repository.TestimonyRepository;
import com.duty.manager.service.ExecutionFactService;
import com.duty.manager.service.ParticipantService;
import com.duty.manager.service.ServiceException;
import com.duty.manager.service.TestimonyService;
import com.duty.manager.service.TimeService;
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

@Service
@RequiredArgsConstructor
public class TestimonyServiceImpl implements TestimonyService {

    private final TimeService timeService;

    private final TestimonyRepository testimonyRepository;

    private final ParticipantService participantService;

    private final ExecutionFactService executionFactService;

    private final ModelMapper modelMapper;

    @Override
    public UUID testifyExecutionFact(@NotNull UUID executionFactId, @NotNull Authentication authentication) {
        UUID witnessId = participantService.getParticipant(authentication.getName()).getId();
        checkIfExist(executionFactId);
        throwIfPersonAlreadyTestifiedFact(executionFactId, witnessId);
        return testimonyRepository.save(
                Testimony.builder()
                        .withExecutionFactId(executionFactId)
                        .withWitnessId(witnessId)
                        .withTimestamp(timeService.now())
                        .build()
        ).getId();
    }

    private void checkIfExist(UUID executionFactId) {
        executionFactService.getById(executionFactId);
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
        getDTO.setWitnessFullName(participantService.getParticipant(getDTO.getWitnessId().toString()).getFullName());
        getDTO.setDutyName(executionFactService.getById(getDTO.getExecutionFactId()).getDutyName());
        return getDTO;
    }

}
