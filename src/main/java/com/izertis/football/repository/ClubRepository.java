package com.izertis.football.repository;

import com.izertis.football.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * Searches public clubs by optional name (officialName or popularName) and federation.
     * All parameters are optional – null means "no filter on this field".
     */
    @Query("SELECT c FROM Club c WHERE c.publicVisible = true "
            + "AND (:name IS NULL OR LOWER(c.officialName) LIKE LOWER(CONCAT('%', :name, '%')) "
            +              "OR LOWER(c.popularName)   LIKE LOWER(CONCAT('%', :name, '%'))) "
            + "AND (:federation IS NULL OR LOWER(c.federation) = LOWER(:federation))")
    List<Club> searchPublicClubs(@Param("name") String name,
                                  @Param("federation") String federation);
}
