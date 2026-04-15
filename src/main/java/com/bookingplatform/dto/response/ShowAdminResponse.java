package com.bookingplatform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
public class ShowAdminResponse {
    private Long showId;
    private Long movieId;
    private String movieTitle;
    private Long screenId;
    private String screenName;
    private String theatreName;
    private LocalDate showDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private BigDecimal basePrice;
    private String status;
    private Integer totalSeats;
}
