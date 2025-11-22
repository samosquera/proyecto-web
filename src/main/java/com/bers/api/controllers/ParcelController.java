package com.bers.api.controllers;

import com.bers.api.dtos.ParcelDtos.ParcelCreateRequest;
import com.bers.api.dtos.ParcelDtos.ParcelResponse;
import com.bers.api.dtos.ParcelDtos.ParcelUpdateRequest;
import com.bers.domain.entities.enums.ParcelStatus;
import com.bers.security.config.CustomUserDetails;
import com.bers.services.service.FileStorageService;
import com.bers.services.service.ParcelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/parcels")
@RequiredArgsConstructor
@Slf4j
public class ParcelController {

    private final ParcelService parcelService;
    private final FileStorageService fileStorageService;


    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('PASSENGER','CLERK', 'ADMIN')")
    public ResponseEntity<ParcelResponse> createParcel(@Valid @RequestBody ParcelCreateRequest request) {
        log.info("Creating parcel from {} to {}", request.senderName(), request.receiverName());

        ParcelResponse created = parcelService.createParcel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<List<ParcelResponse>> getAllParcels() {
        log.debug("Retrieving all parcels");

        List<ParcelResponse> parcels = parcelService.getAllParcels();
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<ParcelResponse> getParcelById(@PathVariable Long id) {
        log.debug("Retrieving parcel: {}", id);

        ParcelResponse parcel = parcelService.getParcelById(id);
        return ResponseEntity.ok(parcel);
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ParcelResponse> getParcelByCode(@PathVariable String code) {
        log.debug("Retrieving parcel by code: {}", code);

        ParcelResponse parcel = parcelService.getParcelByCode(code);
        return ResponseEntity.ok(parcel);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('CLERK', 'ADMIN')")
    public ResponseEntity<ParcelResponse> updateParcel(
            @PathVariable Long id,
            @Valid @RequestBody ParcelUpdateRequest request
    ) {
        log.info("Updating parcel: {}", id);

        ParcelResponse updated = parcelService.updateParcel(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteParcel(@PathVariable Long id) {
        log.warn("Deleting parcel: {}", id);

        parcelService.deleteParcel(id);
        return ResponseEntity.noContent().build();
    }

    // Gestionar estados

    @PostMapping("/{id}/in-transit")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<ParcelResponse> markAsInTransit(
            @PathVariable Long id,
            @RequestParam Long tripId
    ) {
        log.info("Marking parcel {} as in-transit for trip: {}", id, tripId);

        ParcelResponse updated = parcelService.markAsInTransit(id, tripId);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/delivered")
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<ParcelResponse> markAsDelivered(
            @PathVariable Long id,
            @RequestParam String otp,
            @RequestParam(required = false) String photoUrl,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Marking parcel {} as delivered with OTP", id);

        ParcelResponse updated = parcelService.markAsDelivered(id, otp, photoUrl, userDetails.getId());
        return ResponseEntity.ok(updated);
    }

    @PostMapping(value = "/{id}/delivered-with-photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('DRIVER', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<ParcelResponse> markAsDeliveredWithPhoto(
            @PathVariable Long id,
            @RequestParam String otp,
            @RequestParam(required = false) MultipartFile photo,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        log.info("Marking parcel {} as delivered with OTP and photo", id);

        String photoUrl = null;
        if (photo != null && !photo.isEmpty()) {
            // Validate file type
            String contentType = photo.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("File must be an image");
            }

            // Validate file size (max 5MB)
            long maxFileSize = 5 * 1024 * 1024; // 5MB
            if (photo.getSize() > maxFileSize) {
                throw new IllegalArgumentException("File size must not exceed 5MB");
            }

            // Store the photo
            photoUrl = fileStorageService.storeFile(photo, "proof-of-delivery");
            log.info("Stored proof photo at: {}", photoUrl);
        }

        ParcelResponse updated = parcelService.markAsDelivered(id, otp, photoUrl, userDetails.getId());
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/failed")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<ParcelResponse> markAsFailed(
            @PathVariable Long id,
            @RequestParam String reason
    ) {
        log.warn("Marking parcel {} as failed - reason: {}", id, reason);

        ParcelResponse updated = parcelService.markAsFailed(id, reason);
        return ResponseEntity.ok(updated);
    }

    // Consultar

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<List<ParcelResponse>> getParcelsByStatus(@PathVariable ParcelStatus status) {
        log.debug("Retrieving parcels with status: {}", status);

        List<ParcelResponse> parcels = parcelService.getParcelsByStatus(status);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/trip/{tripId}")
    @PreAuthorize("hasAnyRole('CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<List<ParcelResponse>> getParcelsByTrip(@PathVariable Long tripId) {
        log.debug("Retrieving parcels for trip: {}", tripId);

        List<ParcelResponse> parcels = parcelService.getParcelsByTripId(tripId);
        return ResponseEntity.ok(parcels);
    }

    @GetMapping("/phone/{phone}")
    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'ADMIN')")
    public ResponseEntity<List<ParcelResponse>> getParcelsByPhone(@PathVariable String phone) {
        log.debug("Retrieving parcels for phone: {}", phone);

        List<ParcelResponse> parcels = parcelService.getParcelsByPhone(phone);
        return ResponseEntity.ok(parcels);
    }

    @PostMapping("/{id}/validate-otp")
    @PreAuthorize("hasAnyRole('PASSENGER', 'CLERK', 'DRIVER', 'ADMIN')")
    public ResponseEntity<Boolean> validateOtp(
            @PathVariable Long id,
            @RequestParam String otp
    ) {
        log.debug("Validating OTP for parcel: {}", id);

        boolean valid = parcelService.validateOtp(id, otp);
        return ResponseEntity.ok(valid);
    }

    // Actualizar estado de un parcel a partir de su codigo
    @PostMapping("/{code}/status")
    @PreAuthorize("hasAnyRole('DRIVER', 'CLERK', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<ParcelResponse> updateParcelStatus(
            @PathVariable String code,
            @RequestParam ParcelStatus status,
            @RequestParam(required = false) String reason,
            @RequestParam(required = false) Long tripId,
            @RequestParam(required = false) String otp,
            @RequestParam(required = false) String photoUrl,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        log.info("Updating parcel {} status to: {}", code, status);

        // Obtener el parcel por codigo
        ParcelResponse parcel = parcelService.getParcelByCode(code);
        Long parcelId = parcel.id();

        ParcelResponse response;

        // Aplicar el cambio de estado segun el tipo solicitado
        switch (status) {
            case IN_TRANSIT -> {
                if (tripId == null) {
                    throw new IllegalArgumentException("tripId is required for IN_TRANSIT status");
                }
                response = parcelService.markAsInTransit(parcelId, tripId);
            }
            case DELIVERED -> {
                if (otp == null || otp.isBlank()) {
                    throw new IllegalArgumentException("OTP is required for DELIVERED status");
                }
                Long deliveredBy = userDetails != null ? userDetails.getId() : null;
                response = parcelService.markAsDelivered(parcelId, otp, photoUrl, deliveredBy);
            }
            case FAILED -> {
                if (reason == null || reason.isBlank()) {
                    throw new IllegalArgumentException("reason is required for FAILED status");
                }
                response = parcelService.markAsFailed(parcelId, reason);
            }
            default -> throw new IllegalArgumentException("Invalid status: " + status +
                    ". Only IN_TRANSIT, DELIVERED, and FAILED are allowed");
        }

        log.info("Parcel {} status updated successfully to: {}", code, status);
        return ResponseEntity.ok(response);
    }
}