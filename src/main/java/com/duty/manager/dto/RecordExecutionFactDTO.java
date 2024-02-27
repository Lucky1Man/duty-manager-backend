package com.duty.manager.dto;

import com.duty.manager.validator.DescriptionOrTemplateIdMustBePresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@DescriptionOrTemplateIdMustBePresent
public class RecordExecutionFactDTO {

    @NotNull(message = "Execution fact must have executor id")
    private UUID executorId;

    private UUID templateId;

    private String description;
}
