package com.bookingplatform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OfferResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String discountType;
    private BigDecimal discountValue;
    private String appliesTo;
    private Integer nthTicketNumber;
    private Boolean stackable;
}
