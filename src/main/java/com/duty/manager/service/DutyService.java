package com.duty.manager.service;

import com.duty.manager.dto.CreateDutyDTO;
import com.duty.manager.dto.GetDutyDTO;
import com.duty.manager.dto.UpdateDutyDTO;

import java.util.List;
import java.util.UUID;

public interface DutyService {

    UUID createDuty(CreateDutyDTO dutyDTO);

    List<GetDutyDTO> getDuties(Long page, Long pageSize);

    GetDutyDTO getDutyByIdentifier(String identifier);

    void updateDuty(UpdateDutyDTO updateDutyDTO);

    void deleteDuty(String identifier);

}
