package com.izertis.football.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a Football Club registered in the Confederation system.
 * Acts as both the domain entity and the security principal (user account).
 */
@Entity
@Table(name = "clubs",
        uniqueConstraints = @UniqueConstraint(name = "uk_club_username", columnNames = "username"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Club {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    /**
     * Login credential – must be a valid email and unique across the system.
     */
    @Column(nullable = false, unique = true, length = 255)
    private String username;

    /**
     * BCrypt-hashed password. Never exposed in API responses.
     */
    @Column(nullable = false)
    private String password;

    /**
     * Official registered name of the club (e.g. "Manchester City F.C.").
     */
    @Column(nullable = false, length = 255)
    private String officialName;

    /**
     * Commonly-known name (e.g. "Man City").
     */
    @Column(nullable = false, length = 255)
    private String popularName;

    /**
     * Acronym of the football federation this club belongs to (max 8 chars).
     */
    @Column(nullable = false, length = 8)
    private String federation;

    /**
     * When true, the club and its players are visible to all authenticated users.
     */
    @Column(nullable = false)
    private boolean publicVisible;

    @OneToMany(mappedBy = "club", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Player> players = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
}
