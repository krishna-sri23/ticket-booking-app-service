package com.bookingplatform.service.discount;

import com.bookingplatform.entity.Offer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AppliedOffer {
    private Offer offer;
    private BigDecimal discountAmount;
}
