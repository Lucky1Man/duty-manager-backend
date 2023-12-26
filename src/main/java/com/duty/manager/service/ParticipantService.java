package com.duty.manager.service;

import com.duty.manager.dto.GetParticipantDTO;
import com.duty.manager.dto.RegisterParticipantDTO;
import com.duty.manager.entity.Participant;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Validated
public interface ParticipantService extends UserDetailsProvider<Participant> {

    UUID registerParticipant(@Valid RegisterParticipantDTO registerParticipantDTO);

    GetParticipantDTO getParticipant(String identifier);

    List<GetParticipantDTO> getParticipants(@Min(0) @NotNull Integer page, @Max(200) @NotNull Integer pageSize);
}
