package com.bers.domain.repositories;

import com.bers.domain.entities.Baggage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BaggageRepository extends JpaRepository<Baggage, Long> {

    Optional<Baggage> findByTagCode(String tagCode);

    List<Baggage> findByTicketId(Long ticketId);

    @Query("SELECT b FROM Baggage b WHERE b.ticket.trip.id = :tripId")
    List<Baggage> findByTripId(@Param("tripId") Long tripId);

    @Query("SELECT SUM(b.weightKg) FROM Baggage b WHERE b.ticket.trip.id = :tripId")
    BigDecimal getTotalWeightByTrip(@Param("tripId") Long tripId);

    boolean existsByTagCode(String tagCode);
}
