package com.bookingplatform.service.discount.rule;

import com.bookingplatform.entity.OfferRule;
import com.bookingplatform.entity.Show;
import com.bookingplatform.service.discount.DiscountContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimeRangeRuleEvaluatorTest {

    private final TimeRangeRuleEvaluator evaluator = new TimeRangeRuleEvaluator(new ObjectMapper());

    private DiscountContext ctxWithStartTime(LocalTime start) {
        Show show = new Show();
        show.setStartTime(start);
        return DiscountContext.builder().show(show).build();
    }

    private OfferRule rule(String json) {
        OfferRule r = new OfferRule();
        r.setRuleValue(json);
        return r;
    }

    @Test
    void passesForAfternoonShowInsideWindow() {
        assertTrue(evaluator.evaluate(
                rule("{\"start\":\"12:00\",\"end\":\"17:00\"}"),
                ctxWithStartTime(LocalTime.of(14, 30))));
    }

    @Test
    void failsForShowBeforeWindow() {
        assertFalse(evaluator.evaluate(
                rule("{\"start\":\"12:00\",\"end\":\"17:00\"}"),
                ctxWithStartTime(LocalTime.of(11, 59))));
    }

    @Test
    void endIsExclusive() {
        assertFalse(evaluator.evaluate(
                rule("{\"start\":\"12:00\",\"end\":\"17:00\"}"),
                ctxWithStartTime(LocalTime.of(17, 0))));
    }

    @Test
    void startIsInclusive() {
        assertTrue(evaluator.evaluate(
                rule("{\"start\":\"12:00\",\"end\":\"17:00\"}"),
                ctxWithStartTime(LocalTime.of(12, 0))));
    }

    @Test
    void malformedRuleFailsSafely() {
        assertFalse(evaluator.evaluate(rule("not-json"), ctxWithStartTime(LocalTime.NOON)));
    }
}
