package com.izertis.football.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Represents a federated player belonging to a Football Club.
 */
@Entity
@Table(name = "players",
        uniqueConstraints = @UniqueConstraint(name = "uk_player_email", columnNames = "email"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String givenName;

    @Column(nullable = false, length = 100)
    private String familyName;

    @Column(nullable = false, length = 100)
    private String nationality;

    /**
     * Valid email, unique across the system.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /**
     * Date of birth in ISO-8601 format (YYYY-MM-DD).
     */
    @Column(nullable = false)
    private LocalDate dateOfBirth;

    /**
     * The club this player belongs to.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "club_id", nullable = false)
    private Club club;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
