package com.duty.manager.service.impl;

import com.duty.manager.dto.CreateDutyDTO;
import com.duty.manager.dto.GetDutyDTO;
import com.duty.manager.dto.UpdateDutyDTO;
import com.duty.manager.entity.Duty;
import com.duty.manager.repository.DutyRepository;
import com.duty.manager.service.DutyService;
import com.duty.manager.service.ServiceException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DutyServiceImpl implements DutyService {

    private final DutyRepository dutyRepository;

    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public UUID createDuty(CreateDutyDTO dutyDTO) {
        throwExceptionIfExist(dutyDTO);
        Duty duty = modelMapper.map(dutyDTO, Duty.class);
        return dutyRepository.save(duty).getId();
    }

    private void throwExceptionIfExist(CreateDutyDTO dutyDTO) {
        String name = dutyDTO.getName();
        try {
            getDuty(name);
            throw new IllegalArgumentException(
                    "Duty with name %s already exists".formatted(name)
            );
        } catch (ServiceException ignored) {
        }
    }

    @Override
    public List<GetDutyDTO> getDuties(Integer page, Integer pageSize) {
        return dutyRepository.findAll(PageRequest.of(page, pageSize)).get()
                .map(this::dutyEntityToGetDTO).toList();
    }

    private GetDutyDTO dutyEntityToGetDTO(Duty duty) {
        return modelMapper.map(duty, GetDutyDTO.class);
    }

    @Override
    public GetDutyDTO getDuty(String identifier) {
        return dutyEntityToGetDTO(getRowDuty(identifier));
    }

    private Duty getRowDuty(String identifier) {
        Optional<Duty> duty;
        try{
            duty = dutyRepository.findById(UUID.fromString(identifier));
        } catch (IllegalArgumentException e) {
            duty = dutyRepository.findByName(identifier);
        }
        return duty.orElseThrow(
                () -> new ServiceException("Duty with identifier %s not found".formatted(identifier))
        );
    }

    @Override
    @Transactional
    public void updateDuty(String identifier, UpdateDutyDTO dutyUpdates) {
        Duty duty = getRowDuty(identifier);
        if(dutyUpdates.getName() != null && dutyRepository.findByName(dutyUpdates.getName()).isEmpty()) {
            duty.setName(dutyUpdates.getName());
        }
        if(dutyUpdates.getDescription() != null) {
            duty.setDescription(dutyUpdates.getDescription());
        }
        dutyRepository.flush();
    }

    @Override
    @Transactional
    public void deleteDuty(String identifier) {
        try {
            Duty duty = getRowDuty(identifier);
            dutyRepository.delete(duty);
            dutyRepository.flush();
        }catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

}
