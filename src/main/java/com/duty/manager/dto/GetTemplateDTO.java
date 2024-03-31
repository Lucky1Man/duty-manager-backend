package com.duty.manager.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder(setterPrefix = "with")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetTemplateDTO extends GetSecuredDTO {
    private UUID id;

    private String name;

    private String description;

    @JsonGetter
    public UUID getId() {
        return id;
    }

    @JsonGetter
    public String getName() {
        return super.getSecuredString(name);
    }

    @JsonGetter
    public String getDescription() {
        return super.getSecuredString(description);
    }
}
