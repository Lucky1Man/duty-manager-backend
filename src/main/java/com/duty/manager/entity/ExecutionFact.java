package com.duty.manager.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
import java.util.List;
import java.util.Objects;
import java.util.Set;
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

    @NotNull(message = "Execution fact must have executor")
    @OneToOne(optional = false, fetch = FetchType.LAZY, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "executor_id", referencedColumnName = "id", nullable = false)
    private Participant executor;

    @NotNull(message = "Execution fact must have duty")
    @OneToOne(optional = false, cascade = {CascadeType.MERGE})
    @JoinColumn(name = "duty_id", referencedColumnName = "id", nullable = false)
    private Duty duty;

    @OneToMany(mappedBy = "executionFact")
    private Set<Testimony> testimonies;

    @Version
    private Long version;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExecutionFact that = (ExecutionFact) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
