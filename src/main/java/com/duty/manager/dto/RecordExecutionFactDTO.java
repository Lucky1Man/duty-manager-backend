package com.duty.manager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class RecordExecutionFactDTO {

    @NotNull(message = "Execution fact must have executor id")
    private UUID executorId;

    @NotNull(message = "Execution fact must have duty id")
    private UUID dutyId;
}
