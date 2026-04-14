package com.bookingplatform.controller;

import com.bookingplatform.dto.response.OfferResponse;
import com.bookingplatform.service.OfferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Offers", description = "List active offers")
@RestController
@RequestMapping("/api/v1/offers")
@RequiredArgsConstructor
public class OfferController {

    private final OfferService offerService;

    @Operation(summary = "List all active offers currently available on the platform")
    @GetMapping
    public List<OfferResponse> getActiveOffers() {
        return offerService.getActiveOffers();
    }
}
