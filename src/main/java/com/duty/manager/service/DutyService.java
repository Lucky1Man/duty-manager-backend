package com.duty.manager.service;

import com.duty.manager.dto.CreateDutyDTO;
import com.duty.manager.dto.GetDutyDTO;
import com.duty.manager.dto.UpdateDutyDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.UUID;

@Validated
public interface DutyService {

    UUID createDuty(@Valid CreateDutyDTO dutyDTO);

    List<GetDutyDTO> getDuties(@NotNull @Min(0) Integer page, @Max(50) @NotNull Integer pageSize);

    GetDutyDTO getDuty(@NotNull String identifier);

    void updateDuty(@NotNull String identifier, @Valid UpdateDutyDTO updateDutyDTO);

    void deleteDuty(@NotNull String identifier);

}
