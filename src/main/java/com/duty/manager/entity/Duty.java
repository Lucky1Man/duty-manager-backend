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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import java.util.UUID;

@Entity(name = "Duty")
@Table(
        name = "duties",
        uniqueConstraints = @UniqueConstraint(
                name = "duties_name_key",
                columnNames = "name"
        )
)
@Getter
@Setter
@ToString
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor
public class Duty {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, columnDefinition="uuid")
    private UUID id;

    @Column(
            name = "name",
            nullable = false,
            columnDefinition = "varchar(100)"
    )
    @Length(min = 1, max = 100)
    @NotNull(message = "Duty must have name")
    private String name;

    @Column(
            name = "description",
            columnDefinition = "varchar(500)"
    )
    @Length(min = 1, max = 500)
    private String description;

    @Version
    private Long version;

}


