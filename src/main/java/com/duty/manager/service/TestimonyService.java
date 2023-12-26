package com.duty.manager.service;

import com.duty.manager.dto.GetTestimonyDTO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Validated
public interface TestimonyService {

    UUID testifyExecutionFact(@NotNull UUID executionFactId, @NotNull Authentication authentication);

    List<GetTestimonyDTO> getTestimoniesForExecutionFact(@NotNull UUID id, @Min(0) @NotNull Integer page,
                                                         @Max(200) @NotNull Integer pageSize);

}
