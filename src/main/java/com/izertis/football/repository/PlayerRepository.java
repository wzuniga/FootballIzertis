package com.izertis.football.repository;

import com.izertis.football.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlayerRepository extends JpaRepository<Player, UUID> {

    List<Player> findAllByClubId(UUID clubId);

    Optional<Player> findByIdAndClubId(UUID id, UUID clubId);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, UUID excludedId);
}
