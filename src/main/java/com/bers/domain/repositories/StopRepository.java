package com.bers.domain.repositories;

import com.bers.domain.entities.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StopRepository extends JpaRepository<Stop, Long> {

    List<Stop> findByRouteIdOrderByOrderAsc(Long routeId);

    List<Stop> findByRouteId(Long routeId);

    List<Stop> findByNameContainingIgnoreCase(String name);

    List<Stop> findByRoute_IdAndOrderGreaterThanOrderByOrderAsc(Long routeId, int fromOrder);
}
