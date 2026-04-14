package com.bookingplatform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
public class ShowTimingResponse {
    private Long showId;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal basePrice;
    private String screenName;
    private Integer availableSeats;
}
