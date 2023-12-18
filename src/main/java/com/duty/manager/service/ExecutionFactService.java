package com.duty.manager.service;

import com.duty.manager.dto.GetExecutionFactDTO;
import com.duty.manager.dto.RecordExecutionFactDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Validated
public interface ExecutionFactService {

    List<GetExecutionFactDTO> getForDateRange(@NotNull LocalDateTime from, LocalDateTime to);

    UUID recordExecutionFact(@Valid RecordExecutionFactDTO factDTO);

    void testifyExecutionFact(@NotNull UUID executionFactId, @NotNull Authentication authentication);

    List<GetExecutionFactDTO> getForDateRangeForPerson(@NotNull LocalDateTime from, LocalDateTime to,
                                                       @NotNull UUID participantId);

}
