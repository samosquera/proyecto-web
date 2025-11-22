package com.bers.domain.repositories;

import com.bers.domain.entities.Ticket;
import com.bers.domain.entities.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByQrCode(String qrCode);

    List<Ticket> findByTripIdAndStatus(Long tripId, TicketStatus status);

    List<Ticket> findByPassengerId(Long passengerId);

    Optional<Ticket> findById(Long id);

    @Query("""
            
                SELECT DISTINCT t FROM Ticket t
            
                LEFT JOIN FETCH t.trip tr
            
                LEFT JOIN FETCH tr.route r
            
                LEFT JOIN FETCH tr.bus b
            
                LEFT JOIN FETCH t.passenger p
            
                LEFT JOIN FETCH t.fromStop fs
            
                LEFT JOIN FETCH t.toStop ts
            
                WHERE t.passenger.id = :passengerId
            
                ORDER BY t.createdAt DESC
            
            """)
    List<Ticket> findByPassengerIdWithDetails(@Param("passengerId") Long passengerId);

    @Query("SELECT t FROM Ticket t WHERE t.trip.id = :tripId " +
            "AND t.seatNumber = :seatNumber AND t.status IN ('SOLD', 'PENDING_PAYMENT')")
    Optional<Ticket> findSoldTicketBySeat(@Param("tripId") Long tripId,
                                          @Param("seatNumber") String seatNumber);

    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.trip.id = :tripId " +
            "AND t.status IN ('SOLD', 'PENDING_PAYMENT')")
    long countSoldTicketsByTrip(@Param("tripId") Long tripId);

    @Query("SELECT t FROM Ticket t WHERE t.trip.id = :tripId " +
            "AND t.status IN ('SOLD','PAYMENT_PENDING') " +
            "AND ((t.fromStop.order <= :stopOrder AND t.toStop.order > :stopOrder))")
    List<Ticket> findActiveTicketsForStop(@Param("tripId") Long tripId,
                                          @Param("stopOrder") Integer stopOrder);

    @Query("""
                SELECT t\s
                FROM Ticket t
                WHERE t.trip.id = :tripId
                  AND t.seatNumber = :seatNumber
                  AND t.status IN ('SOLD', 'RESERVED')
            """)
    List<Ticket> findByTripIdAndSeatNumber(@Param("tripId") Long tripId,
                                           @Param("seatNumber") String seatNumber);


    List<Ticket> findByTripIdAndFromStopIdAndToStopIdAndStatus(Long tripId, Long fromStopId, Long toStopId, TicketStatus ticketStatus);

}
