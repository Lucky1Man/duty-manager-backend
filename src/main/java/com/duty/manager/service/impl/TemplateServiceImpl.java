package com.duty.manager.service.impl;

import com.duty.manager.dto.CreateTemplateDTO;
import com.duty.manager.dto.GetTemplateDTO;
import com.duty.manager.dto.UpdateTemplateDTO;
import com.duty.manager.entity.Template;
import com.duty.manager.repository.TemplateRepository;
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

    private final TemplateRepository templateRepository;

    private final ModelMapper modelMapper;

    @Override
    public UUID createTemplate(CreateTemplateDTO templateDTO) {
        throwExceptionIfExist(templateDTO);
        Template template = modelMapper.map(templateDTO, Template.class);
        return templateRepository.save(template).getId();
    }

    private void throwExceptionIfExist(CreateTemplateDTO templateDTO) {
        String name = templateDTO.getName();
        try {
            getTemplates(name);
            throw new IllegalArgumentException(
                    "Template with name %s already exists".formatted(name)
            );
        } catch (ServiceException ignored) {
        }
    }

    @Override
    public List<GetTemplateDTO> getTemplates(@NotNull @Min(0) Integer page, @Max(50) @NotNull Integer pageSize) {
        return templateRepository.findAll(PageRequest.of(page, pageSize)).get()
                .map(this::templateEntityToGetDTO).toList();
    }

    private GetTemplateDTO templateEntityToGetDTO(Template template) {
        return modelMapper.map(template, GetTemplateDTO.class);
    }

    @Override
    public GetTemplateDTO getTemplates(@NotNull String identifier) {
        return templateEntityToGetDTO(getRowTemplate(identifier));
    }

    private Template getRowTemplate(String identifier) {
        Optional<Template> template;
        try {
            template = templateRepository.findById(UUID.fromString(identifier));
        } catch (IllegalArgumentException e) {
            template = templateRepository.findByName(identifier);
        }
        return template.orElseThrow(
                () -> new ServiceException("Template with identifier %s not found".formatted(identifier))
        );
    }

    @Override
    public void updateTemplates(@NotNull String identifier, @Valid UpdateTemplateDTO templateUpdates) {
        Template template = getRowTemplate(identifier);
        if (templateUpdates.getName() != null && templateRepository.findByName(templateUpdates.getName()).isEmpty()) {
            template.setName(templateUpdates.getName());
        }
        if (templateUpdates.getDescription() != null) {
            template.setDescription(templateUpdates.getDescription());
        }
        templateRepository.flush();
    }

    @Override
    public void deleteTemplates(@NotNull String identifier) {
        try {
            Template template = getRowTemplate(identifier);
            templateRepository.delete(template);
            templateRepository.flush();
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

}
