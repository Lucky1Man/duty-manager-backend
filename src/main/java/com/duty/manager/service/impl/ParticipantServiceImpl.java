package com.duty.manager.service.impl;

import com.duty.manager.dto.RegisterParticipantDTO;
import com.duty.manager.entity.Participant;
import com.duty.manager.repository.ParticipantRepository;
import com.duty.manager.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;

    private final ModelMapper modelMapper;

    @Override
    public UUID registerParticipant(RegisterParticipantDTO registerParticipantDTO) {
        return participantRepository.saveAndFlush(modelMapper.map(registerParticipantDTO, Participant.class)).getId();
    }

    @Override
    public Optional<Participant> getUserByIdentifier(String identifier) {
        return participantRepository.getByEmail(identifier);
    }
}
