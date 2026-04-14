package com.bookingplatform.service.discount.rule;

import com.bookingplatform.entity.OfferRule;
import com.bookingplatform.enums.RuleType;
import com.bookingplatform.service.discount.DiscountContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

/**
 * Evaluates TIME_RANGE rules.
 * rule_value JSON: {"start":"12:00","end":"17:00"}
 * Passes if show.startTime is within [start, end).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimeRangeRuleEvaluator implements RuleEvaluator {

    private final ObjectMapper objectMapper;

    @Override
    public RuleType supportedType() {
        return RuleType.TIME_RANGE;
    }

    @Override
    public boolean evaluate(OfferRule rule, DiscountContext context) {
        try {
            JsonNode node = objectMapper.readTree(rule.getRuleValue());
            LocalTime start = LocalTime.parse(node.get("start").asText());
            LocalTime end = LocalTime.parse(node.get("end").asText());
            LocalTime showStart = context.getShow().getStartTime();
            return !showStart.isBefore(start) && showStart.isBefore(end);
        } catch (Exception e) {
            log.warn("Failed to evaluate TIME_RANGE rule {}: {}", rule.getRuleValue(), e.getMessage());
            return false;
        }
    }
}
