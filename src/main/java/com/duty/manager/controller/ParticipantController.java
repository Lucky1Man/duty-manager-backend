package com.duty.manager.controller;

import com.duty.manager.dto.GetDutyDTO;
import com.duty.manager.dto.GetParticipantDTO;
import com.duty.manager.dto.RegisterParticipantDTO;
import com.duty.manager.service.ParticipantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/participants")
public class ParticipantController {

    private final ParticipantService participantService;

    @PostMapping
    @Operation(
            description = "Register participant."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Participant was successfully registered. It returns id of created participant",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UUID.class)
            )
    )
    public ResponseEntity<UUID> register(@RequestBody RegisterParticipantDTO participantDTO) {
        return ResponseEntity.status(HttpStatus.CREATED.value())
                .body( participantService.registerParticipant(participantDTO));
    }

    @GetMapping("/{identifier}")
    @Operation(description = "Returns participant by specified id or email. Returns 404 if no participant was found")
    @ApiResponse(
            responseCode = "200",
            description = "Returns participant with specified id or email",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetDutyDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Participant with specified id or email was not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponse.class)
            )
    )
    public GetParticipantDTO getParticipant(@PathVariable String identifier) {
        return participantService.getParticipant(identifier);
    }

    @GetMapping
    @Operation(description = "Returns list of registered participants")
    @ApiResponse(
            responseCode = "200",
            description = "You are allowed to get this list, defaults: page = 0, pageSize = 200," +
                    " restrictions: page min = 0, pageSize max = 200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(
                            schema = @Schema(implementation = GetParticipantDTO.class)
                    )
            )
    )
    public List<GetParticipantDTO> getParticipants(@RequestParam(required = false, defaultValue = "0")
                                                   Integer page,
                                                   @RequestParam(required = false, defaultValue = "200")
                                                   Integer pageSize) {
        return participantService.getParticipants(page, pageSize);
    }

}
