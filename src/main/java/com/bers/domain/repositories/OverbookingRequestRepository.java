package com.bers.domain.repositories;

import com.bers.domain.entities.OverbookingRequest;
import com.bers.domain.entities.enums.OverbookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OverbookingRequestRepository extends JpaRepository<OverbookingRequest, Long> {

    List<OverbookingRequest> findByTripIdAndStatus(Long tripId, OverbookingStatus status);

    List<OverbookingRequest> findByStatus(OverbookingStatus status);

    Optional<OverbookingRequest> findByTicketId(Long ticketId);

    @Query("SELECT COUNT(or) FROM OverbookingRequest or WHERE or.trip.id = :tripId AND or.status = 'APPROVED'")
    long countApprovedOverbookingsByTrip(@Param("tripId") Long tripId);

    @Query("SELECT or FROM OverbookingRequest or WHERE or.status = 'PENDING_APPROVAL' AND or.expiresAt < :currentTime")
    List<OverbookingRequest> findExpiredPendingRequests(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT or FROM OverbookingRequest or WHERE or.trip.id = :tripId")
    List<OverbookingRequest> findByTripId(@Param("tripId") Long tripId);
}