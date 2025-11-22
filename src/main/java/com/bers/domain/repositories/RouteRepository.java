package com.bers.domain.repositories;

import com.bers.domain.entities.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByCode(String code);


    List<Route> findByOriginIgnoreCaseAndDestinationIgnoreCase(String origin, String destination);

    List<Route> findByOriginContainingIgnoreCaseOrDestinationContainingIgnoreCase(
            String origin, String destination);

    @Query("SELECT DISTINCT LOWER(r.origin) FROM Route r")
    List<String> getAllOrigins();

    @Query("SELECT LOWER(r.destination) FROM Route r WHERE LOWER(r.origin) = LOWER(:origin)")
    List<String> getDestinationsByOrigin(@Param("origin") String origin);

    @Query("SELECT r FROM Route r JOIN FETCH r.stops WHERE r.id = :id")
    Optional<Route> findByIdWithStops(Long id);

    boolean existsByCode(String code);
}
