package com.duty.manager.service;

import com.duty.manager.dto.CreateTemplateDTO;
import com.duty.manager.dto.GetDutyDTO;
import com.duty.manager.dto.UpdateTemplateDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Validated
public interface TemplateService {

    UUID createTemplate(@Valid CreateTemplateDTO dutyDTO);

    List<GetDutyDTO> getTemplates(@NotNull @Min(0) Integer page, @Max(50) @NotNull Integer pageSize);

    GetDutyDTO getTemplates(@NotNull String identifier);

    void updateTemplates(@NotNull String identifier, @Valid UpdateTemplateDTO updateTemplateDTO);

    void deleteTemplates(@NotNull String identifier);

}
