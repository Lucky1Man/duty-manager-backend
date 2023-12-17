package com.duty.manager.controller;

import com.duty.manager.dto.CreateDutyDTO;
import com.duty.manager.dto.GetDutyDTO;
import com.duty.manager.dto.UpdateDutyDTO;
import com.duty.manager.service.DutyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/duties")
public class DutyController {

    private final DutyService dutyService;

    @GetMapping
    @Operation(description = "Returns duties at specified page with specified page size. Page count starts from 0.")
    @ApiResponse(
            responseCode = "200",
            description = "Returns duties that are at specified page with specified page size",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(
                            schema = @Schema(implementation = GetDutyDTO.class)
                    )
            )
    )
    public List<GetDutyDTO> getDuty(@RequestParam(required = false, defaultValue = "0")
                                   Integer page,
                                   @RequestParam(required = false, defaultValue = "50")
                                   Integer pageSize) {
        return dutyService.getDuties(page, pageSize);
    }

    @GetMapping("/{dutyIdentifier}")
    @Operation(description = "Returns duty by specified id or name. Returns 404 if no duty was found")
    @ApiResponse(
            responseCode = "200",
            description = "Returns duty with specified duty id or duty name",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetDutyDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Duty with specified duty id or duty name was not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponse.class)
            )
    )
    public GetDutyDTO getDuty(@PathVariable String dutyIdentifier) {
        return dutyService.getDuty(dutyIdentifier);
    }

    @PostMapping
    @Operation(
            description = "Adds given duty to database. Returns 400 if duty with given name already exists"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(
            responseCode = "201",
            description = "Returns id of duty that was added",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UUID.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Means that name already exist",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponse.class)
            )
    )
    public ResponseEntity<UUID> addDuty(@RequestBody CreateDutyDTO addDutyDto) {
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(dutyService.createDuty(addDutyDto));
    }

    @PutMapping("/{dutyIdentifier}")
    @Operation(
            description = "Updates duty with given identifier with data from UpdateDutyDTO." +
                    " If UpdateDutyDTO has null fields then that specific field will not be effected"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Means that duty was updated and all given parameters were changed"
    )
    public void updateDuty(@PathVariable String dutyIdentifier, @RequestBody UpdateDutyDTO updateDutyDTO) {
        dutyService.updateDuty(dutyIdentifier, updateDutyDTO);
    }

    @DeleteMapping("/{dutyIdentifier}")
    @Operation(
            description = "Deletes duty with given identifier"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Means that duty was deleted"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Means that duty with given identifier does not exist",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponse.class)
            )
    )
    public void deleteDuty(@PathVariable String dutyIdentifier) {
        dutyService.deleteDuty(dutyIdentifier);
    }

}
