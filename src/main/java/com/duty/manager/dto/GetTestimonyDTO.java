package com.duty.manager.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetTestimonyDTO {

    private UUID id;

    private UUID witnessId;

    private String witnessFullName;

    private UUID executionFactId;

    private String templateName;

    private LocalDateTime timestamp;
}
