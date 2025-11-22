package com.bers.services.scheduler;

import com.bers.domain.entities.Trip;
import com.bers.domain.entities.enums.TripStatus;
import com.bers.domain.repositories.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

 /* Transiciones:

  - SCHEDULED → BOARDING (30 min antes de salida)

  - BOARDING → DEPARTED (a la hora de salida)
 */

@Component
@RequiredArgsConstructor
@Slf4j

public class TripStatusModifierScheduler {


    private final TripRepository tripRepository;

    // Actualiza estados de viajes cada 5 minutos.

    @Scheduled(cron = "0 */5 * * * *")

    @Transactional

    public void updateTripStatuses() {

        log.debug("Iniciando actualizacion de estados de viajes...");


        try {

            int updatedToBoarding = updateToBoarding();

            int updatedToDeparted = updateToDeparted();


            int totalUpdated = updatedToBoarding + updatedToDeparted;


            if (totalUpdated > 0) {

                log.info("Actualizacion completada: {} viajes actualizados (BOARDING: {}, DEPARTED: {})",

                        totalUpdated, updatedToBoarding, updatedToDeparted);

            } else {

                log.debug("No hay viajes que requieran actualizacion de estado");

            }


        } catch (Exception e) {

            log.error("Error al actualizar estados de viajes", e);

        }

    }

    /*
      Actualiza viajes de SCHEDULED a BOARDING.

      Se ejecuta 30 minutos antes de la hora de salida.
     */

    private int updateToBoarding() {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime boardingTime = now.plusMinutes(30);


        List<Trip> tripsToUpdate = tripRepository.findAll().stream()

                .filter(trip -> trip.getStatus() == TripStatus.SCHEDULED)

                .filter(trip -> {

                    LocalDateTime departureTime = trip.getDepartureAt();

                    // Cambiar a BOARDING si falta 30 minutos o menos

                    return departureTime != null &&

                            !departureTime.isAfter(boardingTime) &&

                            departureTime.isAfter(now);

                })

                .toList();


        tripsToUpdate.forEach(trip -> {

            trip.setStatus(TripStatus.BOARDING);

            tripRepository.save(trip);

            log.info("Viaje {} actualizado a BOARDING (salida: {})",

                    trip.getId(), trip.getDepartureAt());

        });


        return tripsToUpdate.size();

    }

    /*

      Actualiza viajes de BOARDING a DEPARTED.

      Se ejecuta cuando llega la hora de salida.

     */

    private int updateToDeparted() {

        LocalDateTime now = LocalDateTime.now();


        List<Trip> tripsToUpdate = tripRepository.findAll().stream()

                .filter(trip -> trip.getStatus() == TripStatus.BOARDING)

                .filter(trip -> {

                    LocalDateTime departureTime = trip.getDepartureAt();

                    // Cambiar a DEPARTED si ya paso la hora de salida

                    return departureTime != null && !departureTime.isAfter(now);

                })

                .toList();


        tripsToUpdate.forEach(trip -> {

            trip.setStatus(TripStatus.DEPARTED);

            tripRepository.save(trip);

            log.info("Viaje {} actualizado a DEPARTED (salida programada: {})",

                    trip.getId(), trip.getDepartureAt());

        });


        return tripsToUpdate.size();

    }
}
