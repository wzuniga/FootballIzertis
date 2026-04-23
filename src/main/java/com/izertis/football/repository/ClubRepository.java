package com.izertis.football.repository;

import com.izertis.football.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClubRepository extends JpaRepository<Club, UUID> {

    Optional<Club> findByUsername(String username);

    boolean existsByUsername(String username);

    /** Returns all clubs that are flagged as publicly visible. */
    List<Club> findAllByPublicVisibleTrue();
}
