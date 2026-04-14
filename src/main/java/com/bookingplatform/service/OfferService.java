package com.bookingplatform.service;

import com.bookingplatform.dto.response.OfferResponse;
import com.bookingplatform.entity.Offer;
import com.bookingplatform.repository.OfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;

    @Transactional(readOnly = true)
    public List<OfferResponse> getActiveOffers() {
        return offerRepository.findActiveOffers(LocalDate.now()).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private OfferResponse toResponse(Offer o) {
        return OfferResponse.builder()
                .id(o.getId())
                .code(o.getCode())
                .name(o.getName())
                .description(o.getDescription())
                .discountType(o.getDiscountType().name())
                .discountValue(o.getDiscountValue())
                .appliesTo(o.getAppliesTo().name())
                .nthTicketNumber(o.getNthTicketNumber())
                .stackable(o.getStackable())
                .build();
    }
}
