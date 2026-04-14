package com.bookingplatform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AppliedOfferResponse {
    private String code;
    private String name;
    private BigDecimal discountApplied;
}
