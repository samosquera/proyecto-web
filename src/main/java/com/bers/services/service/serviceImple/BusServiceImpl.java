package com.bers.services.service.serviceImple;

import com.bers.api.dtos.BusDtos.BusCreateRequest;
import com.bers.api.dtos.BusDtos.BusResponse;
import com.bers.api.dtos.BusDtos.BusUpdateRequest;
import com.bers.domain.entities.Bus;
import com.bers.domain.entities.enums.BusStatus;
import com.bers.domain.repositories.BusRepository;
import com.bers.services.mappers.BusMapper;
import com.bers.services.service.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BusServiceImpl implements BusService {

    private final BusRepository busRepository;
    private final BusMapper busMapper;

    @Override
    public BusResponse createBus(BusCreateRequest request) {
        if (busRepository.existsByPlate(request.plate())) {
            throw new IllegalArgumentException("Bus plate already exists: " + request.plate());
        }

        Bus bus = busMapper.toEntity(request);
        Bus savedBus = busRepository.save(bus);
        return busMapper.toResponse(savedBus);
    }

    @Override
    public BusResponse updateBus(Long id, BusUpdateRequest request) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + id));

        busMapper.updateEntity(request, bus);
        Bus updatedBus = busRepository.save(bus);
        return busMapper.toResponse(updatedBus);
    }

    @Override
    @Transactional
    public BusResponse getBusById(Long id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + id));
        return busMapper.toResponse(bus);
    }

    @Override
    @Transactional
    public BusResponse getBusWithSeats(Long id) {
        Bus bus = busRepository.findByIdWithSeats(id)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + id));
        return busMapper.toResponse(bus);
    }

    @Override
    @Transactional
    public BusResponse getBusbyPlate(String plate) {
        Bus bus = busRepository.findByPlate(plate)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found with plate: " + plate));
        return busMapper.toResponse(bus);
    }

    @Override
    @Transactional
    public List<BusResponse> getAllBuses() {
        return busRepository.findAll().stream()
                .map(busMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BusResponse> getBusesByStatus(BusStatus status) {
        return busRepository.findByStatus(status).stream()
                .map(busMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<BusResponse> getAvailableBuses(Integer minCapacity) {
        return busRepository.findAvailableBusesByCapacity(BusStatus.ACTIVE, minCapacity).stream()
                .map(busMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBus(Long id) {
        if (!busRepository.existsById(id)) {
            throw new IllegalArgumentException("Bus not found: " + id);
        }
        busRepository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean existsByPlate(String plate) {
        return busRepository.existsByPlate(plate);
    }

    @Override
    public BusResponse changeBusStatus(Long id, BusStatus status) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bus not found: " + id));
        bus.setStatus(status);
        Bus updatedBus = busRepository.save(bus);
        return busMapper.toResponse(updatedBus);
    }
}
