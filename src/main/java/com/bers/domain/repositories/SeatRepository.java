package com.bers.domain.repositories;

import com.bers.domain.entities.Seat;
import com.bers.domain.entities.enums.SeatType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByBusId(Long busId);

    List<Seat> findByBusIdOrderByNumberAsc(Long busId);

    Optional<Seat> findByBusIdAndNumber(Long busId, String number);

    List<Seat> findAll();

    List<Seat> findByBusIdAndType(Long busId, SeatType type);

    long countByBusId(Long busId);


}
