package com.duty.manager.service.impl;

import com.duty.manager.dto.GetExecutionFactDTO;
import com.duty.manager.dto.RecordExecutionFactDTO;
import com.duty.manager.service.ExecutionFactService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExecutionFactServiceImpl implements ExecutionFactService {
    @Override
    public List<GetExecutionFactDTO> getForDateRange(@NotNull LocalDateTime from, LocalDateTime to) {
        return null;
    }

    @Override
    public UUID recordExecutionFact(RecordExecutionFactDTO factDTO) {
        return null;
    }

    @Override
    public void testifyExecutionFact(@NotNull UUID executionFactId, @NotNull Authentication authentication) {

    }

    @Override
    public List<GetExecutionFactDTO> getForDateRangeForPerson(@NotNull LocalDateTime from, LocalDateTime to, @NotNull UUID participantId) {
        return null;
    }
}
