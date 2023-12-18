package com.duty.manager.controller;

import com.duty.manager.dto.GetExecutionFactDTO;
import com.duty.manager.dto.RecordExecutionFactDTO;
import com.duty.manager.service.ExecutionFactService;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/execution-facts")
public class ExecutionFactController {

    private final ExecutionFactService executionFactService;

    @GetMapping(params = {"from", "to"})
    public List<GetExecutionFactDTO> getForDateRange(@RequestParam LocalDateTime from,
                                                     @RequestParam(required = false) LocalDateTime to) {
        return executionFactService.getForDateRange(from, to);
    }

    @PostMapping
    public UUID recordExecutionFact(@RequestBody RecordExecutionFactDTO factDTO) {
        return executionFactService.recordExecutionFact(factDTO);
    }

    @PostMapping("/{executionFactId}")
    public void testifyExecutionFact(@PathVariable UUID executionFactId, Authentication authentication) {
        executionFactService.testifyExecutionFact(executionFactId, authentication);
    }

    @GetMapping(params = {"from", "to", "participantId"})
    public List<GetExecutionFactDTO> getForDateRangeForPerson(@RequestParam LocalDateTime from,
                                                              @RequestParam(required = false) LocalDateTime to,
                                                              @RequestParam UUID participantId
                                                              ) {
        return executionFactService.getForDateRangeForPerson(from, to, participantId);
    }


}
