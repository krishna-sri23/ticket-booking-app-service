package com.bookingplatform.service.discount;

import com.bookingplatform.entity.Show;
import com.bookingplatform.entity.ShowSeat;
import com.bookingplatform.entity.Theatre;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * All the context a discount engine needs to evaluate offers for a booking.
 */
@Data
@Builder
public class DiscountContext {
    private Show show;
    private Theatre theatre;
    private Long cityId;
    private List<ShowSeat> seats;
}
