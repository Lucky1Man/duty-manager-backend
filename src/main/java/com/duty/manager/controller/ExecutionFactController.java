package com.duty.manager.controller;

import com.duty.manager.dto.GetExecutionFactDTO;
import com.duty.manager.dto.GetTestimonyDTO;
import com.duty.manager.dto.RecordExecutionFactDTO;
import com.duty.manager.service.ExecutionFactService;
import com.duty.manager.service.TestimonyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

//TODO describe all endpoints with open api
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/execution-facts")
public class ExecutionFactController {

    private final ExecutionFactService executionFactService;

    private final TestimonyService testifyExecutionFact;

    @GetMapping(path = "/finished", params = {"from"})
    public List<GetExecutionFactDTO> getFinishedForDateRange(@RequestParam LocalDateTime from,
                                                             @RequestParam(required = false) LocalDateTime to) {
        return executionFactService.getFinishedForDateRange(from, to);
    }

    @PostMapping
    public UUID recordExecutionFact(@RequestBody RecordExecutionFactDTO factDTO) {
        return executionFactService.recordExecutionFact(factDTO);
    }

    @PostMapping("/{executionFactId}")
    public void finishExecution(@PathVariable UUID executionFactId) {
        executionFactService.finishExecution(executionFactId);
    }

    @PostMapping("/{executionFactId}/testimonies")
    public UUID testifyExecutionFact(@PathVariable UUID executionFactId, Authentication authentication) {
        return testifyExecutionFact.testifyExecutionFact(executionFactId, authentication);
    }

    @GetMapping("/{executionFactId}/testimonies")
    public List<GetTestimonyDTO> getTestimoniesForExecutionFact(@PathVariable UUID executionFactId,
                                                                @RequestParam(required = false, defaultValue = "0")
                                                                Integer page,
                                                                @RequestParam(required = false, defaultValue = "200")
                                                                Integer pageSize) {
        return testifyExecutionFact.getTestimoniesForExecutionFact(executionFactId, page, pageSize);
    }

    @GetMapping(path = "/finished", params = {"from", "executorId"})
    public List<GetExecutionFactDTO> getFinishedForDateRangeForExecutor(@RequestParam LocalDateTime from,
                                                                        @RequestParam(required = false) LocalDateTime to,
                                                                        @RequestParam UUID executorId) {
        return executionFactService.getFinishedForDateRangeForParticipant(from, to, executorId);
    }

    @GetMapping(path = "/active", params = {"from"})
    public List<GetExecutionFactDTO> getActiveForDateRange(@RequestParam LocalDateTime from,
                                                           @RequestParam(required = false) LocalDateTime to) {
        return executionFactService.getActiveForDateRange(from, to);
    }

    @GetMapping(path = "/active", params = {"from", "executorId"})
    public List<GetExecutionFactDTO> getActiveForDateRangeForExecutor(@RequestParam LocalDateTime from,
                                                                      @RequestParam(required = false) LocalDateTime to,
                                                                      @RequestParam UUID executorId) {
        return executionFactService.getActiveForDateRangeForParticipant(from, to, executorId);
    }


}
