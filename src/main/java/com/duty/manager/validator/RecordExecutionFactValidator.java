package com.duty.manager.validator;

import com.duty.manager.dto.RecordExecutionFactDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RecordExecutionFactValidator implements ConstraintValidator<DescriptionOrTemplateIdMustBePresent,
        RecordExecutionFactDTO> {
    @Override
    public boolean isValid(RecordExecutionFactDTO recordExecutionFactDTO,
                           ConstraintValidatorContext constraintValidatorContext) {
        return recordExecutionFactDTO.getTemplateId() != null || recordExecutionFactDTO.getDescription() != null;
    }
}
