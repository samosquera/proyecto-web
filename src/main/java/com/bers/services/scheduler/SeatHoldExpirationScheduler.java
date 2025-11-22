package com.bers.services.scheduler;

import com.bers.domain.repositories.SeatHoldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduler que expira automáticamente los holds vencidos.
 * Usa el metodo optimizado del repository con UPDATE directo.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeatHoldExpirationScheduler {

    private final SeatHoldRepository seatHoldRepository;

// Se ejecuta cada 3 minutos para actualizar
// el estado de los holds expired directamente en bd
    @Scheduled(fixedRate = 180000) // 180,000 ms = 3 minutos
    @Transactional
    public void expireOldHoldsAutomatically() {
        try {
            LocalDateTime now = LocalDateTime.now();

            log.info("Running scheduled hold expiration check at {}", now);

            // USA EL METODO OPTIMIZADO DE TU REPOSITORY
            // Este hace: UPDATE seat_holds SET status = 'EXPIRED' WHERE...
            int expiredCount = seatHoldRepository.expireOldHolds(now);

            if (expiredCount > 0) {
                log.info("Expired {} holds at  {}", expiredCount, now);
            } else {
                log.info("No expired holds found");
            }

        } catch (Exception e) {
            log.error("Error expiring old holds in scheduler", e);
            // No lanzar la excepción para que el scheduler siga funcionando
        }
    }

// Opcional: para limpieza de holds muy antiguos xd
    @Scheduled(cron = "0 0 3 * * *") // 3 AM todos los días
    @Transactional
    public void cleanupVeryOldExpiredHolds() {
        try {
            LocalDateTime cutoff = LocalDateTime.now().minusDays(7);

            log.info("Starting cleanup of expired holds older than 7 days...");

            int deleted = seatHoldRepository.deleteExpiredHoldsOlderThan(cutoff);

            if (deleted > 0) {
                log.info("Cleanup completed: {} old holds deleted", deleted);
            } else {
                log.info("No old holds to clean up");
            }

        } catch (Exception e) {
            log.error("Error cleaning up old holds", e);
        }
    }
}
