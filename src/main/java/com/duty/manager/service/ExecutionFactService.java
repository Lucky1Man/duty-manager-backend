package com.duty.manager.service;

import com.duty.manager.dto.GetExecutionFactDTO;
import com.duty.manager.dto.RecordExecutionFactDTO;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Validated
public interface ExecutionFactService {

    List<GetExecutionFactDTO> getFinishedForDateRange(@NotNull LocalDateTime from, @Nullable LocalDateTime to);

    UUID recordExecutionFact(@Valid RecordExecutionFactDTO factDTO);

    List<GetExecutionFactDTO> getFinishedForDateRangeForParticipant(@NotNull LocalDateTime from, @Nullable LocalDateTime to,
                                                                    @NotNull UUID participantId);

    void finishExecution(UUID id, Authentication authentication);

    List<GetExecutionFactDTO> getActiveForDateRange(@NotNull LocalDateTime from, @Nullable LocalDateTime to);

    List<GetExecutionFactDTO> getActiveForDateRangeForParticipant(@NotNull LocalDateTime from, @Nullable LocalDateTime to,
                                                                    @NotNull UUID participantId);

    GetExecutionFactDTO getById(UUID id);

    List<GetExecutionFactDTO> getInRange(@NotNull LocalDateTime from, @Nullable LocalDateTime to);

    List<GetExecutionFactDTO> getInRangeForParticipant(@NotNull LocalDateTime from, @Nullable LocalDateTime to,
                                                       @NotNull UUID participantId);

}
