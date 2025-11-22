package com.bers.domain.repositories;

import com.bers.domain.entities.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

    Optional<Assignment> findByTripId(Long tripId);

    List<Assignment> findByDriverId(Long driverId);

    List<Assignment> findByDriverIdAndAssignedAtBetween(Long driverId, LocalDateTime start, LocalDateTime end);

    List<Assignment> findByDispatcherId(Long dispatcherId);


    @Query("SELECT a FROM Assignment a JOIN FETCH a.trip t JOIN FETCH a.driver " +
            "WHERE a.trip.id = :tripId")
    Optional<Assignment> findByTripIdWithDetails(@Param("tripId") Long tripId);

    @Query("SELECT a FROM Assignment a WHERE a.driver.id = :driverId " +
            "AND a.trip.date = CURRENT_DATE AND a.trip.status IN ('SCHEDULED', 'BOARDING')")
    List<Assignment> findActiveAssignmentsByDriver(@Param("driverId") Long driverId);

    @Query("SELECT a FROM Assignment a WHERE a.trip.departureAt BETWEEN :start AND :end")
    List<Assignment> findByDepartureDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    boolean existsByTripId(Long tripId);

}
