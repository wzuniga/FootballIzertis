package com.izertis.football.repository;

import com.izertis.football.domain.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Searches players of a club by optional name (givenName or familyName) and nationality.
     * All parameters are optional – null means "no filter on this field".
     */
    @Query("SELECT p FROM Player p WHERE p.club.id = :clubId "
            + "AND (:name IS NULL OR LOWER(p.givenName)   LIKE LOWER(CONCAT('%', :name, '%')) "
            +              "OR LOWER(p.familyName) LIKE LOWER(CONCAT('%', :name, '%'))) "
            + "AND (:nationality IS NULL OR LOWER(p.nationality) = LOWER(:nationality))")
    List<Player> searchByClub(@Param("clubId") UUID clubId,
                               @Param("name") String name,
                               @Param("nationality") String nationality);
}
