package com.bookingplatform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ShowSeatResponse {
    private Long showSeatId;
    private String seatNumber;
    private String rowLabel;
    private String seatType;
    private String status;
    private BigDecimal price;
}
