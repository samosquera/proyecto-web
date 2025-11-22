package com.bers.domain.repositories;

import com.bers.domain.entities.Seat;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.enums.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    @Query("SELECT t FROM Trip t JOIN FETCH t.route JOIN FETCH t.bus WHERE t.route.id = :routeId AND t.date = :date")
    List<Trip> findByRouteIdAndDate(@Param("routeId") Long routeId, @Param("date") LocalDate date);

    @Query("SELECT t FROM Trip t JOIN FETCH t.route JOIN FETCH t.bus WHERE t.route.id = :routeId AND t.date = :date AND t.status = :status")
    List<Trip> findByRouteIdAndDateAndStatus(@Param("routeId") Long routeId, @Param("date") LocalDate date, @Param("status") TripStatus status);

    @Query("SELECT t FROM Trip t JOIN FETCH t.route JOIN FETCH t.bus WHERE t.date = :date")
    List<Trip> findByDate(@Param("date") LocalDate date);

    @Query("SELECT t FROM Trip t JOIN FETCH t.route JOIN FETCH t.bus WHERE t.date = :date AND t.status = :status")
    List<Trip> findByDateAndStatus(@Param("date") LocalDate date, @Param("status") TripStatus status);


    @Query("SELECT t FROM Trip t WHERE t.date = CURRENT DATE AND t.status in ('SCHEDULED','BOARDING')")
    List<Trip> findTodayActiveTrips();

    @Query("SELECT t FROM Trip t WHERE t.status = :status")
    List<Trip> finByStatus(TripStatus status);

    @Query("SELECT t FROM Trip t JOIN FETCH t.route JOIN FETCH t.bus WHERE t.id = :id")
    Optional<Trip> findByIdWithDetails(Long id);

    @Query("SELECT t FROM Trip t WHERE t.date = :date AND t.status = :status " +
            "AND t.departureAt BETWEEN :startTime AND :endTime")
    List<Trip> findByDateAndTimeRange(
            @Param("date") LocalDate date,
            @Param("status") TripStatus status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT t FROM Trip t WHERE t.bus.id = :busId AND t.date = :date " +
            "AND t.status NOT IN ('CANCELLED', 'ARRIVED')")
    List<Trip> findActiveTripsByBusAndDate(@Param("busId") Long busId, @Param("date") LocalDate date);

    List<Trip> findByStatusAndDepartureAtBefore(TripStatus status, LocalDateTime dateTime);

    @Query("""
                SELECT t FROM Trip t
                WHERE (LOWER(t.route.origin) = LOWER(:origin))
                  AND (LOWER(t.route.destination) = LOWER(:destination))
                  AND (t.date = :date)
                  AND (t.status IN ('SCHEDULED','BOARDING'))
                ORDER BY t.departureAt ASC
            """)
    List<Trip> searchTrips(
            @Param("origin") String origin,
            @Param("destination") String destination,
            @Param("date") LocalDate date
    );

    @Query("SELECT s FROM Trip t JOIN t.bus b JOIN b.seats s WHERE t.id = :tripId ")
    List<Seat> findSeatsByTripId(@Param("tripId") Long tripId);


}
