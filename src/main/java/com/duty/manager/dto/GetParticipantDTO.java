package com.duty.manager.dto;

import com.duty.manager.entity.Role;
import com.fasterxml.jackson.annotation.JsonGetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@SuperBuilder(setterPrefix = "with")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetParticipantDTO extends GetSecuredDTO {

    private UUID id;

    private String fullName;

    private String email;

    private Role role;

    private String jwt;


    @JsonGetter
    public String getFullName() {
        return super.getSecuredString(fullName);
    }

    @JsonGetter
    public String getEmail() {
        return super.getSecuredString(email);
    }

}
