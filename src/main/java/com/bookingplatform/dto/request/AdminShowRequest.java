package com.bookingplatform.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AdminShowRequest {

    @NotNull(message = "movieId is required")
    private Long movieId;

    @NotNull(message = "screenId is required")
    private Long screenId;

    @NotNull(message = "showDate is required")
    private LocalDate showDate;

    @NotNull(message = "startTime is required")
    private LocalTime startTime;

    @NotNull(message = "endTime is required")
    private LocalTime endTime;

    @NotNull(message = "basePrice is required")
    @Positive(message = "basePrice must be positive")
    private BigDecimal basePrice;
}
