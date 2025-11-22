package com.bers.domain.repositories;

import com.bers.domain.entities.SeatHold;
import com.bers.domain.entities.enums.HoldStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long> {

    Optional<SeatHold> findByTripIdAndSeatNumberAndStatus(
            Long tripId, String seatNumber, HoldStatus status);

    @Query("SELECT s FROM SeatHold s WHERE s.status = 'HOLD' AND s.trip = :tripId")
    List<SeatHold> findActiveHoldsByTripId(Long tripId);


    List<SeatHold> findByTripIdAndStatus(Long tripId, HoldStatus status);

    List<SeatHold> findByUserIdAndStatus(Long userId, HoldStatus status);

    @Query("SELECT sh FROM SeatHold sh WHERE sh.status = 'HOLD' " +
            "AND sh.expiresAt < :currentTime")
    List<SeatHold> findExpiredHolds(@Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query("UPDATE SeatHold sh SET sh.status = 'EXPIRED' " +
            "WHERE sh.status = 'HOLD' AND sh.expiresAt < :currentTime")
    int expireOldHolds(@Param("currentTime") LocalDateTime currentTime);

    boolean existsByTripIdAndSeatNumberAndStatus(
            Long tripId, String seatNumber, HoldStatus status);

    @Query("SELECT COUNT(sh) FROM SeatHold sh WHERE sh.trip.id = :tripId " +
            "AND sh.status = 'HOLD'")
    long countActiveHoldsByTrip(@Param("tripId") Long tripId);

    @Modifying
    @Query("DELETE FROM SeatHold sh WHERE sh.status = 'EXPIRED' " +
            "AND sh.expiresAt < :cutoffDate")
    int deleteExpiredHoldsOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);

    @Query("SELECT sh FROM SeatHold sh WHERE sh.trip.id = :tripId " +
            "AND sh.fromStop.id = :fromStopId AND sh.toStop.id = :toStopId " +
            "AND sh.status = 'HOLD' AND sh.expiresAt > :currentTime")
    List<SeatHold> findActiveHoldsByTripAndSegment(
            @Param("tripId") Long tripId,
            @Param("fromStopId") Long fromStopId,
            @Param("toStopId") Long toStopId,
            @Param("currentTime") LocalDateTime currentTime);
}
