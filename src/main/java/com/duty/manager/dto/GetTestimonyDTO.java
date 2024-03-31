package com.duty.manager.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTestimonyDTO extends GetSecuredDTO {

    private UUID id;

    private UUID witnessId;

    private String witnessFullName;

    private UUID executionFactId;

    private String templateName;

    private LocalDateTime timestamp;

    @JsonGetter
    public String getWitnessFullName() {
        return super.getSecuredString(witnessFullName);
    }

    @JsonGetter
    public String getTemplateName() {
        return super.getSecuredString(templateName);
    }
}
