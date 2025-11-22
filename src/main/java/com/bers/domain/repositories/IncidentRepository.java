package com.bers.domain.repositories;

import com.bers.domain.entities.Incident;
import com.bers.domain.entities.enums.EntityType;
import com.bers.domain.entities.enums.IncidentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);

    List<Incident> findByType(IncidentType type);

    List<Incident> findByReportedById(Long reportedById);

    @Query("SELECT i FROM Incident i WHERE i.entityType = :entityType " +
            "AND i.entityId = :entityId ORDER BY i.createdAt DESC")
    List<Incident> findByEntityOrderByCreatedAtDesc(
            @Param("entityType") EntityType entityType,
            @Param("entityId") Long entityId);

    @Query("SELECT i FROM Incident i WHERE i.createdAt BETWEEN :start AND :end " +
            "ORDER BY i.createdAt DESC")
    List<Incident> findByDateRange(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    @Query("SELECT COUNT(i) FROM Incident i WHERE i.type = :type " +
            "AND i.createdAt >= :since")
    long countByTypeAndCreatedAtAfter(
            @Param("type") IncidentType type,
            @Param("since") LocalDateTime since);
}
