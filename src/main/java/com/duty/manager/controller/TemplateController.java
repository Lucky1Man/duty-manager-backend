package com.duty.manager.controller;

import com.duty.manager.dto.CreateTemplateDTO;
import com.duty.manager.dto.GetTemplateDTO;
import com.duty.manager.dto.UpdateTemplateDTO;
import com.duty.manager.service.TemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final TemplateService templateService;

    @GetMapping
    @Operation(description = "Returns templates at specified page with specified page size. Page count starts from 0.")
    @ApiResponse(
            responseCode = "200",
            description = "Returns templates that are at specified page with specified page size",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(
                            schema = @Schema(implementation = GetTemplateDTO.class)
                    )
            )
    )
    public List<GetTemplateDTO> getTemplates(@RequestParam(required = false, defaultValue = "0")
                                   Integer page,
                                   @RequestParam(required = false, defaultValue = "50")
                                   Integer pageSize) {
        return templateService.getTemplates(page, pageSize);
    }

    @GetMapping("/{templateIdentifier}")
    @Operation(description = "Returns template by specified id or name. Returns 404 if no template was found")
    @ApiResponse(
            responseCode = "200",
            description = "Returns template with specified template id or template name",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = GetTemplateDTO.class)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Template with specified template id or template name was not found",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponse.class)
            )
    )
    public GetTemplateDTO getTemplate(@PathVariable String templateIdentifier) {
        return templateService.getTemplates(templateIdentifier);
    }

    @PostMapping
    @Operation(
            description = "Adds given template to database. Returns 400 if template with given name already exists"
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(
            responseCode = "201",
            description = "Returns id of template that was added",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = UUID.class)
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Means that name already exist",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponse.class)
            )
    )
    public ResponseEntity<UUID> addTemplate(@RequestBody CreateTemplateDTO addTemplateDto) {
        return ResponseEntity.status(HttpStatus.CREATED.value()).body(templateService.createTemplate(addTemplateDto));
    }

    @PutMapping("/{templateIdentifier}")
    @Operation(
            description = "Updates template with given identifier with data from UpdateTemplateDTO." +
                    " If UpdateTemplateDTO has null fields then that specific field will not be effected"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Means that template was updated and all given parameters were changed"
    )
    public void updateTemplate(@PathVariable String templateIdentifier, @RequestBody UpdateTemplateDTO updateTemplateDTO) {
        templateService.updateTemplates(templateIdentifier, updateTemplateDTO);
    }

    @DeleteMapping("/{templateIdentifier}")
    @Operation(
            description = "Deletes template with given identifier"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Means that template was deleted"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Means that template with given identifier does not exist",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ExceptionResponse.class)
            )
    )
    public void deleteTemplate(@PathVariable String templateIdentifier) {
        templateService.deleteTemplates(templateIdentifier);
    }

}
