package com.duty.manager.dto;

import com.duty.manager.validator.PasswordFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class RegisterParticipantDTO {

    @Length(min = 1, max = 100)
    @NotNull(message = "Participant must have full name")
    private String fullName;

    @Length(min = 3, max = 320)
    @NotNull(message = "Participant must have email")
    @Email
    private String email;

    @Length(min = 8, max = 72)
    @NotNull(message = "Participant must have password")
    @PasswordFormat
    private String password;

}
