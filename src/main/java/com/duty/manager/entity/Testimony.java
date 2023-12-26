package com.duty.manager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
import java.util.Objects;
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
@Builder(setterPrefix = "with")
public class Testimony {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, columnDefinition="uuid")
    private UUID id;

    @NotNull(message = "Testimony must have witness")
    @OneToOne(optional = false)
    @JoinColumn(name = "witness_id", referencedColumnName = "id", nullable = false)
    private Participant witness;

    @NotNull(message = "Testimony must have execution fact")
    @ManyToOne(optional = false)
    @JoinColumn(name = "execution_fact_id", referencedColumnName = "id", nullable = false)
    private ExecutionFact executionFact;

    @Column(name = "timestamp", nullable = false)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime timestamp;

    @Version
    private Long version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Testimony testimony = (Testimony) o;
        return Objects.equals(id, testimony.id) && Objects.equals(witness, testimony.witness) && Objects.equals(executionFact, testimony.executionFact);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, witness, executionFact);
    }
}
