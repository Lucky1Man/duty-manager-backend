package com.duty.manager.validator;

import com.duty.manager.dto.RecordExecutionFactDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RecordExecutionFactValidator implements ConstraintValidator<DescriptionOrDutyIdMustBePresent,
        RecordExecutionFactDTO> {
    @Override
    public boolean isValid(RecordExecutionFactDTO recordExecutionFactDTO,
                           ConstraintValidatorContext constraintValidatorContext) {
        return recordExecutionFactDTO.getDutyId() != null || recordExecutionFactDTO.getDescription() != null;
    }
}
