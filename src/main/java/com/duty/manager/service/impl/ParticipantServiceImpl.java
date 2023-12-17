package com.duty.manager.service.impl;

import com.duty.manager.dto.RegisterParticipantDTO;
import com.duty.manager.entity.Participant;
import com.duty.manager.repository.ParticipantRepository;
import com.duty.manager.service.ParticipantService;
import com.duty.manager.service.ServiceException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final ParticipantRepository participantRepository;

    private final ModelMapper modelMapper;

    private final PasswordEncoder passwordEncoder;

    @Override
    public UUID registerParticipant(RegisterParticipantDTO registerParticipantDTO) {
        throwExceptionIfEmailIsTaken(registerParticipantDTO);
        Participant newParticipant = modelMapper.map(registerParticipantDTO, Participant.class);
        newParticipant.setPassword(passwordEncoder.encode(newParticipant.getPassword()));
        return participantRepository.saveAndFlush(newParticipant).getId();
    }

    private void throwExceptionIfEmailIsTaken(RegisterParticipantDTO registerParticipantDTO) {
        if(participantRepository.findByEmail(registerParticipantDTO.getEmail()).isPresent()){
            throw new ServiceException("Email is already taken");
        }
    }

    @Override
    public Optional<Participant> getUserByIdentifier(String identifier) {
        return participantRepository.findByEmail(identifier);
    }
}
