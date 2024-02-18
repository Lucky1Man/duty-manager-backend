package com.duty.manager.dto;

import com.duty.manager.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder(setterPrefix = "with")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetParticipantDTO  {

    private UUID id;

    private String fullName;

    private String email;

    private Role role;
}
