package com.bers.domain.repositories;

import com.bers.domain.entities.FareRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FareRuleRepository extends JpaRepository<FareRule, Long> {

    List<FareRule> findByRouteId(Long routeId);

    Optional<FareRule> findByRouteIdAndFromStopIdAndToStopId(
            Long routeId, Long fromStopId, Long toStopId);

    @Query("SELECT fr FROM FareRule fr WHERE fr.route.id = :routeId " +
            "AND fr.fromStop.id = :fromStopId AND fr.toStop.id = :toStopId")
    Optional<FareRule> findFareForSegment(
            @Param("routeId") Long routeId,
            @Param("fromStopId") Long fromStopId,
            @Param("toStopId") Long toStopId);

    @Query("SELECT fr FROM FareRule fr WHERE fr.route.id = :routeId " +
            "AND fr.dynamicPricing = 'ON'")
    List<FareRule> findDynamicPricingRules(@Param("routeId") Long routeId);
}
