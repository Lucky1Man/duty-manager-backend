package com.duty.manager.controller;

import com.duty.manager.dto.RegisterParticipantDTO;
import com.duty.manager.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    @PostMapping
    public UUID register(@RequestBody RegisterParticipantDTO participantDTO) {
        return participantService.registerParticipant(participantDTO);
    }

}
