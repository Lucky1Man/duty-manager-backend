package com.duty.manager.service.impl;

import com.duty.manager.dto.GetParticipantDTO;
import com.duty.manager.dto.RegisterParticipantDTO;
import com.duty.manager.entity.Participant;
import com.duty.manager.entity.Role;
import com.duty.manager.repository.ParticipantRepository;
import com.duty.manager.repository.RoleRepository;
import com.duty.manager.service.ParticipantService;
import com.duty.manager.service.ServiceException;
import com.duty.manager.service.UserDetailsProvider;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantServiceImpl implements ParticipantService, UserDetailsProvider<Participant> {

    private final ParticipantRepository participantRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public UUID registerParticipant(RegisterParticipantDTO registerParticipantDTO) {
        throwExceptionIfEmailIsTaken(registerParticipantDTO);
        Participant newParticipant = modelMapper.map(registerParticipantDTO, Participant.class);
        newParticipant.setPassword(passwordEncoder.encode(newParticipant.getPassword()));
        newParticipant.setRole(roleRepository.findByName(Role.PARTICIPANT.getName())
                .orElseThrow(()->new ServiceException("Roles are not configured in the database")));
        return participantRepository.saveAndFlush(newParticipant).getId();
    }

    private void throwExceptionIfEmailIsTaken(RegisterParticipantDTO registerParticipantDTO) {
        if (participantRepository.findByEmail(registerParticipantDTO.getEmail()).isPresent()) {
            throw new ServiceException("Email is already taken");
        }
    }

    @Override
    public GetParticipantDTO getParticipant(String identifier) {
        try {
            UUID id = UUID.fromString(identifier);
            return mapToGetDTO(participantRepository.findById(id).orElseThrow(notFound(identifier)));
        } catch (IllegalArgumentException e) {
            return mapToGetDTO(participantRepository.findByEmail(identifier).orElseThrow(notFound(identifier)));
        }
    }

    private GetParticipantDTO mapToGetDTO(Participant participant) {
        return modelMapper.map(participant, GetParticipantDTO.class);
    }

    private Supplier<ServiceException> notFound(String identifier) {
        return () -> new ServiceException("Participant with identifier %s not found".formatted(identifier));
    }

    @Override
    public List<GetParticipantDTO> getParticipants(@Min(0) @NotNull Integer page, @Max(200) @NotNull Integer pageSize) {
        return participantRepository.findAll(PageRequest.of(page, pageSize)).stream().map(this::mapToGetDTO).toList();
    }

    @Override
    public Optional<Participant> getUserByIdentifier(String identifier) {
        return participantRepository.findByEmail(identifier);
    }
}
