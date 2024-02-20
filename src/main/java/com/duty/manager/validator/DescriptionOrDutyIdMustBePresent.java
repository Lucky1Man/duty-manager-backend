package com.duty.manager.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = RecordExecutionFactValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DescriptionOrDutyIdMustBePresent {

    String message() default "You must provide either description for execution fact or duty id.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
