package com.duty.manager.controller;

import com.duty.manager.dto.GetExecutionFactDTO;
import com.duty.manager.dto.GetTestimonyDTO;
import com.duty.manager.dto.RecordExecutionFactDTO;
import com.duty.manager.service.ExecutionFactService;
import com.duty.manager.service.TestimonyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/execution-facts")
public class ExecutionFactController {

    private final ExecutionFactService executionFactService;

    private final TestimonyService testifyExecutionFact;

    @GetMapping(path = "/finished", params = {"from"})
    @Operation(description = "Returns execution facts based on specified date range, \"finish date\" is used for search")
    @ApiResponse(
            responseCode = "200",
            description = "Default value for \"to\" is current day. The limit for maximal list size is 200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(
                            schema = @Schema(implementation = GetExecutionFactDTO.class)
                    )
            )
    )
    public List<GetExecutionFactDTO> getFinishedForDateRange(@RequestParam LocalDateTime from,
                                                             @RequestParam(required = false) LocalDateTime to) {
        return executionFactService.getFinishedForDateRange(from, to);
    }

    @PostMapping
    @Operation(
            description = "Records given execution fact."
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(
            responseCode = "201",
            description = "Returns id of execution fact that was recorded",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UUID.class)
            )
    )
    public ResponseEntity<UUID> recordExecutionFact(@RequestBody RecordExecutionFactDTO factDTO) {
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(executionFactService.recordExecutionFact(factDTO));
    }

    @PostMapping("/finished")
    @Operation(
            description = "Sets finish time for execution fact with given id to current time."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Finish time was set successfully"
    )
    public void finishExecution(@RequestBody UUID executionFactId) {
        executionFactService.finishExecution(executionFactId);
    }

    @PostMapping("/{executionFactId}/testimonies")
    @Operation(
            description = "Authenticated person testifies execution fact."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Execution fact was successfully testified. It returns id of created testimony",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UUID.class)
            )
    )
    public ResponseEntity<UUID> testifyExecutionFact(@PathVariable UUID executionFactId, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED.value())
                .body(testifyExecutionFact.testifyExecutionFact(executionFactId, authentication));
    }

    @GetMapping("/{executionFactId}/testimonies")
    @Operation(description = "Returns testimonies for specified execution fact.")
    @ApiResponse(
            responseCode = "200",
            description = "Returns testimonies for specified execution fact. Default value for \"page\" is 0," +
                    " \"pageSize\" is 200. Min value for page is 0, Max value for page size is 200.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(
                            schema = @Schema(implementation = GetTestimonyDTO.class)
                    )
            )
    )
    public List<GetTestimonyDTO> getTestimoniesForExecutionFact(@PathVariable UUID executionFactId,
                                                                @RequestParam(required = false, defaultValue = "0")
                                                                Integer page,
                                                                @RequestParam(required = false, defaultValue = "200")
                                                                Integer pageSize) {
        return testifyExecutionFact.getTestimoniesForExecutionFact(executionFactId, page, pageSize);
    }

    @GetMapping(path = "/finished", params = {"from", "executorId"})
    @Operation(description = "Returns execution facts based on specified date range and for specific executor," +
            " \"finish date\" is used for search")
    @ApiResponse(
            responseCode = "200",
            description = "Default value for \"to\" is current day. The limit for maximal list size is 200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(
                            schema = @Schema(implementation = GetExecutionFactDTO.class)
                    )
            )
    )
    public List<GetExecutionFactDTO> getFinishedForDateRangeForExecutor(@RequestParam LocalDateTime from,
                                                                        @RequestParam(required = false) LocalDateTime to,
                                                                        @RequestParam UUID executorId) {
        return executionFactService.getFinishedForDateRangeForParticipant(from, to, executorId);
    }

    @GetMapping(path = "/active", params = {"from"})
    @Operation(description = "Returns execution facts based on specified date range," +
            " \"start date\" is used for search, ignores all records that have \"finish time\"")
    @ApiResponse(
            responseCode = "200",
            description = "Default value for \"to\" is current day. The limit for maximal list size is 200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(
                            schema = @Schema(implementation = GetExecutionFactDTO.class)
                    )
            )
    )
    public List<GetExecutionFactDTO> getActiveForDateRange(@RequestParam LocalDateTime from,
                                                           @RequestParam(required = false) LocalDateTime to) {
        return executionFactService.getActiveForDateRange(from, to);
    }

    @GetMapping(path = "/active", params = {"from", "executorId"})
    @Operation(description = "Returns execution facts based on specified date range and for specific executor," +
            " \"start date\" is used for search, ignores all records that have \"finish time\"")
    @ApiResponse(
            responseCode = "200",
            description = "Default value for \"to\" is current day. The limit for maximal list size is 200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(
                            schema = @Schema(implementation = GetExecutionFactDTO.class)
                    )
            )
    )
    public List<GetExecutionFactDTO> getActiveForDateRangeForExecutor(@RequestParam LocalDateTime from,
                                                                      @RequestParam(required = false) LocalDateTime to,
                                                                      @RequestParam UUID executorId) {
        return executionFactService.getActiveForDateRangeForParticipant(from, to, executorId);
    }


}
