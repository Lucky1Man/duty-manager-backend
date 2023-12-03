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

@Entity(name = "Participant")
@Table(
        name = "participants",
        uniqueConstraints = @UniqueConstraint(
                name = "participants_email_key",
                columnNames = "email"
        )
)
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class Participant {
    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, columnDefinition="uuid")
    private UUID id;

    @Column(
            name = "full_name",
            nullable = false,
            columnDefinition = "varchar(100)"
    )
    @Length(min = 1, max = 100)
    @NotNull(message = "Participant must have full name")
    private String fullName;

    @Column(
            name = "email",
            nullable = false,
            columnDefinition = "varchar(320)"
    )
    @Length(min = 3, max = 320)
    @NotNull(message = "Participant must have email")
    private String email;

    @Column(
            name = "password",
            nullable = false,
            columnDefinition = "varchar(60)"
    )
    @Length(min = 60, max = 60)
    @NotNull(message = "Participant must have password")
    private String password;

    @Version
    private Long version;
}
