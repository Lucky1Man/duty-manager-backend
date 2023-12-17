package com.duty.manager.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<PasswordFormat, String> {

    // if any changes are made to which characters are allowed then change default message at
    // package com.duty.manager.validator.PasswordFormat
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value.chars().allMatch(ch ->
                Character.isLetter(ch) && Character.UnicodeScript.of(ch) == Character.UnicodeScript.LATIN ||
                        ch == '_' || ch == '-' || ch == '&'
        );
    }

}
