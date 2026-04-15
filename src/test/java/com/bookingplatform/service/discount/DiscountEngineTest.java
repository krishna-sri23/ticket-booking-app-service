package com.bookingplatform.service.discount;

import com.bookingplatform.entity.*;
import com.bookingplatform.enums.AppliesTo;
import com.bookingplatform.enums.DiscountType;
import com.bookingplatform.repository.OfferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscountEngineTest {

    private OfferRepository offerRepository;
    private DiscountEngine engine;

    @BeforeEach
    void setUp() {
        offerRepository = mock(OfferRepository.class);
        engine = new DiscountEngine(offerRepository, Collections.emptyList());
        engine.init();
    }

    private ShowSeat seat(BigDecimal price) {
        ShowSeat s = new ShowSeat();
        s.setPrice(price);
        return s;
    }

    private DiscountContext ctx(List<ShowSeat> seats) {
        Theatre t = new Theatre();
        t.setId(1L);
        City c = new City();
        c.setId(1L);
        t.setCity(c);
        Show show = new Show();
        return DiscountContext.builder().show(show).theatre(t).cityId(1L).seats(seats).build();
    }

    private Offer nthTicket50Percent() {
        Offer o = Offer.builder()
                .code("THIRD_TICKET_50")
                .name("50% off 3rd")
                .discountType(DiscountType.PERCENTAGE)
                .discountValue(new BigDecimal("50"))
                .appliesTo(AppliesTo.NTH_TICKET)
                .nthTicketNumber(3)
                .priority(1)
                .stackable(true)
                .active(true)
                .validFrom(LocalDate.MIN)
                .validUntil(LocalDate.MAX)
                .build();
        return o;
    }

    @Test
    void nthTicketDiscountAppliesToMostExpensiveNthSeat() {
        when(offerRepository.findActiveOffers(any())).thenReturn(List.of(nthTicket50Percent()));

        List<ShowSeat> seats = List.of(
                seat(new BigDecimal("100.00")),
                seat(new BigDecimal("300.00")),
                seat(new BigDecimal("200.00")));

        List<AppliedOffer> applied = engine.calculateDiscounts(ctx(seats));

        assertEquals(1, applied.size());
        // Sorted desc: 300, 200, 100. 3rd = 100. 50% = 50.00
        assertEquals(0, new BigDecimal("50.00").compareTo(applied.get(0).getDiscountAmount()));
    }

    @Test
    void nthTicketReturnsNoDiscountBelowThreshold() {
        when(offerRepository.findActiveOffers(any())).thenReturn(List.of(nthTicket50Percent()));
        List<ShowSeat> seats = List.of(
                seat(new BigDecimal("100.00")),
                seat(new BigDecimal("100.00")));
        List<AppliedOffer> applied = engine.calculateDiscounts(ctx(seats));
        assertTrue(applied.isEmpty());
    }

    @Test
    void flatPerBookingCannotExceedTotal() {
        Offer flat = Offer.builder()
                .code("FLAT_500")
                .name("Flat 500")
                .discountType(DiscountType.FLAT)
                .discountValue(new BigDecimal("500"))
                .appliesTo(AppliesTo.PER_BOOKING)
                .priority(1).stackable(true).active(true)
                .validFrom(LocalDate.MIN).validUntil(LocalDate.MAX)
                .build();
        when(offerRepository.findActiveOffers(any())).thenReturn(List.of(flat));

        List<ShowSeat> seats = List.of(seat(new BigDecimal("200.00")));
        List<AppliedOffer> applied = engine.calculateDiscounts(ctx(seats));

        assertEquals(1, applied.size());
        // Flat 500 capped at total 200
        assertEquals(0, new BigDecimal("200.00").compareTo(applied.get(0).getDiscountAmount()));
    }
}
