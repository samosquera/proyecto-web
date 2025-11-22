package com.bers.domain.repositories;

import com.bers.domain.entities.Bus;
import com.bers.domain.entities.enums.BusStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {
    Optional<Bus> findById(Long id);

    Optional<Bus> findByPlate(String plate);

    List<Bus> findByStatus(BusStatus status);

    @Query("SELECT b FROM Bus b JOIN FETCH b.seats WHERE b.id = :id")
    Optional<Bus> findByIdWithSeats(Long id);

    @Query("SELECT b FROM Bus b WHERE b.status = :status AND b.capacity >= :minCapacity")
    List<Bus> findAvailableBusesByCapacity(BusStatus status, Integer minCapacity);

    boolean existsByPlate(String plate);
}
