package com.duty.manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "Testimony")
@Table(
        name = "testimonies",
        uniqueConstraints = @UniqueConstraint(
                name = "testimonies_witness_id_execution_fact_id_key",
                columnNames = {"witness_id", "execution_fact_id"}
        )
)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder(setterPrefix = "with")
public class Testimony {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, columnDefinition="uuid")
    private UUID id;

    @Column(name = "witness_id", nullable = false, columnDefinition="uuid not null")
    @NotNull(message = "Testimony must have witness id")
    private UUID witnessId;

    @Column(name = "execution_fact_id", nullable = false, columnDefinition="uuid not null")
    @NotNull(message = "Testimony must have execution fact id")
    private UUID executionFactId;

    @Column(name = "timestamp", nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime timestamp;

    @Version
    private Long version;
}
