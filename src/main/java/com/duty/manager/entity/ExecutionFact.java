package com.duty.manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "ExecutionFact")
@Table(name = "execution_facts")
@Getter
@Setter
@ToString
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionFact {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, columnDefinition="uuid")
    private UUID id;

    @Column(
            name = "start_time",
            nullable = false
    )
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @NotNull(message = "Execution fact must have start time")
    private LocalDateTime startTime;

    @Column(name = "finish_time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime finishTime;

    @Column(name = "executor_id", nullable = false, columnDefinition="uuid not null")
    @NotNull(message = "Execution fact must have executor id")
    private UUID executorId;

    @Column(name = "duty_id", nullable = false, columnDefinition="uuid not null")
    @NotNull(message = "Execution fact must have duty id")
    private UUID dutyId;

    @Version
    private Long version;

}
