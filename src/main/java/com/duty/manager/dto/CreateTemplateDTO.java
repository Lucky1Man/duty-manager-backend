package com.duty.manager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateDTO {
    @Length(min = 1, max = 100)
    @NotNull(message = "Template must have name")
    private String name;

    @Length(min = 1, max = 500)
    private String description;
}
