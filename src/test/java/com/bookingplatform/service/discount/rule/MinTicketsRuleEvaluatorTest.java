package com.bookingplatform.service.discount.rule;

import com.bookingplatform.entity.OfferRule;
import com.bookingplatform.entity.ShowSeat;
import com.bookingplatform.service.discount.DiscountContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinTicketsRuleEvaluatorTest {

    private final MinTicketsRuleEvaluator evaluator = new MinTicketsRuleEvaluator(new ObjectMapper());

    private OfferRule rule(int count) {
        OfferRule r = new OfferRule();
        r.setRuleValue("{\"count\":" + count + "}");
        return r;
    }

    private DiscountContext ctxWithSeats(int n) {
        List<ShowSeat> seats = Collections.nCopies(n, new ShowSeat());
        return DiscountContext.builder().seats(seats).build();
    }

    @Test
    void passesWhenExactlyMeetingMinimum() {
        assertTrue(evaluator.evaluate(rule(3), ctxWithSeats(3)));
    }

    @Test
    void passesWhenExceedingMinimum() {
        assertTrue(evaluator.evaluate(rule(3), ctxWithSeats(5)));
    }

    @Test
    void failsWhenBelowMinimum() {
        assertFalse(evaluator.evaluate(rule(3), ctxWithSeats(2)));
    }
}
