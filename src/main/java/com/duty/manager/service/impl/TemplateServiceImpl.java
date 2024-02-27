package com.duty.manager.service.impl;

import com.duty.manager.dto.CreateTemplateDTO;
import com.duty.manager.dto.GetDutyDTO;
import com.duty.manager.dto.UpdateTemplateDTO;
import com.duty.manager.entity.Template;
import com.duty.manager.repository.DutyRepository;
import com.duty.manager.service.TemplateService;
import com.duty.manager.service.ServiceException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TemplateServiceImpl implements TemplateService {

    private final DutyRepository dutyRepository;

    private final ModelMapper modelMapper;

    @Override
    public UUID createTemplate(CreateTemplateDTO dutyDTO) {
        throwExceptionIfExist(dutyDTO);
        Template template = modelMapper.map(dutyDTO, Template.class);
        return dutyRepository.save(template).getId();
    }

    private void throwExceptionIfExist(CreateTemplateDTO dutyDTO) {
        String name = dutyDTO.getName();
        try {
            getTemplates(name);
            throw new IllegalArgumentException(
                    "Duty with name %s already exists".formatted(name)
            );
        } catch (ServiceException ignored) {
        }
    }

    @Override
    public List<GetDutyDTO> getTemplates(@NotNull @Min(0) Integer page, @Max(50) @NotNull Integer pageSize) {
        return dutyRepository.findAll(PageRequest.of(page, pageSize)).get()
                .map(this::dutyEntityToGetDTO).toList();
    }

    private GetDutyDTO dutyEntityToGetDTO(Template template) {
        return modelMapper.map(template, GetDutyDTO.class);
    }

    @Override
    public GetDutyDTO getTemplates(@NotNull String identifier) {
        return dutyEntityToGetDTO(getRowDuty(identifier));
    }

    private Template getRowDuty(String identifier) {
        Optional<Template> duty;
        try {
            duty = dutyRepository.findById(UUID.fromString(identifier));
        } catch (IllegalArgumentException e) {
            duty = dutyRepository.findByName(identifier);
        }
        return duty.orElseThrow(
                () -> new ServiceException("Duty with identifier %s not found".formatted(identifier))
        );
    }

    @Override
    public void updateTemplates(@NotNull String identifier, @Valid UpdateTemplateDTO dutyUpdates) {
        Template template = getRowDuty(identifier);
        if (dutyUpdates.getName() != null && dutyRepository.findByName(dutyUpdates.getName()).isEmpty()) {
            template.setName(dutyUpdates.getName());
        }
        if (dutyUpdates.getDescription() != null) {
            template.setDescription(dutyUpdates.getDescription());
        }
        dutyRepository.flush();
    }

    @Override
    public void deleteTemplates(@NotNull String identifier) {
        try {
            Template template = getRowDuty(identifier);
            dutyRepository.delete(template);
            dutyRepository.flush();
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

}
