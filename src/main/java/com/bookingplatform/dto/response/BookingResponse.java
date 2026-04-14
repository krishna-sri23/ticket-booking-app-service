package com.bookingplatform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BookingResponse {
    private Long bookingId;
    private Long userId;
    private Long showId;
    private String movieTitle;
    private String theatreName;
    private String screenName;
    private List<String> bookedSeats;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    private List<AppliedOfferResponse> appliedOffers;
    private String status;
    private LocalDateTime bookedAt;
}
