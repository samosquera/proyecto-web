package com.bers.services.scheduler;

import com.bers.services.service.NoShowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoShowScheduler {

    private final NoShowService noShowService;


    // Ejecuta cada minuto para verificar trips proximos a salir

    @Scheduled(fixedDelay = 120000)
    public void checkForNoShows() {
        log.debug("Starting no-show check process");
        try {
            noShowService.processUpcomingTripsNoShow();
        } catch (Exception e) {
            log.error("Error in no-show scheduler: {}", e.getMessage(), e);
        }
    }
}