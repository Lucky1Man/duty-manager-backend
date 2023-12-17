package com.duty.manager.service;

import com.duty.manager.dto.RegisterParticipantDTO;
import com.duty.manager.entity.Participant;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Validated
public interface ParticipantService extends UserDetailsProvider<Participant> {

    UUID registerParticipant(@Valid RegisterParticipantDTO registerParticipantDTO);

}
