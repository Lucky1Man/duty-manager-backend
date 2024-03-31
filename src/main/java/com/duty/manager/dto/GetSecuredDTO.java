package com.duty.manager.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder(setterPrefix = "with")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetSecuredDTO {

    private static final int CHARACTERS_TO_SHOW = 2;

    @JsonIgnore
    private boolean isSecured = true;

    @JsonIgnore
    public String getSecuredString(String s) {
        if (isSecured && s != null) {
            StringBuilder value = new StringBuilder();
            if (s.length() >= CHARACTERS_TO_SHOW) {
                value.append(s, 0, CHARACTERS_TO_SHOW);
                if (s.length() > CHARACTERS_TO_SHOW) {
                    value.append("*".repeat(s.length() - CHARACTERS_TO_SHOW));
                }
            } else {
                value.append("***");
            }
            return value.toString();
        } else {
            return s;
        }
    }
}
