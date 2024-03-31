package com.duty.manager.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetExecutionFactDTO extends GetSecuredDTO {

    private UUID id;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startTime;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime finishTime;

    private String executorFullName;

    private UUID executorId;

    private String templateName;

    private List<GetTestimonyDTO> testimonies;

    private String description;

    private UUID templateId;

    @JsonGetter
    public String getExecutorFullName() {
        return super.getSecuredString(executorFullName);
    }

    @JsonGetter
    public String getTemplateName() {
        return super.getSecuredString(templateName);
    }

    @JsonGetter
    public String getDescription() {
        return super.getSecuredString(description);
    }

    @JsonSetter
    public void setTestimonies(List<GetTestimonyDTO> testimonies) {
        testimonies.forEach(t -> t.setSecured(isSecured()));
        this.testimonies = testimonies;
    }
}
