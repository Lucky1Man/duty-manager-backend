package com.duty.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDutyDTO {

    @Length(min = 1, max = 100)
    private String name;

    @Length(min = 1, max = 500)
    private String description;

}
