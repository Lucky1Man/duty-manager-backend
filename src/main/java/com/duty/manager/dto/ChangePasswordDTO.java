package com.duty.manager.dto;

import com.duty.manager.validator.PasswordFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordDTO {
    @NotNull(message = "You must provide old password.")
    private String oldPassword;

    @Length(min = 8, max = 72)
    @NotNull(message = "You must provide new password")
    @PasswordFormat
    private String newPassword;
}
