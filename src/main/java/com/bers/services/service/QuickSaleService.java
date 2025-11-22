package com.bers.services.service;

import com.bers.api.dtos.QuickSaleDtos.AvailableQuickSaleSeatsResponse;
import com.bers.api.dtos.QuickSaleDtos.QuickSaleRequest;
import com.bers.api.dtos.QuickSaleDtos.QuickSaleResponse;

public interface QuickSaleService {
    QuickSaleResponse createQuickSale(QuickSaleRequest request);

    AvailableQuickSaleSeatsResponse getAvailableQuickSaleSeats(Long tripId);
}
