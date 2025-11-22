package com.bers.services.service.serviceImple;

import com.bers.api.dtos.IncidentDtos.IncidentCreateRequest;
import com.bers.api.dtos.ParcelDtos.ParcelCreateRequest;
import com.bers.api.dtos.ParcelDtos.ParcelResponse;
import com.bers.api.dtos.ParcelDtos.ParcelUpdateRequest;
import com.bers.domain.entities.Parcel;
import com.bers.domain.entities.Stop;
import com.bers.domain.entities.Trip;
import com.bers.domain.entities.enums.EntityType;
import com.bers.domain.entities.enums.IncidentType;
import com.bers.domain.entities.enums.ParcelStatus;
import com.bers.domain.repositories.ParcelRepository;
import com.bers.domain.repositories.StopRepository;
import com.bers.domain.repositories.TripRepository;
import com.bers.services.mappers.ParcelMapper;
import com.bers.services.service.IncidentService;
import com.bers.services.service.ParcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ParcelServiceImpl implements ParcelService {

    private final ParcelRepository parcelRepository;
    private final TripRepository tripRepository;
    private final StopRepository stopRepository;
    private final ParcelMapper parcelMapper;
    private final IncidentService incidentService;

    @Override
    public ParcelResponse createParcel(ParcelCreateRequest request) {
        Stop fromStop = stopRepository.findById(request.fromStopId())
                .orElseThrow(() -> new IllegalArgumentException("From stop not found: " + request.fromStopId()));

        Stop toStop = stopRepository.findById(request.toStopId())
                .orElseThrow(() -> new IllegalArgumentException("To stop not found: " + request.toStopId()));

        if (!fromStop.getRoute().getId().equals(toStop.getRoute().getId())) {
            throw new IllegalArgumentException("Stops must belong to the same route");
        }

        Parcel parcel = parcelMapper.toEntity(request);
        parcel.setFromStop(fromStop);
        parcel.setToStop(toStop);

        if (request.tripId() != null) {
            Trip trip = tripRepository.findById(request.tripId())
                    .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + request.tripId()));
            parcel.setTrip(trip);
        }

        Parcel savedParcel = parcelRepository.save(parcel);
        return parcelMapper.toResponse(savedParcel);
    }

    @Override
    public ParcelResponse updateParcel(Long id, ParcelUpdateRequest request) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found: " + id));

        parcelMapper.updateEntity(request, parcel);
        Parcel updatedParcel = parcelRepository.save(parcel);
        return parcelMapper.toResponse(updatedParcel);
    }

    @Override
    @Transactional
    public ParcelResponse getParcelById(Long id) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found: " + id));
        return parcelMapper.toResponse(parcel);
    }

    @Override
    @Transactional
    public ParcelResponse getParcelByCode(String code) {
        Parcel parcel = parcelRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found with code: " + code));
        return parcelMapper.toResponse(parcel);
    }

    @Override
    @Transactional
    public List<ParcelResponse> getAllParcels() {
        return parcelRepository.findAll().stream()
                .map(parcelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ParcelResponse> getParcelsByStatus(ParcelStatus status) {
        return parcelRepository.findByStatus(status).stream()
                .map(parcelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ParcelResponse> getParcelsByTripId(Long tripId) {
        if (!tripRepository.existsById(tripId)) {
            throw new IllegalArgumentException("Trip not found: " + tripId);
        }
        return parcelRepository.findByTripId(tripId).stream()
                .map(parcelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<ParcelResponse> getParcelsByPhone(String phone) {
        return parcelRepository.findByPhone(phone).stream()
                .map(parcelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteParcel(Long id) {
        if (!parcelRepository.existsById(id)) {
            throw new IllegalArgumentException("Parcel not found: " + id);
        }
        parcelRepository.deleteById(id);
    }

    @Override
    public ParcelResponse markAsInTransit(Long id, Long tripId) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found: " + id));

        if (parcel.getStatus() != ParcelStatus.CREATED) {
            throw new IllegalArgumentException("Parcel must be in CREATED status");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new IllegalArgumentException("Trip not found: " + tripId));

        parcel.setTrip(trip);
        parcel.setStatus(ParcelStatus.IN_TRANSIT);
        Parcel updatedParcel = parcelRepository.save(parcel);
        return parcelMapper.toResponse(updatedParcel);
    }

    @Override
    public ParcelResponse markAsDelivered(Long id, String otp, String photoUrl, Long attemptedByUserId) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found: " + id));

        if (parcel.getStatus() != ParcelStatus.IN_TRANSIT) {
            throw new IllegalArgumentException("Parcel must be in IN_TRANSIT status");
        }

        if (!validateOtp(id, otp)) {
            // Create an incident for failed OTP validation
            String incidentNote = String.format(
                    "Failed delivery attempt for parcel %s (code: %s). Invalid OTP provided: %s. Expected: %s",
                    id, parcel.getCode(), otp, parcel.getDeliveryOtp()
            );

            IncidentCreateRequest incidentRequest = new IncidentCreateRequest(
                    EntityType.PARCEL,
                    id,
                    IncidentType.DELIVERY_FAIL,
                    incidentNote,
                    attemptedByUserId
            );

            incidentService.createIncident(incidentRequest);

            // Mark parcel as failed
            parcel.setStatus(ParcelStatus.FAILED);
            Parcel failedParcel = parcelRepository.save(parcel);
            return parcelMapper.toResponse(failedParcel);
        }

        parcel.setStatus(ParcelStatus.DELIVERED);
        parcel.setProofPhotoUrl(photoUrl);
        parcel.setDeliveredAt(LocalDateTime.now());

        Parcel updatedParcel = parcelRepository.save(parcel);
        return parcelMapper.toResponse(updatedParcel);
    }

    @Override
    public ParcelResponse markAsFailed(Long id, String reason) {
        Parcel parcel = parcelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found: " + id));

        if (parcel.getStatus() == ParcelStatus.DELIVERED) {
            throw new IllegalArgumentException("Cannot mark delivered parcel as failed");
        }

        parcel.setStatus(ParcelStatus.FAILED);
        Parcel updatedParcel = parcelRepository.save(parcel);
        return parcelMapper.toResponse(updatedParcel);
    }

    @Override
    @Transactional
    public boolean validateOtp(Long parcelId, String otp) {
        Parcel parcel = parcelRepository.findById(parcelId)
                .orElseThrow(() -> new IllegalArgumentException("Parcel not found: " + parcelId));
        return parcel.getDeliveryOtp().equals(otp);
    }
}
