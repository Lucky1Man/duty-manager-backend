package com.duty.manager.service;

import com.duty.manager.dto.CreateDutyDTO;
import com.duty.manager.dto.GetDutyDTO;
import com.duty.manager.dto.UpdateDutyDTO;

import java.util.List;
import java.util.UUID;

public interface DutyService {

    UUID createDuty(CreateDutyDTO dutyDTO);

    List<GetDutyDTO> getDuties(Integer page, Integer pageSize);

    GetDutyDTO getDuty(String identifier);

    void updateDuty(String identifier, UpdateDutyDTO updateDutyDTO);

    void deleteDuty(String identifier);

}
